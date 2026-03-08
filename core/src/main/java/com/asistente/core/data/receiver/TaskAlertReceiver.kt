package com.asistente.core.data.receiver

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class TaskAlertReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val taskId   = intent.getStringExtra(KEY_TASK_ID)   ?: return
        val taskName = intent.getStringExtra(KEY_TASK_NAME) ?: return

        val deepLinkIntent = Intent(Intent.ACTION_VIEW, Uri.parse("asistente://task/$taskId")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(taskName)
            .setContentText("Tienes una tarea próxima")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
            == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(taskId.hashCode(), notification)
        }
    }

    companion object {
        const val KEY_TASK_ID   = "task_id"
        const val KEY_TASK_NAME = "task_name"
        const val CHANNEL_ID    = "task_alerts_channel"
    }
}