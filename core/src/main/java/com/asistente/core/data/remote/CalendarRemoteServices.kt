package com.asistente.core.data.remote

import android.util.Log
import com.asistente.core.domain.models.Calendar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CalendarRemoteServices @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "Tag"
        private const val COLLECTION_CALENDARS = "calendars"
    }

    private val collection = firestore.collection(COLLECTION_CALENDARS)

    suspend fun getAllCalendarByUserIdRemote(userId: String): List<Calendar> {
        return try {
            val data = collection
                .whereArrayContains("owners", userId)
                .get()
                .await()

            data.toObjects(Calendar::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener calendarios: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getCalendarByIdRemote(id: String): Calendar? {
        return try {
            val document = collection.document(id).get().await()
            document.toObject(Calendar::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener calendario $id: ${e.message}", e)
            null
        }
    }

    suspend fun saveCalendarRemote(calendar: Calendar): Boolean {
        return try {
            collection.document(calendar.id).set(calendar).await()
            Log.d(TAG, "Calendario guardado: ${calendar.id}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar calendario: ${e.message}", e)
            false
        }
    }

    suspend fun deleteCalendarRemote(id: String): Boolean {
        return try {
            collection.document(id).delete().await()
            Log.d(TAG, "Calendario eliminado: $id")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar calendario: ${e.message}", e)
            false
        }
    }

    suspend fun existsCalendar(id: String): Boolean {
        return try {
            collection.document(id).get().await().exists()
        } catch (e: Exception) {
            false
        }
    }
}