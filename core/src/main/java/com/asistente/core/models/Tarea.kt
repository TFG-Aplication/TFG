package com.asistente.core.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date
import androidx.room.ForeignKey
import java.util.UUID


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
    override val owners: List<String> = listOf("local_user"),
    override val syncStatus: Int = 0,

    override val place: String ="",
    override val notes: String="",

    override val category: Categoria = Categoria.Evento,

    override val name: String = "",

    override val parentCalendarId: String = "",

    //atrib expecificos
    @set:ServerTimestamp
    var init_date: Date? = null,

    @set:ServerTimestamp
    var finish_date: Date? = null,


    ) : BaseEntity(id, owners, place, notes, parentCalendarId, category, name, syncStatus)