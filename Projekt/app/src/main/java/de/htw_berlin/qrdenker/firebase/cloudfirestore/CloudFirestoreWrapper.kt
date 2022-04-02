package de.htw_berlin.qrdenker.firebase.cloudfirestore

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object CloudFirestoreWrapper {
    suspend fun replace(collectionPath: String, documentPath : String, map:MutableMap<String,Any>):Void{


        return suspendCoroutine { continuation ->
            var docPath = documentPath

            if (docPath.isBlank()){
                docPath = Firebase.firestore.collection(collectionPath).document().id
                map["ID"] = docPath
            }

            Firebase.firestore.collection(collectionPath).document(docPath).set(map)
                .addOnSuccessListener {
                    continuation.resume(it)
                }
                .addOnFailureListener{
                    continuation.resumeWithException(it)
                }
        }
    }

    suspend fun selectByTitle(collectionPath: String, conditionMap:MutableMap<String, Any>? = null, limit:Long = 15):QuerySnapshot {
        return suspendCoroutine { continuation ->
            val collectionReference = Firebase.firestore.collection(collectionPath)
            var query = collectionReference.limit(limit)

            conditionMap?.let {
                it.forEach{map ->
                    query = collectionReference.whereEqualTo(map.key, map.value)
                }
            }

            query.get()
                .addOnSuccessListener {
                    continuation.resume(it)
                }
                .addOnFailureListener{
                    continuation.resumeWithException(it)
                }
        }
    }

    suspend fun selectById(collectionPath: String, id: String):DocumentSnapshot {
        return suspendCoroutine { continuation ->
            val collectionReference = Firebase.firestore.collection(collectionPath)
            collectionReference.document(id).get()
                .addOnSuccessListener {
                    continuation.resume(it)
                }
                .addOnFailureListener{
                    continuation.resumeWithException(it)
                }
        }
    }

    suspend fun update(collectionPath: String, documentPath: String, map: MutableMap<String, Any>): Void {
        return suspendCoroutine { continuation ->
            Firebase.firestore.collection(collectionPath).document(documentPath).update(map)
                .addOnSuccessListener {
                    continuation.resume(it)
                }
                .addOnFailureListener{
                    continuation.resumeWithException(it)
                }
        }
    }

    suspend fun delete(collectionPath: String, documentPath: String): Void {
        return suspendCoroutine { continuation ->
            Firebase.firestore.collection(collectionPath).document(documentPath).delete()
                .addOnSuccessListener {
                    continuation.resume(it)
                }
                .addOnFailureListener{
                    continuation.resumeWithException(it)
                }
        }
    }
}