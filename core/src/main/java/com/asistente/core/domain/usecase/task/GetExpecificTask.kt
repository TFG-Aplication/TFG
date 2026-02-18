package com.asistente.core.domain.usecase.task

import com.asistente.core.domain.models.Task
import com.asistente.core.domain.ropositories.interfaz.TaskRepositoryInterface
import javax.inject.Inject


class GetSpecificTask @Inject constructor(
    private val repository: TaskRepositoryInterface
) {
    suspend operator fun invoke(taskId: String): Task? {
        require(taskId.isNotBlank()) { "Task ID cannot be empty" }
        return repository.getTaskById(taskId)
    }
}