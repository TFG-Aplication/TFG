package com.asistente.core.data.repository

import androidx.work.*
import com.asistente.core.data.local.daos.CalendarDao
import com.asistente.core.data.worker.CalendarWorker
import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.ropositories.interfaz.CalendarRepositoryInterface
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CalendarRepository @Inject constructor(
    private val localCalendar: CalendarDao,
    private val workManager: WorkManager
) : CalendarRepositoryInterface {

    override suspend fun getCalendarById(id: String): Calendar? {
        return localCalendar.getCalendarById(id)
    }

    override fun getAllCalendarByUserId(id: String): Flow<List<Calendar>> {
        syncWorker(id)
        return localCalendar.getAllCalendarsByUserIdFlow(id)
    }


    override suspend fun saveCalendar(calendar: Calendar) {
        // Guardar en Room con estado "pendiente de subir" (0)
        localCalendar.insertCalendar(calendar.copy(syncStatus = 0))
        syncWorker(calendar.owners.firstOrNull() ?: "unknown") // esto en cuando se haga el login se cambia
    }

    override suspend fun updateCalendar(calendar: Calendar) {
        localCalendar.updateCalendar(calendar.copy(syncStatus = 0))
        syncWorker(calendar.owners.firstOrNull() ?: "unknown")
    }

    override suspend fun deleteCalendar(id: String, isShared: Boolean) {
        if (isShared) {
            val calendar = localCalendar.getCalendarById(id)
            calendar?.let {
                // Marca como "pendiente de borrar" (2) cuando haya conex
                localCalendar.insertCalendar(it.copy(syncStatus = 2))
                syncWorker(it.owners.firstOrNull() ?: "unknown")
            }
        } else {
            localCalendar.deleteCalendarById(id)
        }
    }

    private fun syncWorker(userId: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) //conexion a internet necesaria
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<CalendarWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf(CalendarWorker.KEY_USER_ID to userId))
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniqueWork(
            "sync_calendar_$userId",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }
}