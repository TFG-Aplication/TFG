package com.asistente.core.domain.usecase.task

import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.models.Category
import com.asistente.core.domain.models.Task
import com.asistente.core.domain.ropositories.interfaz.TaskRepositoryInterface
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class CreateTask @Inject constructor(
    private val repository: TaskRepositoryInterface
) {
    /**
     * Crea o actualiza una tarea.
     * Si no se pasa un ID, genera uno nuevo (Creación).
     */

    suspend operator fun invoke(
        id: String = UUID.randomUUID().toString(),
        name: String,
        owners: List<String> = listOf("local_user"),
        place: String?,
        notes: String?,
        init_date: Date,
        finich_date: Date,
        syncStatus: Int = 0,
        calendar: Calendar,
        category: Category?,
        alerts: List<Long>?
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
            syncStatus = syncStatus,
            categoryId = if(category != null) category.id else null,
            alerts = alerts
            )

        repository.saveTask(task, calendar.isShared)
    }
}