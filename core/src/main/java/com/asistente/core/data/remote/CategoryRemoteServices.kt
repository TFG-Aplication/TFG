package com.asistente.core.data.remote

import com.asistente.core.domain.models.Category
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CategoryRemoteServices  @Inject constructor(
private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("Categorys")

    suspend fun getCategoryByIdRemote(id: String): Category? {
        return try {
            val document = collection.document(id).get().await()
            document.toObject(Category::class.java)
        } catch (e: Exception) {
            null
        }
    }


    suspend fun getAllCategorysByCalendarIdRemote(calendarId: String): List<Category> {
        return try {
            val data = collection
                .whereEqualTo("parentCalendarId", calendarId)
                .get()
                .await()
            data.toObjects(Category::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }


    suspend fun saveCategoryRemote(Category: Category): Boolean {
        return try {
            collection.document(Category.id).set(Category).await()
            true
        } catch (e: Exception) {
            false
        }


    }

    suspend fun deleteCategoryRemote(CategoryId: String): Boolean {
        return try {
            collection.document(CategoryId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
}