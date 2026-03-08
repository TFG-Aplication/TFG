package com.asistente.core.domain.models

import androidx.room.Entity
import java.time.LocalTime
import java.util.Date

@Entity(tableName = "time_slots")
data class TimeSlot(
    val id: String,
    val name: String,
    val startTime: LocalTime,       // 08:00
    val endTime: LocalTime,         // 14:00
    val daysOfWeek: List<Int>,      // [1,2,3,4,5] — 1=lunes
    val type: SlotType,             // BLOCKED / PREFERRED / AVAILABLE
    val recurrence: RecurrenceType, // WEEKLY / EVEN_WEEKS / ODD_WEEKS / DATE_RANGE / SINGLE_DAY
    val rangeStart: Date? = null,   // solo si RANGE o SINGLE_DAY
    val rangeEnd: Date? = null,
    val isActive: Boolean = true
)