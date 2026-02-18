package com.asistente.core.domain.usecase.category

import com.asistente.core.domain.models.Category
import com.asistente.core.domain.ropositories.interfaz.CategoryRepositoryInterface
import javax.inject.Inject


class UpdateCategory @Inject constructor(
    private val repository: CategoryRepositoryInterface
) {
    suspend operator fun invoke(
        categoryId: String,
        newName: String? = null,
        newColor: String? = null
    ): Result<Category> {
        return try {
            // Obtener categoría actual
            val currentCategory = repository.getCategoryById(categoryId)
                ?: return Result.failure(IllegalArgumentException("Category not found"))

            // Validar cambios
            if (newName != null && newName.isBlank()) {
                return Result.failure(IllegalArgumentException("Category name cannot be empty"))
            }
            if(newName?.length?:0 < 3 ) {
                return Result.failure(IllegalArgumentException("Category name must be at least 3 characters long"))
            }
            if(newName?.length?:0 > 10) {
                return Result.failure(IllegalArgumentException("Category name must be less than 20 characters long"))
            }
            if (newColor != null && !isValidColor(newColor)) {
                return Result.failure(IllegalArgumentException("Invalid color format. Use #RRGGBB"))
            }

            // Aplicar cambios
            val updatedCategory = currentCategory.copy(
                name = newName?.trim() ?: currentCategory.name,
                color = newColor?.uppercase() ?: currentCategory.color,
                syncStatus = 0 // Marcar como pendiente
            )

            repository.updateCategory(updatedCategory)

            Result.success(updatedCategory)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isValidColor(color: String): Boolean {
        return color.matches(Regex("^#[0-9A-Fa-f]{6}$"))
    }
}