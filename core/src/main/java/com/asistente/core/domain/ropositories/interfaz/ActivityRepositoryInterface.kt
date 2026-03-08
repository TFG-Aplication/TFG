package com.asistente.core.domain.repositories.interfaz

import com.asistente.core.domain.models.Activity
import kotlinx.coroutines.flow.Flow

interface ActivityRepositoryInterface {
    suspend fun getActivityById(id: String): Activity?
    fun getAllActivitiesByUserId(userId: String): Flow<List<Activity>>
    fun getAllActivitiesByCalendarId(calendarId: String): Flow<List<Activity>>
    suspend fun saveActivity(activity: Activity, isSharedCalendar: Boolean)
    suspend fun updateActivity(activity: Activity)
    suspend fun deleteActivity(activityId: String, isShared: Boolean)
}