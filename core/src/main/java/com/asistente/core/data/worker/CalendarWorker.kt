package com.asistente.core.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.asistente.core.data.local.daos.CalendarDao
import com.asistente.core.data.remote.CalendarRemoteServices
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CalendarWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val localCalendar: CalendarDao,
    private val remoteCalendar: CalendarRemoteServices,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userId = inputData.getString("local_user") ?: return Result.failure()

        return try {

            val unsyncedCalendar = localCalendar.getUnsyncedCalendars(userId)

            val remoteCalendars = remoteCalendar.getAllCalendarByUserIdRemote(userId)
            val remoteIds = remoteCalendars.map { it.id }.toSet()

            // SUBIDA: Buscar en Room   syncStatus = 0 para subirlas
            // en room + syn = 0 -> sube a fb
            unsyncedCalendar.forEach { local ->
                remoteCalendar.saveCalendarRemote(local)
                localCalendar.insertCalendar(local.copy(syncStatus = 1))

            }

            // Si está en Firebase pero no en mi Room ->
            // 2 opciones:
            // - estaba en room y se borro sin internet (sync = 2) -> lo borro de fb
            // - otro lo creo y nunca estuvo en room -> lo bajo

            // opcion 1
            val CalendarToDelete = localCalendar.getCalendarBySyncStatus(2, userId)
            CalendarToDelete.forEach { local ->
                remoteCalendar.deleteCalendarRemote(local.id)
                localCalendar.deleteCalendarById(local.id)

            }

            //opcion 2
            // Solo bajamos lo que no tenemos Y que no esté marcado para borrar (estado 2)
            remoteCalendars.forEach { remote ->
                val localVersion = localCalendar.getCalendarById(remote.id)
                if (localVersion == null) { // no existe en local
                    // Es nuevo de otra persona -> Lo bajo
                    localCalendar.insertCalendar(remote.copy(syncStatus = 1))
                } else if (localVersion.syncStatus == 1) {
                    // Ya lo tengo, solo actualizo por si el otro usuario cambió
                    localCalendar.insertCalendar(remote.copy(syncStatus = 1))
                }
                // Si localVersion.syncStatus es 2, NO HACEMOS NADA (ignoramos Firebase hasta que se borre)
            }


            // BAJADA Y LIMPIEZA: Solo si el calendario es compartido
            // Si está en mi Room con syncStatus = 1 pero NO está en Firebase -> Alguien lo borró
            //en room + syn =1 + no fb -> borra de room
            val localCalendars = localCalendar.getAllCalendarsList(userId)
            localCalendars.forEach { local ->
                if (local.syncStatus == 1 && !remoteIds.contains(local.id)) {
                    localCalendar.deleteCalendarById(local.id)
                }
            }

            Result.success()
        } catch (e: Exception) {
            // Si hay error de red, WorkManager no borra nada y reintenta luego
            Result.retry()
        }
    }
}