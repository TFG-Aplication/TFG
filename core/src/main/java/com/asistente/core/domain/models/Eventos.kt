package com.asistente.core.domain.models
import com.google.firebase.firestore.Exclude
import java.util.UUID

open class BaseEntity(
    open val id: String =  UUID.randomUUID().toString(),
    open val owners: List<String> = listOf("local_user"),
    open val place: String ="",
    open val notes: String="",
    open val parentCalendarId: String = "",

    open val color: String = "",


    open val category: Categoria = Categoria.Evento,
    open val name: String = "",

    @get:Exclude
    open val syncStatus: Int = 0
)