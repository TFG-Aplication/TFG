package com.asistente.core.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val userId: String = "",

    val use_name: String = "",
    val email: String = "",
    val createdAt: Long = 0L,


    val syncStatus: Int = 0,

    @get:Exclude
    val lastInternalUpdate: Long = System.currentTimeMillis()
)