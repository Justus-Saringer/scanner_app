package de.htw_berlin.qrdenker

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eu.fragmentstatemanager.StateManager
import de.htw_berlin.qrdenker.databinding.FragmentRecordReadOnlyBinding
import de.htw_berlin.qrdenker.databinding.RecordFragmentBinding
import de.htw_berlin.qrdenker.firebase.viewmodel.DocumentViewModel
import de.htw_berlin.qrdenker.firebase.viewmodel.DocumentViewModelState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class RecordReadOnlyFragment : Fragment() {

    private var _binding: FragmentRecordReadOnlyBinding? = null
    private val binding get() = _binding!!

    val viewModel: RecordSharedViewModel by activityViewModels()
    val documentViewModel: DocumentViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var titleTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        initFlow()
        _binding = FragmentRecordReadOnlyBinding.inflate(layoutInflater, container, false)
        binding.recordSharedViewModel = viewModel
        // Specify the fragment view as the lifecycle owner of the binding.
        // This is used so that the binding can observe LiveData updates
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.defaultBitmap = BitmapFactory.decodeResource(this.resources, R.drawable.image_placeholder)

        recyclerView = binding.recordReadOnlyRecyclerView
        recyclerView.apply {
            adapter = viewModel.recordConstructorRecyclerViewAdapter
        }
        recyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        recyclerView.setHasFixedSize(true)
        titleTextView = binding.recordReadOnlyTitleTextView
        titleTextView.text = viewModel.title

        viewModel.recordConstructorRecyclerViewAdapter.setOnItemClickListener(
            object : RecordConstructorRecyclerViewAdapter.onItemClickListener{
                override fun onItemClick(position: Int) {
                    //Do nothing
                }
            })

    }

    private fun initFlow() {
        lifecycleScope.launch(Dispatchers.Main) {
            whenCreated {
                documentViewModel.documentViewModelState.collect{
                    when(it) {
                        is DocumentViewModelState.UploadSuccess -> {
                            Log.d("???", "${it.documentModel}")
                            Toast.makeText(activity , "upload success", Toast.LENGTH_LONG).show()
                        }
                        is DocumentViewModelState.UploadImageSuccess -> {
                            Log.d("???", "${it.imagePos} , ${it.imageUrl}")
                            Toast.makeText(activity , "upload success", Toast.LENGTH_LONG).show()
                            //writing the Storage Url to imageItem
                            (viewModel.recordItems.value!![it.imagePos]
                                    as RecordConstructorRecyclerViewItem.Image).imageUrl = it.imageUrl
                        }
                        is DocumentViewModelState.FetchSuccess -> {
                            Log.d("???", "${it.documentModel.toDictionary()}")
                            Toast.makeText(activity , "fetch success", Toast.LENGTH_LONG).show()

                            viewModel.updateRecyclerView(it.documentModel.body)
                            viewModel.title = it.documentModel.title
                            titleTextView.text = it.documentModel.title
                            viewModel.id = it.documentModel.id
                        }
                        is DocumentViewModelState.SearchFetchSuccess -> {

                        }
                        is DocumentViewModelState.DeleteSuccess -> {
                            Log.d("???", "${it.documentId}")
                            Toast.makeText(activity , "delete success", Toast.LENGTH_LONG).show()
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

}