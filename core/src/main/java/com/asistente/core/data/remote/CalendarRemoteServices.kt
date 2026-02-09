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

            val data = collection
                .whereArrayContains("owners", userId)
                .get()
                .await()
            return data.toObjects(Calendar::class.java)

    }

    suspend fun getCalendarByIdRemote(id: String): Calendar? {
        return try {
            val document = collection.document(id).get().await()
            document.toObject(Calendar::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveCalendarRemote(calendar: Calendar) {
            collection.document(calendar.id).set(calendar).await()

    }

    suspend fun deleteCalendarRemote(id: String) {
            collection.document(id).delete().await()

    }

}