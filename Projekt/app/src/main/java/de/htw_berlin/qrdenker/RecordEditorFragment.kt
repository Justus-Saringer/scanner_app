package de.htw_berlin.qrdenker

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.eu.fragmentstatemanager.StateManager
import de.htw_berlin.qrdenker.MainActivity.Companion.CREATING_ID
import de.htw_berlin.qrdenker.databinding.RecordEditorFragmentBinding
import java.lang.IllegalArgumentException
import android.graphics.drawable.BitmapDrawable
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import kotlinx.android.synthetic.main.record_editor_fragment.*
import java.util.*


class RecordEditorFragment : Fragment() {

    companion object {
        fun newInstance() = RecordEditorFragment()
    }

    private var _binding: RecordEditorFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecordSharedViewModel by activityViewModels()
    private val pickImage = 100
    private lateinit var titleTextView : TextView
    private lateinit var saveBtn : Button
    private lateinit var uploadImageBtn: Button
    private lateinit var imageView: ImageView
    private lateinit var backBtn: Button

    private var isHeadingEditor : Boolean = false
    private var isImageEditor : Boolean = false
    private var isTextEditor : Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = RecordEditorFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        titleTextView = binding.editorTitleTextview
        titleTextView.text = viewModel.title

        saveBtn = binding.editorSaveBtn
        uploadImageBtn = binding.editorImageUploadBtn
        imageView = binding.editorImageImageView
        backBtn = binding.editorGobackBtn
        binding.editorHeadingEditText.isVisible = false
        binding.editorImageLayout.isVisible = false
        binding.editorTextEditText.isVisible = false

        when(viewModel.currentEditorItem){
            is RecordConstructorRecyclerViewItem.Heading -> {
                binding.editorHeadingEditText.isVisible = true
                binding.editorHeadingEditText.setText(
                    (viewModel.currentEditorItem as RecordConstructorRecyclerViewItem.Heading).heading
                )
                isHeadingEditor = true
            }
            is RecordConstructorRecyclerViewItem.Image ->{
                binding.editorImageLayout.isVisible = true
                binding.editorImageImageView.setImageBitmap(
                    (viewModel.currentEditorItem as RecordConstructorRecyclerViewItem.Image).imageBmp
                )
                isImageEditor = true
            }
            is RecordConstructorRecyclerViewItem.Text ->{
                binding.editorTextEditText.isVisible = true
                binding.editorTextEditText.setText(
                    (viewModel.currentEditorItem as RecordConstructorRecyclerViewItem.Text).text
                )
                isTextEditor = true
            }
            else -> throw  IllegalArgumentException("Invalid ItemType provided")
        }
        saveBtn.setOnClickListener {
                if (isHeadingEditor){
                    viewModel.currentEditorItem = RecordConstructorRecyclerViewItem.Heading(
                        binding.editorHeadingEditText.text.toString()
                    )
                }
                if (isImageEditor){
                    if ((viewModel.currentEditorItem as RecordConstructorRecyclerViewItem.Image)
                            .imageName.isNotBlank()){
                        viewModel.currentEditorItem = RecordConstructorRecyclerViewItem.Image(
                            (imageView.getDrawable() as BitmapDrawable).bitmap,
                            (viewModel.currentEditorItem as RecordConstructorRecyclerViewItem.Image).imageName, uri = viewModel.imageUri!!)
                    }
                    else{
                        viewModel.currentEditorItem = RecordConstructorRecyclerViewItem.Image(
                        (imageView.getDrawable() as BitmapDrawable).bitmap,
                        "${UUID.randomUUID()}.jpg", uri = viewModel.imageUri!!
                    )}

                }
                if(isTextEditor){
                    viewModel.currentEditorItem = RecordConstructorRecyclerViewItem.Text(
                        binding.editorTextEditText.text.toString()
                    )
                }
            if (viewModel.isNewItem) viewModel.addItem()
            else viewModel.replaceItem()
            StateManager.getInstance().removeAllFragmentStream(CREATING_ID, RecordFragment())
        }

        uploadImageBtn.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }

        backBtn.setOnClickListener{
            StateManager.getInstance().removeAllFragmentStream(CREATING_ID, RecordFragment())
        }
        val callback: OnBackPressedCallback =
            object: OnBackPressedCallback(true)
            {
                override fun handleOnBackPressed() {
                    StateManager.getInstance().removeAllFragmentStream(CREATING_ID, RecordFragment())
                }
            }
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        // eventhandling if the done button on the softkeyboard gets pressed
        editor_heading_edit_text.setOnEditorActionListener { _, keyCode, event ->
            if (((event?.action ?: -1) == KeyEvent.ACTION_DOWN)
                || keyCode == EditorInfo.IME_ACTION_DONE) {

                hideKeyboard()
                if (isHeadingEditor){
                    viewModel.currentEditorItem = RecordConstructorRecyclerViewItem.Heading(
                        binding.editorHeadingEditText.text.toString()
                    )
                }
                if (viewModel.isNewItem) viewModel.addItem()
                else viewModel.replaceItem()
                StateManager.getInstance().removeAllFragmentStream(CREATING_ID, RecordFragment())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            viewModel.imageUri = data?.data
            if (viewModel.imageUri != null) {
                imageView.setImageURI(viewModel.imageUri)
                (viewModel.currentEditorItem as
                        RecordConstructorRecyclerViewItem.Image).uri = viewModel.imageUri!!
            }
        }
    }

    // methods to hide the softkeyboard
    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}