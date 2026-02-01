package com.asistente.core.domain.usecase.calendar

import com.asistente.core.data.seeders.category.seederCategory
import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.ropositories.interfaz.CalendarRepositoryInterface
import java.util.UUID
import javax.inject.Inject

// Indica a Hilt que debe suministrar automáticamente las dependencias
// del constructor, evitando la creación manual de la clase.
class CreateCalendar @Inject constructor(
    private val repository: CalendarRepositoryInterface,
    private val categorySeeder: seederCategory
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
    ): Calendar {
        val calendar = Calendar(
            id = id,
            name = name,
            owners = owners,
            isShared = isShared,
            syncStatus = syncStatus
        )


        repository.saveCalendar(calendar)


        categorySeeder.seedDefaultCategories(
            calendarId = calendar.id
        )

        return calendar
    }

}