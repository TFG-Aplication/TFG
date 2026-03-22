package com.asistente.core.domain.usecase.task

import com.asistente.core.domain.ropositories.interfaz.TaskRepositoryInterface
import com.asistente.core.domain.usecase.timeslot.DeleteTimeSlot
import javax.inject.Inject

class DeleteTask @Inject constructor(
    private val repository: TaskRepositoryInterface,
    private val deleteTimeSlot: DeleteTimeSlot
) {
    suspend operator fun invoke(taskId: String, isShared: Boolean) {
        repository.deleteTask(taskId, isShared)
        deleteTimeSlot(taskId, isShared)
    }
}