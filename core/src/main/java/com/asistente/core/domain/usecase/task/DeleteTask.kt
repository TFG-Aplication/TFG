package com.asistente.core.domain.usecase.task

import com.asistente.core.domain.ropositories.interfaz.TaskRepositoryInterface
import javax.inject.Inject

class DeleteTask @Inject constructor(
    private val repository: TaskRepositoryInterface,
) {
        suspend operator fun invoke(taskId: String, isShared: Boolean): Unit {
            return repository.deleteTask(taskId, isShared)
        }

    }