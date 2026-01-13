package com.asistente.core.domain.ropositories.`interface`

import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.models.User
import kotlinx.coroutines.flow.Flow

interface CalendarRepositoryInterface {
    suspend fun getCalendarById(id: String): Calendar?
    suspend fun saveCalendar(calendar: Calendar)
    fun getAllCalendarByUserId(id: String): Flow<List<Calendar>>?
    suspend fun deleteCalendar(id: String, isShared: Boolean)
}