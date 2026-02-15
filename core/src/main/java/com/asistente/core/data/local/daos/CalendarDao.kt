package com.asistente.core.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.models.User
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDao {

    // Reactivas para UI)
    @Query("SELECT * FROM calendars where (owners LIKE '%\"' || :userId || '\"%' OR owners LIKE '%' || :userId || '%') AND syncStatus != 2")
    fun getAllCalendarsByUserIdFlow(userId: String): Flow<List<Calendar>>

    @Query("SELECT * FROM calendars WHERE id = :calendarId ")
    fun getCalendarByIdFlow(calendarId: String): Flow<Calendar?>

    @Query("SELECT * FROM calendars WHERE code = :code ")
    fun getCalendarByCodeFlow(code: String): Flow<Calendar?>

    // SUSPENDIDAS (para lógica de repositorio/sync)
    @Query("SELECT * FROM calendars where (owners LIKE '%\"' || :userId || '\"%' OR owners LIKE '%' || :userId || '%')")
    suspend fun getAllCalendarsByUserId(userId: String): List<Calendar>

    @Query("SELECT * FROM calendars WHERE id = :calendarId AND syncStatus != 2")
    suspend fun getCalendarById(calendarId: String): Calendar?

    @Query("SELECT * FROM calendars WHERE code = :code ")
    suspend fun getCalendarByCode(code: String): Calendar?


    //  INSERT / UPDATE / DELETE

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendar(calendar: Calendar)

    @Update
    suspend fun updateCalendar(calendar: Calendar)

    @Query("DELETE FROM calendars WHERE id = :id")
    suspend fun deleteCalendarById(id: String)

    //  SINCRONIZACIÓN

    @Query("SELECT * FROM calendars WHERE syncStatus = 0 AND owners LIKE '%' || :userId || '%'")
    suspend fun getUnsyncedCalendars(userId: String): List<Calendar>

    @Query("SELECT * FROM calendars WHERE syncStatus = :status AND owners LIKE '%' || :userId || '%'")
    suspend fun getCalendarBySyncStatus(status: Int, userId: String): List<Calendar>

}