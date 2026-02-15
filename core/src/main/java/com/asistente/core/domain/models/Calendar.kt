package com.asistente.core.domain.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID
import javax.annotation.Nonnull

@Entity(tableName = "calendars",
        indices = [Index(value = ["code"], unique = true)]
)
data class Calendar(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @Nonnull
    val name: String = "",
    @Nonnull
    val code: String = "",
    val owners: List<String> = emptyList(),
    val isShared: Boolean = false,
    var syncStatus: Int = 0,
    val firebaseId: String? = null
)