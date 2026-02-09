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
            val data = collection
                .whereEqualTo("parentCalendarId", calendarId)
                .get()
                .await()
            return data.toObjects(Category::class.java)
    }


    suspend fun saveCategoryRemote(Category: Category) {
        collection.document(Category.id).set(Category).await()
    }

    suspend fun deleteCategoryRemote(CategoryId: String) {
            collection.document(CategoryId).delete().await()
    }
    
}