package com.asistente.core.domain.usecase.timeslot

import com.asistente.core.domain.models.TimeSlot
import com.asistente.core.domain.ropositories.interfaz.TimeSlotRepositoryInterface
import javax.inject.Inject

class GetSpecificTimeSlot @Inject constructor(
    private val repository: TimeSlotRepositoryInterface
) {
    suspend operator fun invoke(timeSlotId: String): TimeSlot? {
        require(timeSlotId.isNotBlank()) { "Time Slot ID cannot be empty" }
        return repository.getTimeSlotById(timeSlotId)
    }
}