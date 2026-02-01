package com.asistente.core.data.repository

import com.asistente.core.data.local.daos.CategoryDao
import com.asistente.core.data.remote.CategoryRemoteServices
import com.asistente.core.domain.models.Category
import com.asistente.core.domain.ropositories.interfaz.CategoryRepositoryInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val localCategory: CategoryDao,
    private val remoteCategory: CategoryRemoteServices,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

) : CategoryRepositoryInterface {

    override suspend fun getCategoryById(id: String): Category? {
        var Category = localCategory.getCategoryById(id)
        if (Category == null)
            Category = remoteCategory.getCategoryByIdRemote(id)
            if (Category != null) {
                localCategory.insertCategory(Category)
            }

        return Category
    }

    override fun getAllCategoryByCalendarId(calendarId: String): Flow<List<Category>> {
        externalScope.launch {
            try {
                val remoteCategorys = remoteCategory.getAllCategorysByCalendarIdRemote(calendarId)

                val localCategorys = localCategory.getAllCategoryList(calendarId)

                val remoteIds = remoteCategorys.map { it.id }
                localCategorys.forEach { local ->
                    if (local.syncStatus == 1 && !remoteIds.contains(local.id)) {
                        localCategory.deleteCategoryById(local.id)
                    }
                    if (local.syncStatus == 0 && !remoteIds.contains(local.id)) {
                        remoteCategory.saveCategoryRemote(local)
                        local.syncStatus = 1
                    }
                }
                remoteCategorys.forEach { remote ->
                    localCategory.insertCategory(remote.copy(syncStatus = 1))
                }
            } catch (e: Exception) {
            }
        }
        return localCategory.getAllCategorysByCalendarId(calendarId)
    }

    override suspend fun saveCategory(Category: Category) {
        // Guarda en Room -> Sube a FireBase
        localCategory.insertCategory(Category)
        externalScope.launch {
            val success = remoteCategory.saveCategoryRemote(Category)
            if (success) {
                // SOLO si Firebase confirma, actualizamos local a status 1
                localCategory.insertCategory(Category.copy(syncStatus = 1))

            }

        }
    }

    override suspend fun deleteCategory(CategoryId: String, isShared: Boolean) {
        if (isShared) {
            val success = remoteCategory.deleteCategoryRemote(CategoryId)
            if (success) localCategory.deleteCategoryById(CategoryId)
            else throw Exception("No se pudo borrar: verifica tu conexión")
        } else {
            localCategory.deleteCategoryById(CategoryId)
            externalScope.launch { remoteCategory.deleteCategoryRemote(CategoryId) }
        }
    }
}