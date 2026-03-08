package com.asistente.core.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.asistente.core.data.local.daos.TimeSlotDao
import com.asistente.core.data.remote.TimeSlotRemoteServices
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TimeSlotWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val timeSlotDao: TimeSlotDao,
    private val timeSlotRemoteServices: TimeSlotRemoteServices
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_CALENDAR_ID = "calendar_id"
    }

    override suspend fun doWork(): Result {
        val calendarId = inputData.getString(KEY_CALENDAR_ID) ?: return Result.failure()
        return try {
            uploadPendingSlots(calendarId)
            deleteMarkedSlots(calendarId)
            downloadRemoteSlots(calendarId)
            cleanupDeletedSlots(calendarId)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    // LOCAL → FIREBASE
    private suspend fun uploadPendingSlots(calendarId: String) {
        timeSlotDao.getUnsyncedTimeSlots(calendarId).forEach { slot ->
            val success = timeSlotRemoteServices.saveTimeSlotRemote(slot)
            if (success) timeSlotDao.insertTimeSlot(slot.copy(syncStatus = 1))
        }
    }

    // ELIMINAR MARCADOS COMO syncStatus=2
    private suspend fun deleteMarkedSlots(calendarId: String) {
        timeSlotDao.getTimeSlotBySyncStatus(2, calendarId).forEach { slot ->
            val deleted = timeSlotRemoteServices.deleteTimeSlotRemote(slot.id)
            val exists  = timeSlotRemoteServices.existsTimeSlot(slot.id)
            if (deleted || !exists) timeSlotDao.deleteTimeSlotById(slot.id)
        }
    }

    // FIREBASE → LOCAL
    private suspend fun downloadRemoteSlots(calendarId: String) {
        timeSlotRemoteServices.getAllTimeSlotsByCalendarIdRemote(calendarId).forEach { remote ->
            val local = timeSlotDao.getTimeSlotById(remote.id)
            when {
                local == null            -> timeSlotDao.insertTimeSlot(remote.copy(syncStatus = 1))
                local.syncStatus == 1    -> timeSlotDao.insertTimeSlot(remote.copy(syncStatus = 1))
                local.syncStatus == 2    -> { /* marcado para eliminar, no sobrescribir */ }
                local.syncStatus == 0    -> { /* pendiente de subir, conflicto */ }
            }
        }
    }

    // ELIMINAR LOCALES QUE YA NO EXISTEN EN FIREBASE
    private suspend fun cleanupDeletedSlots(calendarId: String) {
        val remoteIds = timeSlotRemoteServices
            .getAllTimeSlotsByCalendarIdRemote(calendarId)
            .map { it.id }.toSet()

        timeSlotDao.getAllTimeSlotsByCalendarId(calendarId).forEach { local ->
            if (local.syncStatus == 1 && local.id !in remoteIds)
                timeSlotDao.deleteTimeSlotById(local.id)
        }
    }
}