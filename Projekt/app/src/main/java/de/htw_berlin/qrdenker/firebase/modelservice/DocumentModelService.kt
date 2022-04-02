package de.htw_berlin.qrdenker.firebase.modelservice

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import de.htw_berlin.qrdenker.firebase.cloudfirestore.CloudFirestoreWrapper
import de.htw_berlin.qrdenker.firebase.model.DocumentModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DocumentModelService {

    suspend fun addDocument(documentModel: DocumentModel):Void = withContext(Dispatchers.IO){
        return@withContext CloudFirestoreWrapper.replace(
            documentModel.collectionPath,
            documentModel.id,
            documentModel.toDictionary()
        )
    }

    suspend fun search(collectionPath: String , title: String): QuerySnapshot = withContext(Dispatchers.IO){
        val map = mutableMapOf<String, Any>()
        if (!title.isNullOrBlank()) {
            map[DocumentModel.TITLE_KEY] = title
        }
        return@withContext CloudFirestoreWrapper.selectByTitle(
            collectionPath,
            map
        )
    }

    suspend fun fetch(collectionPath: String , id: String): DocumentSnapshot = withContext(Dispatchers.IO){

        return@withContext CloudFirestoreWrapper.selectById(
            collectionPath,
            id
        )
    }

    suspend fun modify(documentModel: DocumentModel):Void = withContext(Dispatchers.IO) {
        return@withContext CloudFirestoreWrapper.update(
            documentModel.collectionPath,
            documentModel.id,
            documentModel.toDictionary()
        )
    }

    suspend fun delete(collectionPath: String, documentPath: String): Void = withContext(Dispatchers.IO) {
        return@withContext CloudFirestoreWrapper.delete(collectionPath, documentPath )
    }

}