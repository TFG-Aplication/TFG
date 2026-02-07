package com.asistente.core.data.local.daos


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.asistente.core.domain.models.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks where owners LIKE '%' || :id || '%'")
    fun getAllTasksByUserId(id: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks where owners LIKE '%' || :id || '%'")
    fun getAllTaskListByUserId(id: String): List<Task>

    @Query("SELECT * FROM tasks where parentCalendarId LIKE :id")
    fun getAllTasksByCalendarId(id: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks where parentCalendarId LIKE :id")
    fun getAllTaskList(id: String): List<Task>

    @Query("SELECT * FROM tasks where id = :id")
    suspend fun getTaskById(id: String): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)
}