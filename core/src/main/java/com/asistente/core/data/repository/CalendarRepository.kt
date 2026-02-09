package com.asistente.core.data.repository

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.asistente.core.data.local.daos.CalendarDao
import com.asistente.core.data.remote.CalendarRemoteServices
import com.asistente.core.data.worker.CalendarWorker
import com.asistente.core.data.worker.CategoryWorker
import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.ropositories.interfaz.CalendarRepositoryInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CalendarRepository @Inject constructor(
    private val localCalendar: CalendarDao,
    @ApplicationContext private val context: Context

) : CalendarRepositoryInterface {

    private val workManager = WorkManager.getInstance(context)


    override suspend fun getCalendarById(id: String): Calendar? {
        return localCalendar.getCalendarById(id)

    }

    override fun getAllCalendarByUserId(id: String): Flow<List<Calendar>> {
        syncWorker(id)
        return localCalendar.getAllCalendarsByUserId(id)

    }

    override suspend fun saveCalendar(calendar: Calendar) {
            // Guardar en Room con estado "pendiente de subir" (0)
            localCalendar.insertCalendar(calendar.copy(syncStatus = 0))

            // poner en cola en el Worker para que lo suba cuando haya internet
            syncWorker("local_user")

    }

    override suspend fun deleteCalendar(id: String, isShared: Boolean) {
        if (isShared) {
            val calendar = localCalendar.getCalendarById(id)
            calendar?.let {
                // Marca como "pendiente de borrar" (2) cuando haya conex
                localCalendar.insertCalendar(it.copy(syncStatus = 2))

                // El Worker se encargará de borrarlo en Firebase y luego en Room
                syncWorker("local_user")
            }
        } else {
            // Borrado local inmediato si no es compartido
            localCalendar.deleteCalendarById(id)
            // intentar borrar en remoto si existe
            workManager.enqueue(
                OneTimeWorkRequestBuilder<CategoryWorker>()
                    .setInputData(workDataOf("calendar_id" to "GLOBAL", "local_user" to "local_user"))
                    .build()
            )
        }
    }

    private fun syncWorker(userId: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) //conexion a internet necesaria
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<CalendarWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf("local_user" to userId))
            // Reintento exponencial si falla Firebase
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniqueWork(
            "sync_calendar_$userId",
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            syncRequest
        )
    }
}

