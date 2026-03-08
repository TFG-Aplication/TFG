package com.asistente.core.data.repository

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.asistente.core.data.local.daos.ActivityDao
import com.asistente.core.data.worker.ActivityWorker
import com.asistente.core.domain.models.Activity
import com.asistente.core.domain.repositories.interfaz.ActivityRepositoryInterface
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ActivityRepository @Inject constructor(
    private val activityDao: ActivityDao,
    private val workManager: WorkManager
) : ActivityRepositoryInterface {

    override suspend fun getActivityById(id: String): Activity? =
        activityDao.getActivityById(id)

    override fun getAllActivitiesByUserId(userId: String): Flow<List<Activity>> =
        activityDao.getAllActivitiesByUserIdFlow(userId)

    override fun getAllActivitiesByCalendarId(calendarId: String): Flow<List<Activity>> =
        activityDao.getAllActivitiesByCalendarIdFlow(calendarId)

    override suspend fun saveActivity(activity: Activity, isSharedCalendar: Boolean) {
        activityDao.insertActivity(activity.copy(syncStatus = 0))
        enqueueSyncWorker(activity.parentCalendarId)
    }

    override suspend fun updateActivity(activity: Activity) {
        activityDao.insertActivity(activity.copy(syncStatus = 0))
        enqueueSyncWorker(activity.parentCalendarId)
    }

    override suspend fun deleteActivity(activityId: String, isShared: Boolean) {
        val activity = activityDao.getActivityById(activityId)
        activity?.let {
            activityDao.insertActivity(it.copy(syncStatus = 2))
            enqueueSyncWorker(it.parentCalendarId)
        }
    }

    private fun enqueueSyncWorker(calendarId: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<ActivityWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf(ActivityWorker.KEY_CALENDAR_ID to calendarId))
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniqueWork(
            "sync_activity_$calendarId",
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )
    }
}