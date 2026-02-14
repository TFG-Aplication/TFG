package com.asistente.core.domain.usecase.category

import com.asistente.core.domain.ropositories.interfaz.CategoryRepositoryInterface
import javax.inject.Inject

class DeleteCategory @Inject constructor(
    private val repository: CategoryRepositoryInterface,
) {
        suspend operator fun invoke(calendarId: String, isShared: Boolean): Unit {
            return repository.deleteCategory(calendarId, isShared)
        }

    }