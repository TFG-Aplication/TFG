package com.asistente.core.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date
import androidx.room.ForeignKey
import java.util.UUID
import javax.annotation.Nonnull


@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Calendar::class,
            parentColumns = ["id"],
            childColumns = ["parentCalendarId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Task(
    @PrimaryKey override val id: String = UUID.randomUUID().toString(),
    override val owners: List<String> = emptyList(),
    override val syncStatus: Int = 0,
    override val categoryId: String? = null,
    override val place: String? = null,
    override val notes: String? = null,
    @Nonnull
    override val name: String = "",
    @Nonnull
    override val parentCalendarId: String = "",

    override val alerts: List<Long>? = emptyList(),


    //atrib expecificos
    @Nonnull
    @set:ServerTimestamp
    var init_date: Date? = null,
    @Nonnull
    @set:ServerTimestamp
    var finish_date: Date? = null,


    ) : BaseEntity(id, owners, place, notes, parentCalendarId, name, categoryId, alerts, syncStatus)