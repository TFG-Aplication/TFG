package com.asistente.core.data.repository

import com.asistente.core.data.local.daos.TaskDao
import com.asistente.core.data.remote.TaskRemoteServices
import com.asistente.core.domain.models.Task
import com.asistente.core.domain.ropositories.interfaz.TaskRepositoryInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject


class TaskRepository @Inject constructor(
    private val localTask: TaskDao,
    private val remoteTask: TaskRemoteServices,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : TaskRepositoryInterface {



    override suspend fun getTaskById(id: String): Task? {
        var task = localTask.getTaskById(id)
        if (task == null)
            task = remoteTask.getTaskByIdRemote(id)
            if (task != null)
                localTask.insertTask(task)
// deberia a lo mejor poner q si esta en local pero no en remoto subirla ???
        return task
    }

    override fun getAllTaskByUserId(id: String): Flow<List<Task>> {
        externalScope.launch {
            try {
                val remoteTasks = remoteTask.getAllTasksByUserIdRemote(id)
                val localTasks = localTask.getAllTaskListByUserId(id)
                val remoteIds = remoteTasks.map { it.id }
                localTasks.forEach { local ->
                    if (local.syncStatus == 1 && !remoteIds.contains(local.id)) {
                        localTask.deleteTaskById(local.id)
                    }
                    if (local.syncStatus == 0 && !remoteIds.contains(local.id)) {
                        remoteTask.saveTaskRemote(local)
                        local.syncStatus = 1
                    }
                }
                remoteTasks.forEach { remote ->
                    localTask.insertTask(remote)
                }

            } catch (e: Exception) {
            }
        }

        // El Flow de Room se disparará automáticamente al detectar los cambios del launch
        return localTask.getAllTasksByUserId(id)
    }

    override fun getAllTaskByCalendarId(calendarId: String): Flow<List<Task>> {
        externalScope.launch {
            try {
                val remoteTasks = remoteTask.getAllTasksByCalendarIdRemote(calendarId)

                val localTasks = localTask.getAllTaskList(calendarId)

                val remoteIds = remoteTasks.map { it.id }
                localTasks.forEach { local ->
                    if (local.syncStatus == 1 && !remoteIds.contains(local.id)) {
                        localTask.deleteTaskById(local.id)
                    }
                    if (local.syncStatus == 0 && !remoteIds.contains(local.id)) {
                        remoteTask.saveTaskRemote(local)
                        local.syncStatus = 1
                    }
                }
                remoteTasks.forEach { localTask.insertTask(it) }
            } catch (e: Exception) {
            }
        }
        return localTask.getAllTasksByCalendarId(calendarId)
    }

    override suspend fun saveTask(task: Task, isSharedCalendar: Boolean) {
        if (isSharedCalendar) {
            // Si caleario es compartido y solo si hay internet
            val success = remoteTask.saveTaskRemote(task)
            if (success) {
                localTask.insertTask(task)
            } else {
                throw Exception("Conexión necesaria para modificar calendarios compartidos")
            }
        } else {
            // si calendario es normal
            localTask.insertTask(task)
            externalScope.launch {
                remoteTask.saveTaskRemote(task)
            }
        }
    }

    override suspend fun deleteTask(taskId: String, isShared: Boolean) {
        if (isShared) {
            val success = remoteTask.deleteTaskRemote(taskId)
            if (success) localTask.deleteTaskById(taskId)
            else throw Exception("No se pudo borrar: verifica tu conexión")
        } else {
            localTask.deleteTaskById(taskId)
            externalScope.launch { remoteTask.deleteTaskRemote(taskId) }
        }
    }
}