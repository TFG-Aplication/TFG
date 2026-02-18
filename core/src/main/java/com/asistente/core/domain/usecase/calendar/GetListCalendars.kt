package com.asistente.core.domain.usecase.calendar

import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.ropositories.interfaz.CalendarRepositoryInterface
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GetListCalendars @Inject constructor(
    private val repository: CalendarRepositoryInterface
) {
    operator fun invoke(userId: String): Flow<List<Calendar>>? {
        require(userId.isNotBlank()) { "User ID cannot be empty" }
        return repository.getAllCalendarByUserId(userId)
    }
}