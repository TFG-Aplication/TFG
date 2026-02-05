package com.asistente.core.data.remote

import com.asistente.core.domain.models.Calendar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.text.get
import kotlin.text.set

class CalendarRemoteServices @Inject constructor(

    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("calendars")

    suspend fun getAllCalendarByUserIdRemote(userId: String): List<Calendar> {
        return try {
            val data = collection
                .whereArrayContains("owners", userId)
                .get()
                .await()
            data.toObjects(Calendar::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCalendarByIdRemote(id: String): Calendar? {
        return try {
            val document = collection.document(id).get().await()
            document.toObject(Calendar::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveCalendarRemote(calendar: Calendar): Boolean {
        return try {
            collection.document(calendar.id).set(calendar).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteCalendarRemote(id: String): Boolean {
        return try {
            collection.document(id).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

}