package com.asistente.core.domain.usecase.calendar

import com.asistente.core.data.seeders.category.seederCategory
import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.ropositories.interfaz.CalendarRepositoryInterface
import java.util.UUID
import javax.inject.Inject

class CreateCalendar @Inject constructor(
    private val repository: CalendarRepositoryInterface,
    private val categorySeeder: seederCategory
) {
    suspend operator fun invoke(
        name: String,
        code: String = generateCode(name),
        owners: List<String>,
        isShared: Boolean = false
    ): Result<Calendar> {
        return try {
            // Validaciones
            if (name.isBlank()) {
                return Result.failure(IllegalArgumentException("Calendar name cannot be empty"))
            }
            if(name.length < 3) {
                return Result.failure(IllegalArgumentException("Calendar name must be at least 3 characters long"))
            }
            if(name.length > 20) {
                return Result.failure(IllegalArgumentException("Calendar name must be less than 20 characters long"))
            }
            if (owners.isEmpty()) {
                return Result.failure(IllegalArgumentException("Calendar must have at least one owner"))
            }

            val calendar = Calendar(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                code = code,
                owners = owners,
                isShared = isShared,
                syncStatus = 0 // Pendiente de sincronización
            )

            repository.saveCalendar(calendar)

            // Sembrar categorías por defecto
            categorySeeder.seedDefaultCategories(calendarId = calendar.id)

            Result.success(calendar)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateCode(name: String): String {
        return name.take(4).uppercase() + UUID.randomUUID().toString().take(4)
    }
}