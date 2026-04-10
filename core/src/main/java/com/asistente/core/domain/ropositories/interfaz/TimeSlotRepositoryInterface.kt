package com.asistente.core.domain.ropositories.interfaz

import com.asistente.core.domain.models.TimeSlot
import kotlinx.coroutines.flow.Flow

interface TimeSlotRepositoryInterface {
    suspend fun getTimeSlotById(id: String): TimeSlot?
    fun getAllTimeSlotsByCalendarId(calendarId: String): Flow<List<TimeSlot>>
    fun getAllTimeSlotsByUserId(userId: String): Flow<List<TimeSlot>>
    suspend fun getTimeSlotByTaskId(taskId: String): TimeSlot?
    suspend fun saveTimeSlot(timeSlot: TimeSlot, isSharedCalendar: Boolean)
    suspend fun updateTimeSlot(timeSlot: TimeSlot)
    suspend fun deleteTimeSlot(timeSlotId: String, isShared: Boolean)
    suspend fun deleteTimeSlotByTaskId(taskId: String, isShared: Boolean)
}