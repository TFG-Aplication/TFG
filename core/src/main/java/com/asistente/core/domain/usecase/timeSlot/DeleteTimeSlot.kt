package com.asistente.core.domain.usecase.timeslot

import com.asistente.core.domain.models.SlotType
import com.asistente.core.domain.ropositories.interfaz.TaskRepositoryInterface
import com.asistente.core.domain.ropositories.interfaz.TimeSlotRepositoryInterface
import javax.inject.Inject

class DeleteTimeSlot @Inject constructor(
    private val repository: TimeSlotRepositoryInterface,
    private val taskRepository: TaskRepositoryInterface
) {
    suspend operator fun invoke(timeSlotId: String, isShared: Boolean) {
        val timeSlot = repository.getTimeSlotById(timeSlotId) ?: return

        when (timeSlot.slotType) {

            // TASK_BLOCKED: no se borra la franja ni la tarea,
            // solo se desactiva el flag blockTimeSlot de la tarea asociada
            SlotType.TASK_BLOCKED -> {
                requireNotNull(timeSlot.taskId) {
                    "La franja TASK_BLOCKED no tiene tarea asociada"
                }
                val task = taskRepository.getTaskById(timeSlot.taskId) ?: return
                taskRepository.updateTask(task.copy(blockTimeSlot = false))
                repository.deleteTimeSlot(timeSlotId, isShared)
            }

            // BLOCKED: borrado directo
            SlotType.BLOCKED -> {
                repository.deleteTimeSlot(timeSlotId, isShared)
            }
        }
    }
}