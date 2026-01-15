package com.asistente.core.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import androidx.room.Index

@Entity(tableName = "users",
    indices = [Index(value = ["code"], unique = true), Index(value = ["email"], unique = true)]
)
data class User(
    @PrimaryKey
    val userId: String = "",

    val useName: String = "",
    val email: String = "",

    val createdAt: Long = 0L,


    val syncStatus: Int = 0,

    @get:Exclude
    val lastInternalUpdate: Long = System.currentTimeMillis()
)