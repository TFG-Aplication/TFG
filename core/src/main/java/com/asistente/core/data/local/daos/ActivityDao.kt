package com.asistente.core.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.asistente.core.domain.models.Activity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {

    @Query("SELECT * FROM activities WHERE (owners LIKE '%\"' || :userId || '\"%' OR owners LIKE '%' || :userId || '%') AND syncStatus != 2")
    fun getAllActivitiesByUserIdFlow(userId: String): Flow<List<Activity>>

    @Query("SELECT * FROM activities WHERE parentCalendarId = :id AND syncStatus != 2")
    fun getAllActivitiesByCalendarIdFlow(id: String): Flow<List<Activity>>

    @Query("SELECT * FROM activities WHERE id = :activityId")
    fun getActivityByIdFlow(activityId: String): Flow<Activity?>

    @Query("SELECT * FROM activities WHERE (owners LIKE '%\"' || :userId || '\"%' OR owners LIKE '%' || :userId || '%') AND syncStatus != 2")
    suspend fun getAllActivitiesByUserId(userId: String): List<Activity>

    @Query("SELECT * FROM activities WHERE parentCalendarId = :id AND syncStatus != 2")
    suspend fun getAllActivitiesByCalendarId(id: String): List<Activity>

    @Query("SELECT * FROM activities WHERE id = :activityId")
    suspend fun getActivityById(activityId: String): Activity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: Activity)

    @Update
    suspend fun updateActivity(activity: Activity)

    @Query("DELETE FROM activities WHERE id = :id")
    suspend fun deleteActivityById(id: String)

    @Query("SELECT * FROM activities WHERE syncStatus = 0 AND parentCalendarId = :calendarId")
    suspend fun getUnsyncedActivities(calendarId: String): List<Activity>

    @Query("SELECT * FROM activities WHERE syncStatus = :status AND parentCalendarId = :calendarId")
    suspend fun getActivityBySyncStatus(status: Int, calendarId: String): List<Activity>

    // ── Consultas específicas del planificador ──
    @Query("SELECT * FROM activities WHERE is_scheduled = 0 AND parentCalendarId = :calendarId AND syncStatus != 2")
    suspend fun getUnscheduledActivities(calendarId: String): List<Activity>

    @Query("SELECT * FROM activities WHERE is_scheduled = 1 AND parentCalendarId = :calendarId AND syncStatus != 2")
    fun getScheduledActivitiesFlow(calendarId: String): Flow<List<Activity>>
}