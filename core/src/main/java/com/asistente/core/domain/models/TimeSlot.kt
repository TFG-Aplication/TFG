package com.asistente.core.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import java.util.Date
import java.util.UUID
import javax.annotation.Nonnull

@Entity(
    tableName = "time_slots",
    indices = [Index(value = ["parentCalendarId"])]
)
data class TimeSlot(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @Nonnull
    val name: String = "",

    @Nonnull
    val parentCalendarId: String = "",

    val owners: List<String> = emptyList(),
    var syncStatus: Int = 0,
    val firebaseId: String? = null,

    //  Horario dentro del día 
    val startMinuteOfDay: Int = 480,    // 08:00 por defecto
    val endMinuteOfDay: Int = 840,      // 14:00 por defecto

    //  Días de la semana activos 
    // Lista de ints: 1=Lunes … 7=Domingo
    val daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5),

    //  Tipo de recurrencia 
    val recurrenceType: RecurrenceType = RecurrenceType.WEEKLY,

    // Solo para DATE_RANGE y SINGLE_DAY:
    val rangeStart: Date? = null,
    val rangeEnd: Date? = null,

    //  Tipo de franja 
    val slotType: SlotType = SlotType.BLOCKED,

    //  Estado 
    val isActive: Boolean = true
)