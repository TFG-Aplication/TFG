package com.asistente.core.data.remote

import android.util.Log
import com.asistente.core.domain.models.Activity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ActivityRemoteServices @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "Tag"
        private const val COLLECTION_ACTIVITY = "activities"
    }

    private val collection = firestore.collection(COLLECTION_ACTIVITY)

    suspend fun getActivityByIdRemote(id: String): Activity? {
        return try {
            collection.document(id).get().await().toObject(Activity::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener activity $id: ${e.message}", e)
            null
        }
    }

    suspend fun getAllActivitiesByCalendarIdRemote(calendarId: String): List<Activity> {
        return try {
            collection.whereEqualTo("parentCalendarId", calendarId).get().await()
                .toObjects(Activity::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener activities: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun saveActivityRemote(activity: Activity): Boolean {
        return try {
            if (activity.categoryId == null) {
                collection.document(activity.id).set(buildMapWithoutNullables(activity)).await()
                try {
                    collection.document(activity.id).update("categoryId", FieldValue.delete()).await()
                } catch (e: Exception) { }
            } else {
                collection.document(activity.id).set(buildMap(activity)).await()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar Activity: ${e.message}", e)
            false
        }
    }

    suspend fun deleteActivityRemote(activityId: String): Boolean {
        return try {
            collection.document(activityId).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar activity: ${e.message}", e)
            false
        }
    }

    suspend fun existsActivity(id: String): Boolean {
        return try {
            collection.document(id).get().await().exists()
        } catch (e: Exception) { false }
    }

    private fun buildMap(activity: Activity): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>(
            "id" to activity.id,
            "name" to activity.name,
            "parentCalendarId" to activity.parentCalendarId,
            "categoryId" to activity.categoryId,
            "owners" to activity.owners,
            "syncStatus" to activity.syncStatus,
            "durationMinutes" to activity.durationMinutes,
            "priority" to activity.priority,
            "is_scheduled" to activity.is_scheduled
        )
        activity.place?.let { map["place"] = it }
        activity.notes?.let { map["notes"] = it }
        activity.alerts?.let { map["alerts"] = it }
        activity.earliest_start?.let { map["earliest_start"] = it }
        activity.deadline?.let { map["deadline"] = it }
        activity.scheduled_start?.let { map["scheduled_start"] = it }
        activity.scheduled_end?.let { map["scheduled_end"] = it }
        activity.firebaseId?.let { map["firebaseId"] = it }
        return map
    }

    private fun buildMapWithoutNullables(activity: Activity): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to activity.id,
            "name" to activity.name,
            "parentCalendarId" to activity.parentCalendarId,
            "owners" to activity.owners,
            "syncStatus" to activity.syncStatus,
            "durationMinutes" to activity.durationMinutes,
            "priority" to activity.priority,
            "is_scheduled" to activity.is_scheduled
        )
        activity.place?.let { map["place"] = it }
        activity.notes?.let { map["notes"] = it }
        activity.alerts?.let { map["alerts"] = it }
        activity.earliest_start?.let { map["earliest_start"] = it }
        activity.deadline?.let { map["deadline"] = it }
        activity.scheduled_start?.let { map["scheduled_start"] = it }
        activity.scheduled_end?.let { map["scheduled_end"] = it }
        activity.firebaseId?.let { map["firebaseId"] = it }
        return map
    }
}