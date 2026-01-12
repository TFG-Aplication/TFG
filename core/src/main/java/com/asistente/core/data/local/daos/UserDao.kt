package com.asistente.core.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.asistente.core.domain.models.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users where userId = :id")
    fun getUsersById(id: String): User

    @Query("SELECT * FROM users where email = :email LIMIT 1")
    fun getUsersByEmail(email: String): User?

    @Query("SELECT * FROM users where code = :code")
    fun getUsersByCodeQR(code: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

}