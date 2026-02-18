package com.asistente.core.domain.usecase.task

import com.asistente.core.domain.models.Task
import com.asistente.core.domain.ropositories.interfaz.TaskRepositoryInterface
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GetListTask @Inject constructor(
    private val repository: TaskRepositoryInterface
) {
    operator fun invoke(calendarId: String): Flow<List<Task>>? {
        require(calendarId.isNotBlank()) { "Calendar ID cannot be empty" }
        return repository.getAllTaskByCalendarId(calendarId)
    }
}