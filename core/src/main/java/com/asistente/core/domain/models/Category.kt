package com.asistente.core.domain.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date
import java.util.UUID

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = Calendar::class,
            parentColumns = ["id"],
            childColumns = ["parentCalendarId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Category(
    @PrimaryKey var id: String = UUID.randomUUID().toString(),
    var owners: List<String> = emptyList(),
    var syncStatus: Int = 0,
    var name: String = "",
    var parentCalendarId: String = "",
    var color: String = "",

    )