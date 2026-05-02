package com.asistente.core.domain.usecase.task

import com.asistente.core.domain.models.RecurrenceType
import com.asistente.core.domain.models.SlotType
import com.asistente.core.domain.models.TimeSlot
import java.util.Calendar
import java.util.Date


fun buildTimeSlotForTask(
    name: String,
    calendarId: String,
    owners: List<String>,
    taskId: String,
    initDate: Date,
    finishDate: Date
): TimeSlot {
    val calInit = Calendar.getInstance().apply { time = initDate }
    val calFin  = Calendar.getInstance().apply { time = finishDate }

    val isSameDay = calInit.get(Calendar.YEAR) == calFin.get(Calendar.YEAR) &&
            calInit.get(Calendar.DAY_OF_YEAR) == calFin.get(Calendar.DAY_OF_YEAR)

    return if (isSameDay) {
        TimeSlot(
            name             = name.trim(),
            parentCalendarId = calendarId,
            owners           = owners,
            slotType         = SlotType.TASK_BLOCKED,
            taskId           = taskId,
            recurrenceType   = RecurrenceType.SINGLE_DAY,
            rangeStart       = initDate,
            rangeEnd         = finishDate,
            startMinuteOfDay = calInit.get(Calendar.HOUR_OF_DAY) * 60 + calInit.get(Calendar.MINUTE),
            endMinuteOfDay   = calFin.get(Calendar.HOUR_OF_DAY) * 60 + calFin.get(Calendar.MINUTE),
            enable           = true
        )
    } else {
        // TASK_RANGE: startMinuteOfDay = inicio del primer día, endMinuteOfDay = fin del último día
        // Los días intermedios se infieren como completos (0 → 1440)
        TimeSlot(
            name             = name.trim(),
            parentCalendarId = calendarId,
            owners           = owners,
            slotType         = SlotType.TASK_BLOCKED,
            taskId           = taskId,
            recurrenceType   = RecurrenceType.TASK_RANGE,
            rangeStart       = initDate,   // fecha+hora exacta de inicio
            rangeEnd         = finishDate, // fecha+hora exacta de fin
            startMinuteOfDay = calInit.get(Calendar.HOUR_OF_DAY) * 60 + calInit.get(Calendar.MINUTE),
            endMinuteOfDay   = calFin.get(Calendar.HOUR_OF_DAY) * 60 + calFin.get(Calendar.MINUTE),
            enable           = true
        )
    }
}