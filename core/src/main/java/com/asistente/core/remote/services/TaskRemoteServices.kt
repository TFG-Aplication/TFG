package com.asistente.core.remote.services


import com.asistente.core.models.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TaskRemoteServices(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("tasks")

    suspend fun getAllUsersRemote(): List<Task> {
        return try {
            val data = collection.get().await()
            data.toObjects(Task::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserByIdRemote(id: String): Task? {
        return try {
            val document = collection.document(id).get().await()
            document.toObject(Task::class.java)
        } catch (e: Exception) {
            null
        }
    }
}