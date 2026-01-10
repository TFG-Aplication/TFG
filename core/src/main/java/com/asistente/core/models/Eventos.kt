package com.asistente.core.models
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude

open class BaseEntity(
    open val id: String = "",
    open val owners: List<String> = listOf("local_user"),
    open val place: String ="",
    open val notes: String="",
    open val parentCalendarId: String = "",

    open val category: Categoria = Categoria.Evento,
    open val name: String = "",

    @get:Exclude
    open val syncStatus: Int = 0
)