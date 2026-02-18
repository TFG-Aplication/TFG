package com.asistente.core.domain.usecase.category

import com.asistente.core.domain.models.Category
import com.asistente.core.domain.ropositories.interfaz.CategoryRepositoryInterface
import javax.inject.Inject


class GetSpecificCategory @Inject constructor(
    private val repository: CategoryRepositoryInterface
) {
    suspend operator fun invoke(categoryId: String): Category? {
        return repository.getCategoryById(categoryId)
    }
}