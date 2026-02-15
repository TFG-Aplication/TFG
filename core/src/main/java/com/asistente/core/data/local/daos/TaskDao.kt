package com.asistente.core.data.local.daos


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.asistente.core.domain.models.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // Reactivas para UI)
    @Query("SELECT * FROM tasks where (owners LIKE '%\"' || :userId || '\"%' OR owners LIKE '%' || :userId || '%') AND syncStatus != 2")
    fun getAllTasksByUserIdFlow(userId: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks where parentCalendarId = :id AND syncStatus != 2")
    fun getAllTasksByCalendarIdFlow(id: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskByIdFlow(taskId: String): Flow<Task?>

    // SUSPENDIDAS (para lógica de repositorio/sync)

    @Query("SELECT * FROM tasks where (owners LIKE '%\"' || :userId || '\"%' OR owners LIKE '%' || :userId || '%') AND syncStatus != 2")
    suspend fun getAllTasksByUserId(userId: String): List<Task>

    @Query("SELECT * FROM tasks where parentCalendarId = :id AND syncStatus != 2")
    suspend fun getAllTasksByCalendarId(id: String): List<Task>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): Task?

    //  INSERT / UPDATE / DELETE

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    //  SINCRONIZACIÓN

    @Query("SELECT * FROM tasks WHERE syncStatus = 0 AND parentCalendarId = :calendarId")
    suspend fun getUnsyncedTask(calendarId: String): List<Task>

    @Query("SELECT * FROM tasks WHERE syncStatus = :status AND parentCalendarId = :calendarId")
    suspend fun getTaskBySyncStatus(status: Int, calendarId: String): List<Task>

}