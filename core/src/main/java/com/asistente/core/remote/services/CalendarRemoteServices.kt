package com.asistente.core.remote.services



import com.asistente.core.models.Calendar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CalendarRemoteServices(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("calendars")

    suspend fun getAllUsersRemote(): List<Calendar> {
        return try {
            val data = collection.get().await()
            data.toObjects(Calendar::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserByIdRemote(id: String): Calendar? {
        return try {
            val document = collection.document(id).get().await()
            document.toObject(Calendar::class.java)
        } catch (e: Exception) {
            null
        }
    }
}