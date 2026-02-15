package com.asistente.core.domain.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date
import java.util.UUID
import javax.annotation.Nonnull

@Entity(
    tableName = "categories",
    indices = [Index(value = ["parentCalendarId"])]
)
data class Category(
    @PrimaryKey var id: String = UUID.randomUUID().toString(),
    var syncStatus: Int = 0,
    @Nonnull
    var name: String = "",
    @Nonnull
    var parentCalendarId: String = "",
    var color: String = "",
    val firebaseId: String? = null

    )