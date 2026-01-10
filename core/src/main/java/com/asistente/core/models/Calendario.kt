package com.asistente.core.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "calendars")
data class Calendar(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val owners: List<String> = listOf("local_user"),

    val syncStatus: Int = 0
)