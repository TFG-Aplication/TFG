package com.asistente.core.data.remote

import android.util.Log
import com.asistente.core.domain.models.TimeSlot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TimeSlotRemoteServices @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "Tag"
        private const val COLLECTION = "time_slots"
    }

    private val collection = firestore.collection(COLLECTION)

    suspend fun getTimeSlotByIdRemote(id: String): TimeSlot? {
        return try {
            collection.document(id).get().await().toObject(TimeSlot::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener timeSlot $id: ${e.message}", e)
            null
        }
    }

    suspend fun getAllTimeSlotsByCalendarIdRemote(calendarId: String): List<TimeSlot> {
        return try {
            collection
                .whereEqualTo("parentCalendarId", calendarId)
                .get().await()
                .toObjects(TimeSlot::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener timeSlots: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun saveTimeSlotRemote(timeSlot: TimeSlot): Boolean {
        return try {
            collection.document(timeSlot.id).set(buildMap(timeSlot)).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar TimeSlot: ${e.message}", e)
            false
        }
    }

    suspend fun deleteTimeSlotRemote(timeSlotId: String): Boolean {
        return try {
            collection.document(timeSlotId).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar timeSlot: ${e.message}", e)
            false
        }
    }

    suspend fun existsTimeSlot(id: String): Boolean {
        return try {
            collection.document(id).get().await().exists()
        } catch (e: Exception) { false }
    }

    private fun buildMap(ts: TimeSlot): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>(
            "id"                 to ts.id,
            "name"               to ts.name,
            "parentCalendarId"   to ts.parentCalendarId,
            "owners"             to ts.owners,
            "syncStatus"         to ts.syncStatus,
            "startMinuteOfDay"   to ts.startMinuteOfDay,
            "endMinuteOfDay"     to ts.endMinuteOfDay,
            "daysOfWeek"         to ts.daysOfWeek,
            "recurrenceType"     to ts.recurrenceType.name,
            "slotType"           to ts.slotType.name,
            "enable"           to ts.enable,
            "taskId"             to ts.taskId
        )
        ts.rangeStart?.let  { map["rangeStart"]  = it }
        ts.rangeEnd?.let    { map["rangeEnd"]    = it }
        ts.firebaseId?.let  { map["firebaseId"]  = it }
        return map
    }
}