package com.asistente.core.data.remote

import com.asistente.core.domain.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRemoteService(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("users")

    suspend fun getUserByIdRemote(id: String): User? {
        return try {
            val document = collection.document(id).get().await()
            document.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUserByEmailRemote(email: String): User? {
        return try {
            val query = collection
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()
            query.documents.firstOrNull()?.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUserByCodeQRRemote(code: String): User? {
        return try {
            val query = collection
                .whereEqualTo("code", code)
                .limit(1)
                .get()
                .await()
            query.documents.firstOrNull()?.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }
}