package de.htw_berlin.qrdenker.firebase.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.htw_berlin.qrdenker.MainActivity
import de.htw_berlin.qrdenker.RecordConstructorRecyclerViewItem
import de.htw_berlin.qrdenker.firebase.model.DocumentModel
import de.htw_berlin.qrdenker.firebase.modelservice.DocumentModelService
import de.htw_berlin.qrdenker.firebase.storage.FirebaseStorageManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.Exception
import java.text.FieldPosition


sealed class DocumentViewModelState{
    data class UploadSuccess(val documentModel: DocumentModel): DocumentViewModelState()
    data class UploadImageSuccess(val imagePos: Int, val imageUrl : String): DocumentViewModelState()
    data class FetchSuccess(val documentModel: DocumentModel): DocumentViewModelState()
    data class SearchFetchSuccess(val documents: MutableList<DocumentModel>): DocumentViewModelState()
    data class DeleteSuccess(val documentId : String): DocumentViewModelState()
    data class Error(val message:String?): DocumentViewModelState()

    object Empty : DocumentViewModelState()
    object Loading : DocumentViewModelState()

    object None : DocumentViewModelState()
}


class DocumentViewModel: ViewModel() {
    var documentList = mutableListOf<DocumentModel>()
    private val _documentViewModelState = MutableStateFlow<DocumentViewModelState>(DocumentViewModelState.None)
    val documentViewModelState: StateFlow<DocumentViewModelState> = _documentViewModelState

    fun addDocument(documentModel: DocumentModel) = viewModelScope.launch{
        _documentViewModelState.value = DocumentViewModelState.Loading

        try {
            coroutineScope {
                val addDoc = async {
                    DocumentModelService.addDocument(documentModel)
                }
                addDoc.await()

                _documentViewModelState.value = DocumentViewModelState.UploadSuccess(documentModel)
            }
        }
        catch (e: Exception) {
            _documentViewModelState.value = DocumentViewModelState.Error(e.message)
        }

    }

    fun fetch(collectionPath: String, id: String) = viewModelScope.launch {

        _documentViewModelState.value = DocumentViewModelState.Loading

        try {
            coroutineScope {
                val search = async{
                    DocumentModelService.fetch(collectionPath, id)
                }

                val documentSnapshot = search.await()

                val list = mutableListOf<MutableMap<String, Any>>()

                    list.add(documentSnapshot.data as MutableMap<String, Any>)

                if (list.isEmpty()){
                    _documentViewModelState.value = DocumentViewModelState.Empty
                    return@coroutineScope
                }

                val documentModel = DocumentModel()
                documentModel.parsing(list[0])
                for (item in documentModel.body){
                    if (item is RecordConstructorRecyclerViewItem.Image){
                        if (item.imageUrl.isNotBlank()){
                            withContext(Dispatchers.IO){
                                try {
                                    val imageBmp: Bitmap?
                                    val `in` = java.net.URL(item.imageUrl).openStream()
                                    imageBmp = BitmapFactory.decodeStream(`in`)
                                    item.imageBmp = imageBmp
                                }
                                catch (e: Exception) {
                                    Log.e("Error Message", e.message.toString())
                                }
                            }
                        }
                    }
                }

                _documentViewModelState.value = DocumentViewModelState.FetchSuccess(documentModel)
            }
        }
        catch (e: Exception){
            _documentViewModelState.value = DocumentViewModelState.Error(e.message)
        }
    }

    fun search(collectionPath: String, title: String = "") = viewModelScope.launch {

        _documentViewModelState.value = DocumentViewModelState.Loading

        try {
            coroutineScope {
                val search = async{
                    DocumentModelService.search(collectionPath, title)
                }

                val querySnapshot = search.await()

                val list = mutableListOf<MutableMap<String, Any>>()

                for (document in querySnapshot.documents){
                    list.add(document.data as MutableMap<String, Any>)
                }
                if (list.isEmpty()){
                    _documentViewModelState.value = DocumentViewModelState.Empty
                    return@coroutineScope
                }
                val documents = mutableListOf<DocumentModel>()

                for (element in list){
                    val documentModel = DocumentModel()
                    documentModel.parsing(element)

                    for (item in documentModel.body){
                        if (item is RecordConstructorRecyclerViewItem.Image){
                            if (item.imageUrl.isNotBlank()){
                                withContext(Dispatchers.IO){
                                    try {
                                        val imageBmp: Bitmap?
                                        val `in` = java.net.URL(item.imageUrl).openStream()
                                        imageBmp = BitmapFactory.decodeStream(`in`)
                                        item.imageBmp = imageBmp
                                    }
                                    catch (e: Exception) {
                                        Log.e("Error Message", e.message.toString())
                                    }
                                }
                            }
                        }
                    }

                    documents.add(documentModel)
                }
                _documentViewModelState.value = DocumentViewModelState.SearchFetchSuccess(documents)
                documentList = documents
            }
        }
        catch (e: Exception){
            _documentViewModelState.value = DocumentViewModelState.Error(e.message)
        }
    }

    fun delete(documentId: String) = viewModelScope.launch {
        _documentViewModelState.value = DocumentViewModelState.Loading

        try {
            coroutineScope {
                val delete = async {
                    DocumentModelService.delete("testCollection" ,documentId)
                }
                delete.await()

                _documentViewModelState.value = DocumentViewModelState.DeleteSuccess(documentId)
            }
        }
        catch (e: Exception){
            _documentViewModelState.value = DocumentViewModelState.Error(e.message)
        }
    }

    fun openDocument(position: Int){
        _documentViewModelState.value = DocumentViewModelState.FetchSuccess(documentList[position])
    }

    fun openEmptyDocument(){
        _documentViewModelState.value = DocumentViewModelState.FetchSuccess(DocumentModel())
    }

    fun uploadImages(documentModel: DocumentModel) = viewModelScope.launch{
        _documentViewModelState.value = DocumentViewModelState.Loading
        documentModel.body.size
        if (documentModel.body.size > 0) {
            for (i in 0 until documentModel.body.size) {
                if (documentModel.body[i] is RecordConstructorRecyclerViewItem.Image) {
                    if ((documentModel.body[i] as RecordConstructorRecyclerViewItem.Image).imageUrl.isBlank()) {
                        try {
                            coroutineScope {
                                val result = async {
                                    val imageItem =
                                        documentModel.body[i] as RecordConstructorRecyclerViewItem.Image
                                    FirebaseStorageManager.uploadImage(
                                        imageItem.uri,
                                        documentModel.title,
                                        imageItem.imageName
                                    )
                                }
                                val imageStorageUrl = result.await()
                                _documentViewModelState.value =
                                    DocumentViewModelState.UploadImageSuccess(i, imageStorageUrl)
                            }
                            addDocument(documentModel)
                        }
                        catch (e: Exception){
                            _documentViewModelState.value = DocumentViewModelState.Error(e.message)
                        }

                    }

                }

            }
        }
    }
}