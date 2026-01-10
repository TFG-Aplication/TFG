package com.asistente.core.local.daos

import androidx.room.Dao
import androidx.room.Query
import com.asistente.core.models.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users where userId = :id")
    fun getUsersById(id: String): User
}