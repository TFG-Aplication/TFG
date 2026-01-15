package com.asistente.core.data.repository

import com.asistente.core.data.local.daos.CategoryDao
import com.asistente.core.data.remote.CategoryRemoteServices
import com.asistente.core.domain.models.Category
import com.asistente.core.domain.ropositories.`interface`.CategoryRepositoryInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CategoryRepository (
    private val localCategory: CategoryDao,

    private val remoteCategory: CategoryRemoteServices,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

) : CategoryRepositoryInterface {

    override suspend fun getCategoryById(id: String): Category? {
        var Category = localCategory.getCategoryById(id)
        if (Category == null)
            Category = remoteCategory.getCategoryByIdRemote(id)
        if (Category != null)
            localCategory.insertCategory(Category)

        return Category
    }

    override fun getAllCategoryByUserId(id: String): Flow<List<Category>> {
        externalScope.launch {
            try {
                val remoteCategorys = remoteCategory.getAllCategorysByUserIdRemote(id)
                val localCategorys = localCategory.getAllCategoryListByUserId(id)
                val remoteIds = remoteCategorys.map { it.id }
                localCategorys.forEach { local ->
                    if (!remoteIds.contains(local.id)) {
                        localCategory.deleteCategoryById(local.id)
                    }
                }
                remoteCategorys.forEach { remote ->
                    localCategory.insertCategory(remote)
                }

            } catch (e: Exception) {
            }
        }
        return localCategory.getAllCategorysByUserId(id)
    }

    override fun getAllCategoryByCalendarId(calendarId: String): Flow<List<Category>> {
        externalScope.launch {
            try {
                val remoteCategorys = remoteCategory.getAllCategorysByCalendarIdRemote(calendarId)

                val localCategorys = localCategory.getAllCategoryList(calendarId)

                val remoteIds = remoteCategorys.map { it.id }
                localCategorys.forEach { local ->
                    if (!remoteIds.contains(local.id)) {
                        localCategory.deleteCategoryById(local.id)
                    }
                }
                remoteCategorys.forEach { localCategory.insertCategory(it) }
            } catch (e: Exception) {
            }
        }
        return localCategory.getAllCategorysByCalendarId(calendarId)
    }

    override suspend fun saveCategory(Category: Category, isSharedCalendar: Boolean) {
        if (isSharedCalendar) {
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