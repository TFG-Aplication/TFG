package com.asistente.core.domain.usecase.timeslot

import com.asistente.core.domain.ropositories.interfaz.TimeSlotRepositoryInterface
import javax.inject.Inject

class GetTimeSlotByTaskId @Inject constructor(
    private val repository: TimeSlotRepositoryInterface
) {
    suspend operator fun invoke(taskId: String): String? =
        repository.getTimeSlotByTaskId(taskId)?.name
}