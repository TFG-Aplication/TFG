package com.asistente.core.domain.models
import com.google.firebase.firestore.Exclude
import java.util.UUID

open class BaseEntity(
    open val id: String =  UUID.randomUUID().toString(),
    open val owners: List<String> = emptyList(),
    open val place: String ?=null,
    open val notes: String? = null,
    open val parentCalendarId: String = "",
    open val name: String = "",

    open val categoryId: String? = null,

    open val alerts: List<Long>? = emptyList(),


    @get:Exclude
    open val syncStatus: Int = 0
)