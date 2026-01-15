package com.asistente.core.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "calendars")
data class Calendar(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val code: String = "",
    val owners: List<String> = emptyList(),
    val isShared: Boolean = false,
    val syncStatus: Int = 0
)