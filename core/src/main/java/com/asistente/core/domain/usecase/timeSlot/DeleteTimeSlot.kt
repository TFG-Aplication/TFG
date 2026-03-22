package com.asistente.core.domain.usecase.timeslot

import com.asistente.core.domain.ropositories.interfaz.TimeSlotRepositoryInterface
import javax.inject.Inject

class DeleteTimeSlot @Inject constructor(
    private val repository: TimeSlotRepositoryInterface,
) {
    suspend operator fun invoke(taskId: String, isShared: Boolean) {
        repository.deleteTimeSlotByTaskId(taskId, isShared)
    }
}