package com.asistente.core.domain.usecase.task

import com.asistente.core.domain.models.Task
import com.asistente.core.domain.ropositories.interfaz.TaskRepositoryInterface
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Obtiene una lista de todos las tareas de un calendario

 */
class GetListTask @Inject constructor(
    private val repository: TaskRepositoryInterface,
){
     operator fun invoke(calendarId: String): Flow<List<Task>>? {
         return repository.getAllTaskByCalendarId(calendarId)
     }

}