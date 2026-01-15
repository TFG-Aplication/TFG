package com.asistente.core.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.models.User
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDao {

    @Query("SELECT * FROM calendars where owners LIKE '%' || :userId || '%'")
    fun getAllCalendarsByUserId(userId: String): Flow<List<Calendar>>

    @Query("SELECT * FROM calendars where owners LIKE '%' || :userId || '%'")
    fun getAllCalendarsList(userId: String): List<Calendar>

    @Query("SELECT * FROM calendars where id = :id")
    fun getCalendarById(id: String): Calendar?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendar(calendar: Calendar)

    @Query("DELETE FROM calendars WHERE id = :id")
    suspend fun deleteCalendarById(id: String)

    @Query("SELECT * FROM calendars where code = :code")
    fun getUsersByCodeQR(code: String): Calendar?
}