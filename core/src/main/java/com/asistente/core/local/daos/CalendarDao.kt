package com.asistente.core.local.daos

import androidx.room.Dao
import androidx.room.Query
import com.asistente.core.models.Calendar
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDao {

    @Query("SELECT * FROM calendars")
    fun getAllCalendars(): Flow<List<Calendar>>

    @Query("SELECT * FROM calendars where id = :id")
    fun getCalendarById(id: String): Calendar
}