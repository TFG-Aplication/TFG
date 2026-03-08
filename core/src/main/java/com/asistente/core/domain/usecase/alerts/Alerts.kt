package com.asistente.core.domain.usecase.alerts

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.asistente.core.data.receiver.TaskAlertReceiver
import com.asistente.core.domain.models.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class Alerts @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    operator fun invoke(task: Task) {
        // Cancelar alarmas anteriores de esta tarea
        task.alerts?.forEachIndexed { index, _ ->
            val cancelIntent = buildPendingIntent(task.id, task.name, index)
            alarmManager.cancel(cancelIntent)
        }

        val now = System.currentTimeMillis()

        task.alerts
            ?.filter { it > now }
            ?.forEachIndexed { index, alertTimestamp ->
                val pendingIntent = buildPendingIntent(task.id, task.name, index)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    !alarmManager.canScheduleExactAlarms()
                ) {
                    // Fallback si no tiene permiso de alarma exacta
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        alertTimestamp,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alertTimestamp,
                        pendingIntent
                    )
                }
            }
    }

    private fun buildPendingIntent(taskId: String, taskName: String, index: Int): PendingIntent {
        val intent = Intent(context, TaskAlertReceiver::class.java).apply {
            putExtra(TaskAlertReceiver.KEY_TASK_ID, taskId)
            putExtra(TaskAlertReceiver.KEY_TASK_NAME, taskName)
        }
        // requestCode único por alerta: combinamos hash del id + índice
        val requestCode = (taskId.hashCode() * 31) + index
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}