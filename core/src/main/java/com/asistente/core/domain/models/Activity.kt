package com.asistente.core.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import java.util.Date
import java.util.UUID
import javax.annotation.Nonnull

@Entity(
    tableName = "activities",
    indices = [
        Index(value = ["parentCalendarId"]),
        Index(value = ["categoryId"])
    ]
)
data class Activity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val owners: List<String> = emptyList(),
    var syncStatus: Int = 0,
    val categoryId: String? = null,
    val place: String? = null,
    val notes: String? = null,
    @Nonnull
    val name: String = "",
    @Nonnull
    val parentCalendarId: String = "",
    val alerts: List<Long>? = emptyList(),
    val firebaseId: String? = null,

    // ── Campos específicos de Activity ──
    val durationMinutes: Long = 60,          // duración en minutos
    val earliest_start: Date? = null,        // desde cuándo se puede planificar
    val deadline: Date? = null,              // hasta cuándo debe estar hecha
    val priority: Int = 1,                   // 1=baja, 2=media, 3=alta
    val scheduled_start: Date? = null,       // asignado por el planificador
    val scheduled_end: Date? = null,         // asignado por el planificador
    val is_scheduled: Boolean = false        // ya fue colocada automáticamente
)