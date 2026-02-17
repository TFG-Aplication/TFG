package com.asistente.core.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.asistente.core.data.local.daos.TaskDao
import com.asistente.core.data.remote.TaskRemoteServices
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TaskWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val taskDao: TaskDao,
    private val taskRemoteServices: TaskRemoteServices
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_CALENDAR_ID = "calendar_id"
    }

    override suspend fun doWork(): Result {
        val calendarId = inputData.getString(KEY_CALENDAR_ID)
            ?: return Result.failure()

        return try {
            uploadPendingTasks(calendarId)
            deleteMarkedTasks(calendarId)
            downloadRemoteTasks(calendarId)
            cleanupDeletedTasks(calendarId)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    // SUBIDA: LOCAL → FIREBASE

    private suspend fun uploadPendingTasks(calendarId: String) {
        val unsyncedTasks = taskDao.getUnsyncedTask(calendarId)

        unsyncedTasks.forEach { task ->
            val success = taskRemoteServices.saveTaskRemote(task)
            if (success) {
                taskDao.insertTask(task.copy(syncStatus = 1))
            }
        }
    }

    //ELIMINACIÓN: BORRAR CALENDARIOS MARCADOS

    private suspend fun deleteMarkedTasks(calendarId: String) {
        val tasksToDelete = taskDao.getTaskBySyncStatus(2, calendarId)

        tasksToDelete.forEach { task ->
            val deletedInFirebase = taskRemoteServices.deleteTaskRemote(task.id)
            val existsInFirebase = taskRemoteServices.existsTask(task.id)

            if(deletedInFirebase || !existsInFirebase) {
                taskDao.deleteTaskById(task.id)
            }
        }
    }

    //BAJADA: FIREBASE → LOCAL

    private suspend fun downloadRemoteTasks(calendarId: String) {
        val remoteTasks = taskRemoteServices.getAllTasksByCalendarIdRemote(calendarId)

        remoteTasks.forEach { remoteTask ->
            val localVersion = taskDao.getTaskById(remoteTask.id)

            when {
                localVersion == null -> {
                    // Nueva tarea de otro usuario → Descargar
                    taskDao.insertTask(remoteTask.copy(syncStatus = 1))
                }
                localVersion.syncStatus == 1 -> {
                    // Ya sincronizada → Actualizar con versión remota
                    taskDao.insertTask(remoteTask.copy(syncStatus = 1))
                }

                localVersion.syncStatus == 2 -> {
                    // Marcado para eliminar → NO sobrescribir
                }

                localVersion.syncStatus == 0 -> {
                    // Pendiente de subir → Conflicto ????
                }
            }
        }
    }

    // ELIMINAR LOCALES QUE NO EXISTEN EN FIREBASE

    private suspend fun cleanupDeletedTasks(calendarId: String) {
        val remoteTasks = taskRemoteServices.getAllTasksByCalendarIdRemote(calendarId)
        val remoteIds = remoteTasks.map { it.id }.toSet()

        val localTasks = taskDao.getAllTasksByCalendarId(calendarId)

        localTasks.forEach { localTask ->
            if (localTask.syncStatus == 1 && !remoteIds.contains(localTask.id)) {
                taskDao.deleteTaskById(localTask.id)
            }
        }
    }
}