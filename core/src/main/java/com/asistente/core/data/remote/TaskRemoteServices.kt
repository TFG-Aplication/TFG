package com.asistente.core.data.remote

import android.util.Log
import com.asistente.core.domain.models.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.text.get
import kotlin.text.set

class TaskRemoteServices @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val TAG = "Tag"
        private const val COLLECTION_TASK = "tasks"
    }

    private val collection = firestore.collection(COLLECTION_TASK)

    suspend fun getTaskByIdRemote(id: String): Task? {
        return try {
            val document = collection.document(id).get().await()
            document.toObject(Task::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener task $id: ${e.message}", e)
            null
        }
    }

    suspend fun getAllTasksByUserIdRemote(userid: String): List<Task> {
        return try {
            val data = collection
                .whereArrayContains("owners", userid)
                .get()
                .await()
            data.toObjects(Task::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener task: ${e.message}", e)
            emptyList()
        }
    }


    suspend fun getAllTasksByCalendarIdRemote(calendarId: String): List<Task> {
        return try {
            val data = collection
                .whereEqualTo("parentCalendarId", calendarId)
                .get()
                .await()
            data.toObjects(Task::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener task: ${e.message}", e)
            emptyList()
        }
    }


    suspend fun saveTaskRemote(task: Task): Boolean {
        return try {
            if (task.categoryId == null) {
                // Primero guardar sin categoryId
                val taskMap = buildTaskMapWithoutNullables(task)
                collection.document(task.id).set(taskMap).await()
                // Luego eliminar el campo si existía
                try {
                    collection.document(task.id)
                        .update("categoryId", FieldValue.delete())
                        .await()
                } catch (e: Exception) {
                    // El campo puede no existir, ignorar error
                }
            } else {
                val taskMap = buildTaskMap(task)
                collection.document(task.id).set(taskMap).await()
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("Tag", "Error al guardar Task: ${e.message}", e)
            false
        }
    }

    private fun buildTaskMap(task: Task): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>(
            "id" to task.id,
            "name" to task.name,
            "parentCalendarId" to task.parentCalendarId,
            "categoryId" to task.categoryId,
            "owners" to task.owners,
            "syncStatus" to task.syncStatus,
        )
        task.place?.let { map["place"] = it }
        task.notes?.let { map["notes"] = it }
        task.alerts?.let { map["alerts"] = it }
        task.init_date?.let { map["init_date"] = it }
        task.finish_date?.let { map["finish_date"] = it }
        task.firebaseId?.let { map["firebaseId"] = it }
        return map
    }

    // ✅ Mapa sin campos nullable (para cuando categoryId == null)
    private fun buildTaskMapWithoutNullables(task: Task): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to task.id,
            "name" to task.name,
            "parentCalendarId" to task.parentCalendarId,
            "owners" to task.owners,
            "syncStatus" to task.syncStatus,
        )
        task.place?.let { map["place"] = it }
        task.notes?.let { map["notes"] = it }
        task.alerts?.let { map["alerts"] = it }
        task.init_date?.let { map["init_date"] = it }
        task.finish_date?.let { map["finish_date"] = it }
        task.firebaseId?.let { map["firebaseId"] = it }
        return map
    }

    suspend fun deleteTaskRemote(taskId: String): Boolean {
        return try {
            collection.document(taskId).delete().await()
            Log.d(TAG, "task eliminado: $taskId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar task: ${e.message}", e)

            false
        }
    }

    suspend fun existsTask(id: String): Boolean {
        return try {
            collection.document(id).get().await().exists()
        } catch (e: Exception) {
            false
        }
    }
}