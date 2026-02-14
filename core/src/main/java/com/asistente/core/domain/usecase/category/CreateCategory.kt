package com.asistente.core.domain.usecase.task

import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.models.Category
import com.asistente.core.domain.ropositories.interfaz.CategoryRepositoryInterface
import java.util.UUID
import javax.inject.Inject

class CreateCategory @Inject constructor(
    private val repository: CategoryRepositoryInterface
) {
    /**
     * Crea o actualiza una categoria.
     * Si no se pasa un ID, genera uno nuevo (Creación).
     */

    suspend operator fun invoke(
        id: String = UUID.randomUUID().toString(),
        name: String,
        syncStatus: Int = 0,
        calendar: Calendar,
        color: String
    ) {

        val category = Category(
            id = id,
            name = name,
            parentCalendarId = calendar.id,
            syncStatus = 0,
            color = color
        )

        repository.saveCategory(category, calendar.isShared)
    }
}