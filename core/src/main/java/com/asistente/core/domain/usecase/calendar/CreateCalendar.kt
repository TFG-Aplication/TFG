package com.asistente.core.domain.usecase.calendar

import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.ropositories.`interface`.CalendarRepositoryInterface
import java.util.UUID

class CreateCalendar(
    private val repository: CalendarRepositoryInterface
) {
    /**
     * Crea o actualiza un calendario.
     * Si no se pasa un ID, genera uno nuevo (Creación).
     */
    suspend operator fun invoke(
        name: String,
        id: String = UUID.randomUUID().toString(),
        owners: List<String> = listOf("local_user"),
        isShared: Boolean = false,
        syncStatus: Int = 0
    ) {
        val calendar = Calendar(
            id = id,
            name = name,
            owners = owners,
            isShared = isShared,
            syncStatus = syncStatus
        )

        repository.saveCalendar(calendar)
    }
}