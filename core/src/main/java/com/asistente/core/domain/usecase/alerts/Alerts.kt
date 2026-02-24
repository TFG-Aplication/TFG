package com.asistente.core.domain.usecase.alerts

import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.asistente.core.data.worker.TaskAlertWorker
import com.asistente.core.domain.models.Task
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class Alerts @Inject constructor(
    private val workManager: WorkManager
) {
    operator fun invoke(task: Task) {
        workManager.cancelAllWorkByTag(task.id)

        val now = System.currentTimeMillis()

        task.alerts
            ?.filter { it > now }
            ?.forEach { alertTimestamp ->
                val request = OneTimeWorkRequestBuilder<TaskAlertWorker>()
                    .setInitialDelay(alertTimestamp - now, TimeUnit.MILLISECONDS)
                    .setInputData(
                        workDataOf(
                            TaskAlertWorker.KEY_TASK_ID   to task.id,
                            TaskAlertWorker.KEY_TASK_NAME to task.name
                        )
                    )
                    .addTag(task.id)
                    .build()

                workManager.enqueueUniqueWork(
                    "alert_${task.id}_$alertTimestamp",
                    ExistingWorkPolicy.REPLACE,
                    request
                )
            }
    }
}