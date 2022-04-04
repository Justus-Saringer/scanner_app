package de.htw_berlin.qrdenker

import android.Manifest
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eu.fragmentstatemanager.StateManager
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.draw.LineSeparator
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import de.htw_berlin.qrdenker.MainActivity.Companion.CREATING_ID
import de.htw_berlin.qrdenker.MainActivity.Companion.SEARCH_ID
import de.htw_berlin.qrdenker.databinding.FragmentSearchBinding
import de.htw_berlin.qrdenker.firebase.viewmodel.DocumentViewModel
import de.htw_berlin.qrdenker.firebase.viewmodel.DocumentViewModelState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class SearchFragment : Fragment() {

    private val fileName: String = "qrcode.pdf"
    private var binding : FragmentSearchBinding? = null

    private val documentViewModel : DocumentViewModel by activityViewModels()
    private val printViewModel : PrintViewModel by viewModels()


    internal lateinit var recyclerView : RecyclerView
    private lateinit var viewModel : SearchViewModel

    // false: document, true collection
    private var documentOrCellection: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initFlow()
        viewModel = ViewModelProvider(this.requireActivity()).get(SearchViewModel::class.java)
        binding = FragmentSearchBinding.inflate(layoutInflater, container, false)
        setHasOptionsMenu(true)
        if (binding == null) throw  java.lang.Exception("SearchFragmentBinding was null")
        return binding?.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.print_menu, menu)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Dexter.withActivity(this.activity)
            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object: PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {

                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {

                }

            })
            .check()

        recyclerView = requireNotNull(binding?.searchResultRecyclerView)
        recyclerView.adapter = viewModel.searchRecyclerViewAdapter
        recyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        recyclerView.setHasFixedSize(true)


        binding?.documentTxtview?.setTypeface(Typeface.SANS_SERIF,Typeface.BOLD)

        binding?.searchSwitch?.setOnCheckedChangeListener{ buttonView, isChecked ->
            documentOrCellection = if (isChecked){
                binding?.documentTxtview?.setTypeface(null,Typeface.NORMAL)
                binding?.collectionTxtview?.setTypeface(null, Typeface.BOLD)
                true
            } else{
                binding?.documentTxtview?.setTypeface(null,Typeface.BOLD)
                binding?.collectionTxtview?.setTypeface(null, Typeface.NORMAL)
                false
            }
        }

        // initializing the search with dedicated button
        binding?.searchSearchBtn?.setOnClickListener{
            documentViewModel.search( "testCollection" , binding?.searchInputEdtxt?.text.toString())
            // Code for search
        }
        // or with the done button of the softkeyboard
        binding?.searchInputEdtxt?.setOnEditorActionListener { _, keyCode, event ->
            if (((event?.action ?: -1) == KeyEvent.ACTION_DOWN)
                || keyCode == EditorInfo.IME_ACTION_DONE) {
                // Code for search
                documentViewModel.search( "testCollection" , binding?.searchInputEdtxt?.text.toString())
                hideKeyboard()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        // or
        binding?.searchInputEdtxt?.setOnKeyListener(View.OnKeyListener{ v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP){
                // Code for search
                hideKeyboard()
                documentViewModel.search( "testCollection" , binding?.searchInputEdtxt?.text.toString())
                return@OnKeyListener true
            }
            false
        })

        viewModel.searchRecyclerViewAdapter.setOnItemClickListener(object : SearchRecyclerViewAdapter.onItemClickListener
        {
            override fun onItemClick(position: Int) {
                if((activity as MainActivity).privilege){
                    documentViewModel.openDocument(position)
                    StateManager.getInstance().showFragment(CREATING_ID, RecordFragment())
                }
                else{
                    documentViewModel.openDocument(position)
                    StateManager.getInstance().showFragment(SEARCH_ID, RecordReadOnlyFragment())
                }

            }
        })

        val callback: OnBackPressedCallback =
            object: OnBackPressedCallback(true)
            {
                override fun handleOnBackPressed() {
                    StateManager.getInstance().removeAllFragmentStream(SEARCH_ID, SearchFragment())
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        val itemTouchHelper = ItemTouchHelper(wischiWaschi())
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    fun wischiWaschi() : SwipeGesture {


            val swipeGesture = object : SwipeGesture(this.requireContext()) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    when (direction) {
                        ItemTouchHelper.LEFT -> {
                            val docModel =
                                viewModel.searchRecyclerViewAdapter.items[viewHolder.adapterPosition]
                            //delete from firestore
                            documentViewModel.delete(docModel.id)
                            //delete from recyclerview
                            viewModel.deleteItem(viewHolder.adapterPosition)
                        }
                    }
                }
            }

        return  swipeGesture
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            R.id.printing ->{
                val idList = mutableListOf<Pair<String, String>>()
                viewModel.searchRecyclerViewAdapter.items.forEach { item ->
                    idList.add(
                        Pair(
                            item.id,
                            item.title
                        )
                    )
                }
                printViewModel.generateQRCodes(idList)
                createPDFFile(Common.getAppPath(requireActivity())+fileName, printViewModel.qrCodeList.toTypedArray())
            }
        }
        return true
    }

    private fun initFlow() {
        lifecycleScope.launch(Dispatchers.Main) {
            whenCreated {
                documentViewModel.documentViewModelState.collect{
                    when(it) {
                        is DocumentViewModelState.UploadSuccess -> {
                            Log.d("???", "${it.documentModel}")
                            Toast.makeText(activity , "upload succes", Toast.LENGTH_LONG).show()
                        }
                        is DocumentViewModelState.FetchSuccess -> {
                            Log.d("???", "${it.documentModel.toDictionary()}")
                            Toast.makeText(activity , "fetch succes", Toast.LENGTH_LONG).show()
                        }
                        is DocumentViewModelState.SearchFetchSuccess -> {
                            Log.d("???", it.documents.toString())
                            Toast.makeText(activity , "searchfetch succes", Toast.LENGTH_LONG).show()
                            viewModel.updateRecyclerView(it.documents)
                        }
                        is DocumentViewModelState.DeleteSuccess -> {
                            Log.d("???", it.documentId)
                            Toast.makeText(activity , "delete succes", Toast.LENGTH_LONG).show()
                        }
                        is DocumentViewModelState.Loading -> {

                        }
                        is DocumentViewModelState.Empty -> {

                        }
                        is DocumentViewModelState.Error -> {
                            Log.d("???", "${it.message}")
                            Toast.makeText(activity , "request error: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                        is DocumentViewModelState.None -> Unit
                    }
                }
            }
        }
    }

    // methods to hide the softkeyboard
    private fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    private fun createPDFFile(path: String, qrcodeList: Array<Pair<Bitmap,String>>) {
        if (File(path).exists())
            File(path).delete()
        try {
            val document = Document()
            // Save
            PdfWriter.getInstance(document,FileOutputStream(path))
            // Open to write
            document.open()

            // Settings
            document.pageSize = PageSize.A4
            document.addCreationDate()
            document.addAuthor("Ivan Kanunnikov, Justus Saringer")
            document.addCreator("Ivan Kanunnikov, Justus Saringer")

            // Font
            val colorAccent = BaseColor(0,0,0,255)
            val headingFontSize = 12.0f

            // Custom font
            //val fontName = BaseFont.createFont("res/fonts/", "UTF-8" )
            val fontName = Font.FontFamily.HELVETICA

            // Creating the PDF with QR Codes
            val titleStyle = Font(fontName, 18.0f, Font.NORMAL, BaseColor.BLACK)
            addNewItem(document,"QR-Codes generiert mit der QR-Denker", Element.ALIGN_CENTER,titleStyle)

            val headingStyle = Font(fontName, headingFontSize, Font.NORMAL, colorAccent)


            for (qrcode in qrcodeList)
            {
            val stream3 = ByteArrayOutputStream()
            qrcode.first.compress(Bitmap.CompressFormat.JPEG, 50, stream3)

            val img = Image.getInstance(stream3.toByteArray())
            img.alignment = Element.ALIGN_CENTER
            img.scaleToFit(80f,80f)
            document.add(img)

            addNewItem(document, qrcode.second, Element.ALIGN_CENTER,headingStyle)
            //addLineSeperator(document)
            }

            //close
            document.close()
            Toast.makeText(this.requireContext(),"PDF erfolgreich erstellt", Toast.LENGTH_SHORT).show()
            printPDF()

        }catch (e:Exception)
        {
            Log.e("creating PDF",e.message.toString())
        }
    }


    private fun printPDF() {
        val printManager = requireActivity().getSystemService(Context.PRINT_SERVICE) as PrintManager
        try {
            val printAdapter = PdfDocumentAdapter(requireContext(),Common.getAppPath(requireContext())+fileName)
            printManager.print("Document",printAdapter, PrintAttributes.Builder().build())
        }catch (e: Exception){
            Log.e("printing PDF", e.message.toString())
        }
    }

    @Throws(DocumentException::class)
    private fun addLineSeperator(document: Document) {
        val lineSeperator = LineSeparator()
        lineSeperator.lineColor = BaseColor(0,0,0,0)
        addLineSpace(document)
        document.add(Chunk(lineSeperator))
    }

    @Throws(DocumentException::class)
    private fun addLineSpace(document: Document) {
        document.add(Paragraph(""))
    }

    @Throws(DocumentException::class)
    private fun addNewItem(document: Document, text: String, align: Int, style: Font) {
        val chunk = Chunk(text, style)
        val p = Paragraph(chunk)
        p.alignment = align
        document.add(p)
    }
}