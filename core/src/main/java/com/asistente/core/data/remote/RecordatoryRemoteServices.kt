package com.asistente.core.data.remote

import com.asistente.core.domain.models.Recordatory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RecordatoryRemoteServices (
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("Recordatorys")

    suspend fun getRecordatoryByIdRemote(id: String): Recordatory? {
        return try {
            val document = collection.document(id).get().await()
            document.toObject(Recordatory::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllRecordatorysByUserIdRemote(userid: String): List<Recordatory> {
        return try {
            val data = collection
                .whereArrayContains("owners", userid)
                .get()
                .await()
            data.toObjects(Recordatory::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }


    suspend fun getAllRecordatorysByCalendarIdRemote(calendarId: String): List<Recordatory> {
        return try {
            val data = collection
                .whereEqualTo("parentCalendarId", calendarId)
                .get()
                .await()
            data.toObjects(Recordatory::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }


    suspend fun saveRecordatoryRemote(Recordatory: Recordatory): Boolean {
        return try {
            collection.document(Recordatory.id).set(Recordatory).await()
            true
        } catch (e: Exception) {
            false
        }


    }

    suspend fun deleteRecordatoryRemote(RecordatoryId: String): Boolean {
        return try {
            collection.document(RecordatoryId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}