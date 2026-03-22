package com.asistente.core.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import androidx.room.Index
import java.time.Instant
import java.util.UUID
import javax.annotation.Nonnull


@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["parentCalendarId"]),
        Index(value = ["categoryId"])
    ]
)
data class Task(
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

    //atrib expecificos
    @Nonnull
    val init_date: Date = Date.from(Instant.now()),
    @Nonnull
    val finish_date: Date = Date.from(Instant.now()),
    val firebaseId: String? = null,
    val blockTimeSlot: Boolean = false

    ) 