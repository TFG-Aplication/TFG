package com.asistente.core.domain.usecase.task

import com.asistente.core.domain.models.RecurrenceType
import com.asistente.core.domain.models.SlotType
import com.asistente.core.domain.models.Task
import com.asistente.core.domain.models.TimeSlot
import com.asistente.core.domain.ropositories.interfaz.TaskRepositoryInterface
import com.asistente.core.domain.ropositories.interfaz.TimeSlotRepositoryInterface
import com.asistente.core.domain.usecase.alerts.Alerts
import com.asistente.core.domain.usecase.timeslot.TimeSlotOverlapChecker
import com.asistente.core.domain.usecase.timeslot.CreateTimeSlot
import kotlinx.coroutines.flow.first
import java.util.Date
import java.util.UUID
import javax.inject.Inject


class CreateTask @Inject constructor(
    private val repository: TaskRepositoryInterface,
    private val repositoryTimeSlot: TimeSlotRepositoryInterface,
    private val scheduleTaskAlerts: Alerts,
    private val createTimeSlot: CreateTimeSlot

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
        alerts: List<Long>? = null,
        isSharedCalendar: Boolean,
        blockTimeSlot: Boolean = false

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

            val taskId = UUID.randomUUID().toString()


            val task = Task(
                id = taskId,
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

            // ── Crear franja bloqueada si se solicitó ─────────────────────────
            if (blockTimeSlot) {
                val timeSlot = buildTimeSlotForTask(
                    name, calendarId, owners, taskId, initDate, finishDate
                )

                val listTimeSlots = repositoryTimeSlot.getAllTimeSlotsByCalendarId(calendarId)

                val overlapping = TimeSlotOverlapChecker.findOverlaps(
                    candidate = timeSlot,
                    existingSlots = listTimeSlots.first()
                )

                val conflicting = overlapping.filter { it.slotType == SlotType.TASK_BLOCKED }
                if (conflicting.isNotEmpty()) {
                    return Result.failure(IllegalArgumentException("No se puede crear la franja: se solapa con otras tarea bloqueante, desactiva el bloqueo"))
                }
                else {
                    repository.saveTask(task, isSharedCalendar)
                    scheduleTaskAlerts(task)
                    val slotResult = createTimeSlot(timeSlot, isSharedCalendar)
                    if (slotResult.isFailure) return Result.failure(slotResult.exceptionOrNull()!!)                }
            }
            else {
                repository.saveTask(task, isSharedCalendar)
                scheduleTaskAlerts(task)
            }

            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}