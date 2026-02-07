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
        externalScope.launch {
            try {
                val remote = remoteCategory.getCategoryByIdRemote(id)
                if (remote != null) {
                    localCategory.insertCategory(remote.copy(syncStatus = 1))
                }
            } catch (e: Exception) {
            }
        }
        return localCategory.getCategoryById(id)
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

    override suspend fun saveCategory(Category: Category, isSharedCalendar: Boolean) {
        // Guarda en Room -> Sube a FireBase
        if (isSharedCalendar) {
            // Si caleario es compartido y solo si hay internet
            val success = remoteCategory.saveCategoryRemote(Category)
            if (success) {
                localCategory.insertCategory(Category)
            } else {
                throw Exception("Conexión necesaria para modificar calendarios compartidos")
            }
        } else {
            // si calendario es normal
            localCategory.insertCategory(Category)
            externalScope.launch {
                remoteCategory.saveCategoryRemote(Category)
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