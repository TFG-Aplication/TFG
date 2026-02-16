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
    private val calendarDao: CalendarDao,
    private val remoteServices: CalendarRemoteServices
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "tag" // pa el debug
        const val KEY_USER_ID = "user_id"
    }

    override suspend fun doWork(): Result {
        val userId = inputData.getString(KEY_USER_ID) ?: run {
            Log.e(TAG, "userId no proporcionado")
            return Result.failure()
        }

        return try {
            Log.d(TAG, "Iniciando sincronización para usuario: $userId")

            // SUBIDA: Subir calendarios pendientes (syncStatus = 0)
            uploadPendingCalendars(userId)

            // ELIMINACIÓN: Borrar calendarios marcados (syncStatus = 2)
            deleteMarkedCalendars(userId)

            // BAJADA: Descargar calendarios de Firebase
            downloadRemoteCalendars(userId)

            // LIMPIEZA: Eliminar calendarios locales que no existen en Firebase
            cleanupDeletedCalendars(userId)

            Log.d(TAG, "Sincronización completada")
            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "Error en sincronización: ${e.message}", e)
            Result.retry()
        }
    }

    // SUBIDA: LOCAL → FIREBASE

    private suspend fun uploadPendingCalendars(userId: String) {
        val unsyncedCalendars = calendarDao.getUnsyncedCalendars(userId)

        if (unsyncedCalendars.isEmpty()) {
            Log.d(TAG, "Sin calendarios pendientes de subir")
            return
        }

        Log.d(TAG, "Subiendo ${unsyncedCalendars.size} calendarios pendientes")

        unsyncedCalendars.forEach { calendar ->
            val success = remoteServices.saveCalendarRemote(calendar)

            if (success) {
                calendarDao.insertCalendar(calendar.copy(syncStatus = 1))
                Log.d(TAG, " Subido: ${calendar.name}")
            } else {
                Log.w(TAG, "  Fallo al subir: ${calendar.name}")
            }
        }
    }

    //ELIMINACIÓN: BORRAR CALENDARIOS MARCADOS

    private suspend fun deleteMarkedCalendars(userId: String) {
        val calendarsToDelete = calendarDao.getCalendarBySyncStatus(2, userId)

        if (calendarsToDelete.isEmpty()) {
            Log.d(TAG, "Sin calendarios marcados para eliminar")
            return
        }

        Log.d(TAG, "Eliminando ${calendarsToDelete.size} calendarios marcados")

        calendarsToDelete.forEach { calendar ->
            val deletedInFirebase = remoteServices.deleteCalendarRemote(calendar.id)

            val existsInFirebase = remoteServices.existsCalendar(calendar.id)

            if (deletedInFirebase || !existsInFirebase) {
                calendarDao.deleteCalendarById(calendar.id)
                Log.d(TAG, " Eliminado: ${calendar.name}")
            } else {
                Log.w(TAG, " Fallo al eliminar: ${calendar.name}")
            }
        }
    }

    //BAJADA: FIREBASE → LOCAL

    private suspend fun downloadRemoteCalendars(userId: String) {
        val remoteCalendars = remoteServices.getAllCalendarByUserIdRemote(userId)

        if (remoteCalendars.isEmpty()) {
            Log.d(TAG, "Sin calendarios remotos para descargar")
            return
        }

        Log.d(TAG, "Descargando ${remoteCalendars.size} calendarios de Firebase")

        remoteCalendars.forEach { remoteCalendar ->
            val localCalendar = calendarDao.getCalendarById(remoteCalendar.id)

            when {
                localCalendar == null -> {
                    // No existe localmente → Insertar (es nuevo de otro)
                    calendarDao.insertCalendar(remoteCalendar.copy(syncStatus = 1))
                    Log.d(TAG, "Nuevo: ${remoteCalendar.name}")
                }

                localCalendar.syncStatus == 1 -> {
                    // Ya sincronizado → Actualizar
                    calendarDao.insertCalendar(remoteCalendar.copy(syncStatus = 1))
                    Log.d(TAG, " Actualizado: ${remoteCalendar.name}")
                }

                localCalendar.syncStatus == 2 -> {
                    // Marcado para eliminar → NO sobrescribir
                    Log.d(TAG, "Ignorado (marcado para eliminar): ${remoteCalendar.name}")
                }

                localCalendar.syncStatus == 0 -> {
                    // Pendiente de subir → Conflicto ????
                    Log.d(TAG, "Conflicto, manteniendo local: ${remoteCalendar.name}")
                }
            }
        }
    }

    // ELIMINAR LOCALES QUE NO EXISTEN EN FIREBASE

    private suspend fun cleanupDeletedCalendars(userId: String) {
        val localCalendars = calendarDao.getAllCalendarsByUserId(userId)
        val remoteCalendars = remoteServices.getAllCalendarByUserIdRemote(userId)
        val remoteIds = remoteCalendars.map { it.id }.toSet()

        var deletedCount = 0

        localCalendars.forEach { local ->
            // Eliminar solo si:
            // 1. Está sincronizado (syncStatus = 1) +
            // 2. NO existe en Firebase +
            // 3. Es compartido (calendarios locales no se borran)
            if (local.syncStatus == 1 && !remoteIds.contains(local.id) && local.isShared) {
                calendarDao.deleteCalendarById(local.id)
                deletedCount++
                Log.d(TAG, " Eliminado (borrado en Firebase): ${local.name}")
            }
        }

        if (deletedCount > 0) {
            Log.d(TAG, "Limpieza: $deletedCount calendarios eliminados")
        }
    }
}