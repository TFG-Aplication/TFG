package com.asistente.core.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.asistente.core.data.local.daos.ActivityDao
import com.asistente.core.data.remote.ActivityRemoteServices
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ActivityWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val activityDao: ActivityDao,
    private val activityRemoteServices: ActivityRemoteServices
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_CALENDAR_ID = "calendar_id"
    }

    override suspend fun doWork(): Result {
        val calendarId = inputData.getString(KEY_CALENDAR_ID) ?: return Result.failure()
        return try {
            uploadPendingActivities(calendarId)
            deleteMarkedActivities(calendarId)
            downloadRemoteActivities(calendarId)
            cleanupDeletedActivities(calendarId)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun uploadPendingActivities(calendarId: String) {
        activityDao.getUnsyncedActivities(calendarId).forEach { activity ->
            val success = activityRemoteServices.saveActivityRemote(activity)
            if (success) activityDao.insertActivity(activity.copy(syncStatus = 1))
        }
    }

    private suspend fun deleteMarkedActivities(calendarId: String) {
        activityDao.getActivityBySyncStatus(2, calendarId).forEach { activity ->
            val deleted = activityRemoteServices.deleteActivityRemote(activity.id)
            val exists = activityRemoteServices.existsActivity(activity.id)
            if (deleted || !exists) activityDao.deleteActivityById(activity.id)
        }
    }

    private suspend fun downloadRemoteActivities(calendarId: String) {
        activityRemoteServices.getAllActivitiesByCalendarIdRemote(calendarId).forEach { remote ->
            val local = activityDao.getActivityById(remote.id)
            when {
                local == null -> activityDao.insertActivity(remote.copy(syncStatus = 1))
                local.syncStatus == 1 -> activityDao.insertActivity(remote.copy(syncStatus = 1))
                local.syncStatus == 2 -> { /* marcado para eliminar, no sobrescribir */ }
                local.syncStatus == 0 -> { /* pendiente de subir, conflicto */ }
            }
        }
    }

    private suspend fun cleanupDeletedActivities(calendarId: String) {
        val remoteIds = activityRemoteServices
            .getAllActivitiesByCalendarIdRemote(calendarId).map { it.id }.toSet()
        activityDao.getAllActivitiesByCalendarId(calendarId).forEach { local ->
            if (local.syncStatus == 1 && !remoteIds.contains(local.id))
                activityDao.deleteActivityById(local.id)
        }
    }
}