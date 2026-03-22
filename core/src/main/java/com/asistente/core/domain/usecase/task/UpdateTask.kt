package com.asistente.core.domain.usecase.task

import com.asistente.core.domain.models.RecurrenceType
import com.asistente.core.domain.models.SlotType
import com.asistente.core.domain.models.Task
import com.asistente.core.domain.models.TimeSlot
import com.asistente.core.domain.ropositories.interfaz.TaskRepositoryInterface
import com.asistente.core.domain.usecase.alerts.Alerts
import com.asistente.core.domain.usecase.timeslot.DeleteTimeSlot  // era "timeSlot"
import com.asistente.core.domain.usecase.timeslot.CreateTimeSlot  // ya estaba bien
import java.util.Date
import javax.inject.Inject

class UpdateTask @Inject constructor(
    private val repository: TaskRepositoryInterface,
    private val scheduleTaskAlerts: Alerts,
    private val createTimeSlot: CreateTimeSlot,
    private val deleteTimeSlot: DeleteTimeSlot
) {
    suspend operator fun invoke(
        id: String,
        name: String,
        calendarId: String,
        owners: List<String>,
        initDate: Date,
        finishDate: Date,
        categoryId: String? = null,
        place: String? = null,
        notes: String? = null,
        alerts: List<Long>? = null,
        isSharedCalendar: Boolean,
        blockTimeSlot: Boolean = false,
        previouslyBlockedTimeSlot: Boolean = false
    ): Result<Task> {
        return try {
            // Validaciones
            if (id.isBlank()) {
                return Result.failure(IllegalArgumentException("Task ID cannot be empty"))
            }
            if (name.isBlank()) {
                return Result.failure(IllegalArgumentException("Task name cannot be empty"))
            }
            if (name.length < 3) {
                return Result.failure(IllegalArgumentException("Task name must be at least 3 characters long"))
            }
            if (name.length > 30) {
                return Result.failure(IllegalArgumentException("Task name must be less than 30 characters long"))
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
                id = id,
                name = name.trim(),
                owners = owners,
                parentCalendarId = calendarId,
                categoryId = categoryId,
                place = place?.trim(),
                notes = notes?.trim(),
                init_date = initDate,
                finish_date = finishDate,
                alerts = alerts,
                syncStatus = 0,
                blockTimeSlot = blockTimeSlot
            )

            repository.updateTask(task)
            scheduleTaskAlerts(task)

            // ── Gestión de franja bloqueada ───────────────────────────────────
            when {
                // Se activó blockTimeSlot: crear franja nueva
                blockTimeSlot && !previouslyBlockedTimeSlot -> {
                    val timeSlot = TimeSlot(
                        name = name.trim(),
                        parentCalendarId = calendarId,
                        owners = owners,
                        slotType = SlotType.TASK_BLOCKED,
                        taskId = id,
                        recurrenceType = RecurrenceType.SINGLE_DAY,
                        rangeStart = initDate,
                        rangeEnd = finishDate,
                        isActive = true
                    )
                    createTimeSlot(timeSlot, isSharedCalendar)
                }
                // Se desactivó blockTimeSlot: eliminar franja existente
                !blockTimeSlot && previouslyBlockedTimeSlot -> {
                    deleteTimeSlot(id, isSharedCalendar)
                }
                // Sigue activo: actualizar franja existente con nuevos datos
                blockTimeSlot && previouslyBlockedTimeSlot -> {
                    deleteTimeSlot(id, isSharedCalendar)
                    val timeSlot = TimeSlot(
                        name = name.trim(),
                        parentCalendarId = calendarId,
                        owners = owners,
                        slotType = SlotType.TASK_BLOCKED,
                        taskId = id,
                        recurrenceType = RecurrenceType.SINGLE_DAY,
                        rangeStart = initDate,
                        rangeEnd = finishDate,
                        isActive = true
                    )
                    createTimeSlot(timeSlot, isSharedCalendar)
                }
                // Sigue desactivado: no hacer nada
            }

            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}