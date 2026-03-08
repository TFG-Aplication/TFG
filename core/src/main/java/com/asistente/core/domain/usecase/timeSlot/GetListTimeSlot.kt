package com.asistente.core.domain.usecase.timeslot

import com.asistente.core.domain.models.TimeSlot
import com.asistente.core.domain.ropositories.interfaz.TimeSlotRepositoryInterface
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetListTimeSlot @Inject constructor(
    private val repository: TimeSlotRepositoryInterface
) {
    operator fun invoke(calendarId: String): Flow<List<TimeSlot>> =
        repository.getAllTimeSlotsByCalendarId(calendarId)
}