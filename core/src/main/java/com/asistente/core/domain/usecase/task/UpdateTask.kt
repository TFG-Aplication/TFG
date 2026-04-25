package com.asistente.core.domain.usecase.task

import com.asistente.core.domain.models.RecurrenceType
import com.asistente.core.domain.models.SlotType
import com.asistente.core.domain.models.Task
import com.asistente.core.domain.models.TimeSlot
import com.asistente.core.domain.ropositories.interfaz.TaskRepositoryInterface
import com.asistente.core.domain.ropositories.interfaz.TimeSlotRepositoryInterface
import com.asistente.core.domain.usecase.alerts.Alerts
import com.asistente.core.domain.usecase.timeslot.CreateTimeSlot
import com.asistente.core.domain.usecase.timeslot.TimeSlotOverlapChecker
import kotlinx.coroutines.flow.first
import java.util.Date
import javax.inject.Inject

class UpdateTask @Inject constructor(
    private val repository: TaskRepositoryInterface,
    private val repositoryTimeSlot: TimeSlotRepositoryInterface,
    private val scheduleTaskAlerts: Alerts,
    private val createTimeSlot: CreateTimeSlot,
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

            if(blockTimeSlot){
                // busca franja
                val timeSlot = repositoryTimeSlot.getTimeSlotByTaskId(id)
                if (timeSlot == null) {
                    val calInit = java.util.Calendar.getInstance().apply { time = initDate }
                    val calFin  = java.util.Calendar.getInstance().apply { time = finishDate }
                    val startMinute = calInit.get(java.util.Calendar.HOUR_OF_DAY) * 60 + calInit.get(java.util.Calendar.MINUTE)
                    val endMinute   = calFin.get(java.util.Calendar.HOUR_OF_DAY) * 60 + calFin.get(java.util.Calendar.MINUTE)

                    val timeSlotNew = TimeSlot(
                        name = name.trim(),
                        parentCalendarId = calendarId,
                        owners = owners,
                        slotType = SlotType.TASK_BLOCKED,
                        taskId = id,
                        recurrenceType = RecurrenceType.SINGLE_DAY,
                        rangeStart = initDate,
                        rangeEnd = finishDate,
                        startMinuteOfDay = startMinute,
                        endMinuteOfDay = endMinute,
                        isActive = true
                    )

                    val listTimeSlots = repositoryTimeSlot.getAllTimeSlotsByCalendarId(calendarId)

                    val overlapping = TimeSlotOverlapChecker.findOverlaps(
                        candidate = timeSlotNew,
                        existingSlots = listTimeSlots.first()
                    )

                    val conflicting = overlapping.filter { it.slotType == SlotType.TASK_BLOCKED }
                    if (conflicting.isNotEmpty()) {
                        return Result.failure(IllegalArgumentException("No se puede crear la franja: se solapa con otras tarea bloqueante, desactiva el bloqueo"))
                    }
                    else {
                        repository.updateTask(task)
                        scheduleTaskAlerts(task)
                        createTimeSlot(timeSlotNew, isSharedCalendar)

                    }

                }
                else{
                    val calInit = java.util.Calendar.getInstance().apply { time = initDate }
                    val calFin  = java.util.Calendar.getInstance().apply { time = finishDate }
                    val startMinute = calInit.get(java.util.Calendar.HOUR_OF_DAY) * 60 + calInit.get(java.util.Calendar.MINUTE)
                    val endMinute   = calFin.get(java.util.Calendar.HOUR_OF_DAY) * 60 + calFin.get(java.util.Calendar.MINUTE)

                    val timeSlotUpdated = timeSlot.copy(
                            name = name.trim(),
                            parentCalendarId = calendarId,
                            owners = owners,
                            slotType = SlotType.TASK_BLOCKED,
                            taskId = id,
                            recurrenceType = RecurrenceType.SINGLE_DAY,
                            rangeStart = initDate,
                            rangeEnd = finishDate,
                            startMinuteOfDay = startMinute,
                            endMinuteOfDay = endMinute,
                            isActive = true
                        )
                        repositoryTimeSlot.updateTimeSlot(timeSlotUpdated)
                        repository.updateTask(task)
                        scheduleTaskAlerts(task)

                }
            }
            else {
                val timeSlot = repositoryTimeSlot.getTimeSlotByTaskId(id)
                if (timeSlot != null) {
                    repositoryTimeSlot.deleteTimeSlotByTaskId(id, isSharedCalendar)
                }
                scheduleTaskAlerts(task)
                repository.updateTask(task)

            }
            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}