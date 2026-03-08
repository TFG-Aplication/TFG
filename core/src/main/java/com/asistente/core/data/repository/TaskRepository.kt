package com.asistente.core.data.repository

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.asistente.core.data.local.daos.TaskDao
import com.asistente.core.data.worker.TaskWorker
import com.asistente.core.domain.models.Task
import com.asistente.core.domain.ropositories.interfaz.TaskRepositoryInterface
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val workManager: WorkManager
) : TaskRepositoryInterface {

    override suspend fun getTaskById(id: String): Task? {
        return taskDao.getTaskById(id)
    }

    override fun getAllTaskByUserId(userId: String): Flow<List<Task>> {
        return taskDao.getAllTasksByUserIdFlow(userId)
    }

    override fun getAllTaskByCalendarId(calendarId: String): Flow<List<Task>> {
        return taskDao.getAllTasksByCalendarIdFlow(calendarId)
    }

    override suspend fun saveTask(task: Task, isSharedCalendar: Boolean) {
        taskDao.insertTask(task.copy(syncStatus = 0))


            enqueueSyncWorker(task.parentCalendarId)

    }

    override suspend fun updateTask(task: Task) {
        taskDao.insertTask(task.copy(syncStatus = 0))
        enqueueSyncWorker(task.parentCalendarId)
    }

    override suspend fun deleteTask(taskId: String, isShared: Boolean) {

            val task = taskDao.getTaskById(taskId)
            task?.let {
                taskDao.insertTask(it.copy(syncStatus = 2))
                enqueueSyncWorker(it.parentCalendarId)
            }

    }

    private fun enqueueSyncWorker(calendarId: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<TaskWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf(TaskWorker.KEY_CALENDAR_ID to calendarId))
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10,
                TimeUnit.SECONDS
            )
            .build()

        workManager.enqueueUniqueWork(
            "sync_task_$calendarId",
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )
    }
}