package com.asistente.core.domain.usecase.calendar

import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.ropositories.interfaz.CalendarRepositoryInterface
import javax.inject.Inject


class GetSpecificCalendar @Inject constructor(
    private val repository: CalendarRepositoryInterface
) {
    suspend operator fun invoke(id: String): Calendar? {
        require(id.isNotBlank()) { "Calendar ID cannot be empty" }
        return repository.getCalendarById(id)
    }
}