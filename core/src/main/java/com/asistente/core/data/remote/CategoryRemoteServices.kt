package com.asistente.core.data.remote

import android.util.Log
import com.asistente.core.domain.models.Category
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CategoryRemoteServices  @Inject constructor(
private val firestore: FirebaseFirestore
) {

    companion object {
        private const val TAG = "Tag"
            private const val COLLECTION_CATEGORIES = "categories"
    }
    private val collection = firestore.collection(COLLECTION_CATEGORIES)

    suspend fun getCategoryByIdRemote(id: String): Category? {
        return try {
            val document = collection.document(id).get().await()
            document.toObject(Category::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener categoria $id: ${e.message}", e)
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
            Log.e(TAG, "Error al obtener categorias: ${e.message}", e)
            emptyList()
        }
    }


    suspend fun saveCategoryRemote(Category: Category): Boolean {
        return try {
            collection.document(Category.id).set(Category).await()
            Log.d(TAG, "categories guardado: ${Category.id}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar categories: ${e.message}", e)
            false
        }
    }

    suspend fun deleteCategoryRemote(id: String): Boolean {
        return try {
            collection.document(id).delete().await()
            Log.d(TAG, "categories eliminado: $id")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar categories: ${e.message}", e)
            false
        }
    }

    suspend fun existsCategory(id: String): Boolean {
        return try {
            collection.document(id).get().await().exists()
        } catch (e: Exception) {
            false
        }
    }
    
}