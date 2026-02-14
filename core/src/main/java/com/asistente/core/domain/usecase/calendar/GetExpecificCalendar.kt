package com.asistente.core.domain.usecase.calendar

import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.ropositories.interfaz.CalendarRepositoryInterface
import javax.inject.Inject

class GetExpecificCalendar @Inject constructor(
    private val repository: CalendarRepositoryInterface,

    ){

    operator suspend fun invoke(id: String): Calendar? {
        return repository.getCalendarById(id)



    }
}