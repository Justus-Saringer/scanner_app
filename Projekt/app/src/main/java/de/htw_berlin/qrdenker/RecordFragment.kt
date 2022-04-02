package de.htw_berlin.qrdenker

import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eu.fragmentstatemanager.StateManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.htw_berlin.qrdenker.MainActivity.Companion.CREATING_ID
import de.htw_berlin.qrdenker.databinding.RecordFragmentBinding
import de.htw_berlin.qrdenker.firebase.model.DocumentModel
import de.htw_berlin.qrdenker.firebase.storage.FirebaseStorageManager
import de.htw_berlin.qrdenker.firebase.viewmodel.DocumentViewModel
import de.htw_berlin.qrdenker.firebase.viewmodel.DocumentViewModelState
import io.grpc.ClientStreamTracer
import kotlinx.android.synthetic.main.record_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class RecordFragment : Fragment() {

    companion object {
        fun newInstance() = RecordFragment()
    }

    private var _binding: RecordFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecordSharedViewModel by activityViewModels()
    private val documentViewModel : DocumentViewModel by activityViewModels()
    private lateinit var recyclerView : RecyclerView

    internal lateinit var  addHeadingBtn : FloatingActionButton
    internal lateinit var addImageBtn : FloatingActionButton
    internal lateinit var addTextBtn : FloatingActionButton
    internal lateinit var saveToDbBtn: FloatingActionButton
    internal lateinit var newDocBtn : FloatingActionButton
    internal lateinit var titleEditText : EditText
    internal lateinit var fabMain : FloatingActionButton


    private val rotateOpen : Animation by lazy { AnimationUtils.loadAnimation(this.requireActivity(), R.anim.rotate_open_anim) }
    private val rotateClose : Animation by lazy { AnimationUtils.loadAnimation(this.activity, R.anim.rotate_close_anim) }
    private val fromBottom : Animation by lazy { AnimationUtils.loadAnimation(this.requireContext(), R.anim.from_bottom_anim) }
    private val toBottom : Animation by lazy { AnimationUtils.loadAnimation(this.context, R.anim.to_bottom_anim) }
    private var isClicked : Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        initFlow()
        _binding = RecordFragmentBinding.inflate(layoutInflater, container, false)
        binding.recordSharedViewModel = viewModel
        // Specify the fragment view as the lifecycle owner of the binding.
        // This is used so that the binding can observe LiveData updates
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        viewModel.defaultBitmap = BitmapFactory.decodeResource(this.resources, R.drawable.image_placeholder)

        recyclerView = binding.recordSectionRecyclerView
        recyclerView.apply {
            adapter = viewModel.recordConstructorRecyclerViewAdapter
        }
        recyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        recyclerView.setHasFixedSize(true)

        viewModel.recordConstructorRecyclerViewAdapter.setOnItemClickListener(
            object : RecordConstructorRecyclerViewAdapter.onItemClickListener{
                override fun onItemClick(position: Int) {
                    viewModel.openEditor(position)
                    StateManager.getInstance().showFragment(CREATING_ID, RecordEditorFragment())
                }
            })

        titleEditText = binding.recordTitleEditText
        titleEditText.addTextChangedListener(textWatcher)

        addHeadingBtn = binding.floatingActionButtonTitle
        addImageBtn = binding.floatingActionButtonImage
        addTextBtn = binding.floatingActionButtonText
        saveToDbBtn = binding.floatingActionButtonUpload
        newDocBtn = binding.floatingActionButtonNewDoc
        fabMain = binding.floatingActionButtonMain

        fabMain.setOnClickListener{
            onAddButtonClicked()
        }


        //ItemType Id : 1 -> Heading
        //              2 -> Image
        //              3 -> Text
        addHeadingBtn.setOnClickListener {
            viewModel.openEditor(viewModel.recordConstructorRecyclerViewAdapter.itemCount + 1, 1)
            StateManager.getInstance().showFragment(CREATING_ID, RecordEditorFragment())
        }
        addImageBtn.setOnClickListener {
            viewModel.openEditor(viewModel.recordConstructorRecyclerViewAdapter.itemCount + 1 , 2)
            StateManager.getInstance().showFragment(CREATING_ID, RecordEditorFragment())
        }
        addTextBtn.setOnClickListener {
            viewModel.openEditor(viewModel.recordConstructorRecyclerViewAdapter.itemCount + 1, 3)
            StateManager.getInstance().showFragment(CREATING_ID, RecordEditorFragment())
        }
        newDocBtn.setOnClickListener{
            documentViewModel.openEmptyDocument()
        }

        saveToDbBtn.setOnClickListener {
            addRecord()
        }

        val itemTouchHelper = ItemTouchHelper(wischiWaschi())
        itemTouchHelper.attachToRecyclerView(recyclerView)

        updateView()
    }

    fun updateView(){
        titleEditText.setText( viewModel.title)

    }

    fun wischiWaschi() : SwipeGesture {
        val swipeGesture = object : SwipeGesture(this.requireContext()) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from_pos = viewHolder.adapterPosition
                val to_pos = target.adapterPosition

                Collections.swap(viewModel.recordItems.value, from_pos, to_pos)
                viewModel.recordConstructorRecyclerViewAdapter.notifyItemMoved(from_pos,to_pos)

                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        viewModel.recordConstructorRecyclerViewAdapter.deleteItem(viewHolder.adapterPosition)
                    }
                }
            }
        }
        return  swipeGesture
    }

    private fun addRecord(){
        val documentModel = DocumentModel()

        documentModel.collectionPath = "testCollection"
        documentModel.id = viewModel.id
        documentModel.title = viewModel.title



        documentModel.body = viewModel.recordConstructorRecyclerViewAdapter.items

        var containsImage: Boolean= false

        for (item in documentModel.body){
            if(item is RecordConstructorRecyclerViewItem.Image) containsImage = true
        }
        if (containsImage) documentViewModel.uploadImages(documentModel)
        else documentViewModel.addDocument(documentModel)


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
                        is DocumentViewModelState.UploadImageSuccess -> {
                            Log.d("???", "${it.imagePos} , ${it.imageUrl}")
                            Toast.makeText(activity , "upload succes", Toast.LENGTH_LONG).show()
                            //writing the Storage Url to imageItem
                            (viewModel.recordItems.value!![it.imagePos]
                                    as RecordConstructorRecyclerViewItem.Image).imageUrl = it.imageUrl
                        }
                        is DocumentViewModelState.FetchSuccess -> {
                            Log.d("???", "${it.documentModel.toDictionary()}")
                            Toast.makeText(activity , "fetch succes", Toast.LENGTH_LONG).show()

                            viewModel.updateRecyclerView(it.documentModel.body)
                            viewModel.title = it.documentModel.title
                            viewModel.id = it.documentModel.id
                            updateView()
                        }
                        is DocumentViewModelState.DeleteSuccess -> {
                            Log.d("???", "${it.documentId}")
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

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            viewModel.title = s.toString()
        }
    }

    private fun onAddButtonClicked()
    {
        setVisibility(isClicked)
        setAnimation(isClicked)
        isClicked = !isClicked
    }
    private fun setVisibility(clicked : Boolean)
    {
        if (!clicked)
        {
            floatingActionButton_image.visibility = View.VISIBLE
            floatingActionButton_text.visibility = View.VISIBLE
            floatingActionButton_title.visibility = View.VISIBLE
            floatingActionButton_upload.visibility = View.VISIBLE
            floatingActionButton_newDoc.visibility = View.VISIBLE
        }
        else {
            floatingActionButton_image.visibility = View.INVISIBLE
            floatingActionButton_text.visibility = View.INVISIBLE
            floatingActionButton_title.visibility = View.INVISIBLE
            floatingActionButton_upload.visibility = View.INVISIBLE
            floatingActionButton_newDoc.visibility = View.VISIBLE
        }
    }
    private fun setAnimation(clicked : Boolean)
    {
        if (!clicked)
        {
            floatingActionButton_image.startAnimation(fromBottom)
            floatingActionButton_text.startAnimation(fromBottom)
            floatingActionButton_title.startAnimation(fromBottom)
            floatingActionButton_upload.startAnimation(fromBottom)
            floatingActionButton_main.startAnimation(rotateOpen)
            floatingActionButton_newDoc.startAnimation(fromBottom)
        }
        else
        {
            floatingActionButton_image.startAnimation(toBottom)
            floatingActionButton_text.startAnimation(toBottom)
            floatingActionButton_title.startAnimation(toBottom)
            floatingActionButton_upload.startAnimation(toBottom)
            floatingActionButton_main.startAnimation(rotateClose)
            floatingActionButton_newDoc.startAnimation(toBottom)
        }
    }
}