package com.asistente.core.data.remote

import com.asistente.core.domain.models.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.text.get
import kotlin.text.set

class TaskRemoteServices @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("tasks")

    suspend fun getTaskByIdRemote(id: String): Task? {
        return try {
            val document = collection.document(id).get().await()
            document.toObject(Task::class.java)
        } catch (e: Exception) {
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
            emptyList()
        }
    }


    suspend fun saveTaskRemote(task: Task): Boolean {
        return try {
            collection.document(task.id).set(task).await()
            true
        } catch (e: Exception) {
            false
        }


    }

    suspend fun deleteTaskRemote(taskId: String): Boolean {
        return try {
            collection.document(taskId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}