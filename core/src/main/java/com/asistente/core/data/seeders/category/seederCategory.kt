package com.asistente.core.data.seeders.category

import com.asistente.core.domain.ropositories.interfaz.CategoryRepositoryInterface
import javax.inject.Inject

class seederCategory @Inject constructor(
    private val categoryRepository: CategoryRepositoryInterface
) {

    suspend fun seedDefaultCategories(calendarId: String) {

        val predefinedCategories = MockDataCategory.getPredefinedCategories(calendarId)

        predefinedCategories.forEach { category ->
            categoryRepository.saveCategory(category, false)
        }

    }
}