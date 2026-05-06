package com.asistente.core.domain.usecase.task

import com.asistente.core.domain.ropositories.interfaz.TaskRepositoryInterface
import com.asistente.core.domain.ropositories.interfaz.TimeSlotRepositoryInterface
import com.asistente.core.domain.usecase.timeslot.DeleteTimeSlot
import javax.inject.Inject

class DeleteTask @Inject constructor(
    private val repository: TaskRepositoryInterface,
    private val repositoryTimeSlot: TimeSlotRepositoryInterface,
) {
    // Devuelve el nombre de la franja eliminada, o null si no había
    suspend operator fun invoke(taskId: String, isShared: Boolean): String? {
        val timeSlot = repositoryTimeSlot.getTimeSlotByTaskId(taskId)
        repository.deleteTask(taskId, isShared)
        return if (timeSlot != null) {
            repositoryTimeSlot.deleteTimeSlotByTaskId(taskId, isShared)
            timeSlot.name
        } else null
    }
}