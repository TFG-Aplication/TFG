package com.asistente.core.domain.ropositories.`interface`

import com.asistente.core.domain.models.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepositoryInterface {

    suspend fun getCategoryById(id: String): Category?
    fun getAllCategoryByUserId(id: String): Flow<List<Category>>?
    fun getAllCategoryByCalendarId(email: String): Flow<List<Category>>?

    suspend fun saveCategory(Category: Category, isSharedCalendar: Boolean)

    suspend fun deleteCategory(CategoryId: String, isShared: Boolean)
}