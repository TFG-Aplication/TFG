package com.asistente.core.data.repository

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.asistente.core.data.local.daos.CategoryDao
import com.asistente.core.data.worker.CategoryWorker
import com.asistente.core.domain.models.Category
import com.asistente.core.domain.ropositories.interfaz.CategoryRepositoryInterface
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val localCategory: CategoryDao,
    private val workManager: WorkManager
) : CategoryRepositoryInterface {

    override suspend fun getCategoryById(id: String): Category? {
        return localCategory.getCategoryById(id)
    }

    override fun getAllCategoryByCalendarId(calendarId: String): Flow<List<Category>> {
        return localCategory.getAllCategorysByCalendarIdFlow(calendarId)
    }

    override suspend fun saveCategory(category: Category) {
        // Marcar como pendiente de sincronización
        localCategory.insertCategory(category.copy(syncStatus = 0))
        syncWorker(category.parentCalendarId)
        
    }

    override suspend fun updateCategory(category: Category) {
        // Marcar como pendiente de sincronización
        localCategory.updateCategory(category.copy(syncStatus = 0))
        syncWorker(category.parentCalendarId)
    }

    override suspend fun deleteCategory(categoryId: String, isShared: Boolean) {
        if (isShared) {
            // Soft delete: marcar como eliminado (syncStatus = 2)
            val category = localCategory.getCategoryById(categoryId)
            category?.let {
                localCategory.insertCategory(category.copy(syncStatus = 2))

                // Programar sincronización para eliminar en Firebase
                syncWorker(it.parentCalendarId)
            }
        } else {
            // Hard delete: eliminar directamente de Room
            localCategory.deleteCategoryById(categoryId)
        }
    }
    
    private fun syncWorker(calendarId: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<CategoryWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf(CategoryWorker.KEY_CALENDAR_ID to calendarId))
            .setBackoffCriteria(
                androidx.work.BackoffPolicy.EXPONENTIAL,
                10,
                TimeUnit.SECONDS
            )
            .build()

        workManager.enqueueUniqueWork(
            "sync_category_$calendarId",
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )
    }
}