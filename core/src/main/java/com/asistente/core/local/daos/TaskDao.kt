package com.asistente.core.local.daos


import androidx.room.Dao
import androidx.room.Query
import com.asistente.core.models.Calendar
import com.asistente.core.models.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks where id = :id")
    fun getTaskById(id: String): Task
}