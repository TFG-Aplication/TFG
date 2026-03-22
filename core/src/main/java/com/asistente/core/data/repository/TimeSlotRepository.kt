package com.asistente.core.data.repository

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.asistente.core.data.local.daos.TimeSlotDao
import com.asistente.core.data.worker.TimeSlotWorker
import com.asistente.core.domain.models.TimeSlot
import com.asistente.core.domain.ropositories.interfaz.TimeSlotRepositoryInterface
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TimeSlotRepository @Inject constructor(
    private val timeSlotDao: TimeSlotDao,
    private val workManager: WorkManager
) : TimeSlotRepositoryInterface {

    override suspend fun getTimeSlotById(id: String): TimeSlot? =
        timeSlotDao.getTimeSlotById(id)

    override fun getAllTimeSlotsByCalendarId(calendarId: String): Flow<List<TimeSlot>> =
        timeSlotDao.getAllTimeSlotsByCalendarIdFlow(calendarId)

    override fun getAllTimeSlotsByUserId(userId: String): Flow<List<TimeSlot>> =
        timeSlotDao.getAllTimeSlotsByUserIdFlow(userId)

    override suspend fun saveTimeSlot(timeSlot: TimeSlot, isSharedCalendar: Boolean) {
        timeSlotDao.insertTimeSlot(timeSlot.copy(syncStatus = 0))
        enqueueSyncWorker(timeSlot.parentCalendarId)
    }

    override suspend fun updateTimeSlot(timeSlot: TimeSlot) {
        timeSlotDao.insertTimeSlot(timeSlot.copy(syncStatus = 0))
        enqueueSyncWorker(timeSlot.parentCalendarId)
    }

    override suspend fun deleteTimeSlot(timeSlotId: String, isShared: Boolean) {
        val slot = timeSlotDao.getTimeSlotById(timeSlotId)
        slot?.let {
            timeSlotDao.insertTimeSlot(it.copy(syncStatus = 2))
            enqueueSyncWorker(it.parentCalendarId)
        }
    }

    override suspend fun deleteTimeSlotByTaskId(taskId: String, isShared: Boolean) {
        val slot = timeSlotDao.getTimeSlotByTaskId(taskId)
        slot?.let {
            timeSlotDao.insertTimeSlot(it.copy(syncStatus = 2))
            enqueueSyncWorker(it.parentCalendarId)
        }
    }

    private fun enqueueSyncWorker(calendarId: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<TimeSlotWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf(TimeSlotWorker.KEY_CALENDAR_ID to calendarId))
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniqueWork(
            "sync_timeslot_$calendarId",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}