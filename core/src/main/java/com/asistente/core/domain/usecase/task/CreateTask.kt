package com.asistente.core.domain.usecase.task

import com.asistente.core.domain.models.Task
import com.asistente.core.domain.ropositories.interfaz.TaskRepositoryInterface
import com.asistente.core.domain.usecase.alerts.Alerts
import java.util.Date
import java.util.UUID
import javax.inject.Inject


class CreateTask @Inject constructor(
    private val repository: TaskRepositoryInterface,
    private val scheduleTaskAlerts: Alerts
) {
    suspend operator fun invoke(
        name: String,
        calendarId: String,
        owners: List<String>,
        initDate: Date,
        finishDate: Date,
        categoryId: String? = null,
        place: String? = null,
        notes: String? = null,
        alerts: List<Long>? = listOf(System.currentTimeMillis() + 30_000L), // alerta en 1 minuto,
        isSharedCalendar: Boolean
    ): Result<Task> {
        return try {
            // Validaciones
            if (name.isBlank()) {
                return Result.failure(IllegalArgumentException("Task name cannot be empty"))
            }
            if(name.length < 3) {
                return Result.failure(IllegalArgumentException("Task name must be at least 3 characters long"))
            }
            if(name.length > 30) {
                return Result.failure(IllegalArgumentException("Task name must be less than 20 characters long"))
            }
            if (calendarId.isBlank()) {
                return Result.failure(IllegalArgumentException("Calendar ID cannot be empty"))
            }
            if (owners.isEmpty()) {
                return Result.failure(IllegalArgumentException("Task must have at least one owner"))
            }
            if (finishDate.before(initDate)) {
                return Result.failure(IllegalArgumentException("Finish date cannot be before init date"))
            }

            val task = Task(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                owners = owners,
                parentCalendarId = calendarId,
                categoryId = categoryId,
                place = place?.trim(),
                notes = notes?.trim(),
                init_date = initDate,
                finish_date = finishDate,
                alerts = alerts,
                syncStatus = 0
            )

            repository.saveTask(task, isSharedCalendar)
            scheduleTaskAlerts(task)

            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}