package com.asistente.core.domain.usecase.task

import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.models.Task
import com.asistente.core.domain.ropositories.`interface`.TaskRepositoryInterface
import java.util.Date
import java.util.UUID

class CreateTask (
    private val repository: TaskRepositoryInterface
) {
    /**
     * Crea o actualiza una tarea.
     * Si no se pasa un ID, genera uno nuevo (Creación).
     */


    // crear restriccion de no dos task en el mismo horario en la misma fecha
    // crear restrciion de no id duplicado
    suspend operator fun invoke(
        id: String = UUID.randomUUID().toString(),
        name: String,
        owners: List<String> = listOf("local_user"),
        place: String,
        notes: String,
        init_date: Date,
        finich_date: Date,
        syncStatus: Int = 0,
        calendar: Calendar
        ) {

        val task = Task(
            id = id,
            name = name,
            owners = owners,
            place = place,
            notes = notes,
            init_date = init_date,
            finish_date = finich_date,
            parentCalendarId = calendar.id,
            syncStatus = syncStatus
            )

        repository.saveTask(task, calendar.isShared)
    }
}