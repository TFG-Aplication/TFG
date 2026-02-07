package com.asistente.core.domain.usecase.category

import com.asistente.core.domain.models.Task
import com.asistente.core.domain.ropositories.interfaz.TaskRepositoryInterface
import javax.inject.Inject

class GetExpecificTask @Inject constructor(
    private val repository: TaskRepositoryInterface,

    ){

    operator suspend fun invoke(taskId: String): Task? {
        return repository.getTaskById(taskId)


    }
}