package com.asistente.core.domain.ropositories.interfaz

import com.asistente.core.domain.models.Calendar
import kotlinx.coroutines.flow.Flow

//necesaria pa reducir desacomplamiento
interface CalendarRepositoryInterface {
    suspend fun getCalendarById(id: String): Calendar?
    fun getAllCalendarByUserId(id: String): Flow<List<Calendar>>?
    suspend fun saveCalendar(calendar: Calendar)

    suspend fun updateCalendar(calendar: Calendar)

    suspend fun deleteCalendar(id: String, isShared: Boolean)

}