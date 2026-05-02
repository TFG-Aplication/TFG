package com.asistente.core.domain.usecase.task

import com.asistente.core.domain.models.RecurrenceType
import com.asistente.core.domain.models.SlotType
import com.asistente.core.domain.models.TimeSlot
import java.util.Date


fun buildTimeSlotForTask(
    name: String,
    calendarId: String,
    owners: List<String>,
    taskId: String,
    initDate: Date,
    finishDate: Date
): TimeSlot {
    val calInit = java.util.Calendar.getInstance().apply { time = initDate }
    val calFin  = java.util.Calendar.getInstance().apply { time = finishDate }

    val startMinute = calInit.get(java.util.Calendar.HOUR_OF_DAY) * 60 +
            calInit.get(java.util.Calendar.MINUTE)

    val isSameDay = calInit.get(java.util.Calendar.YEAR)  == calFin.get(java.util.Calendar.YEAR) &&
            calInit.get(java.util.Calendar.DAY_OF_YEAR) == calFin.get(java.util.Calendar.DAY_OF_YEAR)

    val recurrence = if (isSameDay) RecurrenceType.SINGLE_DAY else RecurrenceType.DATE_RANGE

    val endMinute = if (isSameDay) {
        calFin.get(java.util.Calendar.HOUR_OF_DAY) * 60 + calFin.get(java.util.Calendar.MINUTE)
    } else {
        // Franja de inicio → medianoche cada día del rango
        // (puedes cambiar esto a 1440 si quieres "todo el día")
        calFin.get(java.util.Calendar.HOUR_OF_DAY) * 60 + calFin.get(java.util.Calendar.MINUTE)
    }

    return TimeSlot(
        name             = name.trim(),
        parentCalendarId = calendarId,
        owners           = owners,
        slotType         = SlotType.TASK_BLOCKED,
        taskId           = taskId,
        recurrenceType   = recurrence,
        rangeStart       = initDate,
        rangeEnd         = finishDate,
        startMinuteOfDay = startMinute,
        endMinuteOfDay   = endMinute,
        enable           = true
    )
}