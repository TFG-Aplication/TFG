package com.asistente.core.remote.services

import com.asistente.core.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRemoteService(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("users")

    suspend fun getAllUsersRemote(): List<User> {
        return try {
            val data = collection.get().await()
            data.toObjects(User::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserByIdRemote(id: String): User? {
        return try {
            val document = collection.document(id).get().await()
            document.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }
}