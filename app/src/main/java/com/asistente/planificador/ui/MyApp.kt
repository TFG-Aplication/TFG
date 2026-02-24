package com.asistente.planificador.ui

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Configuration
import androidx.hilt.work.HiltWorkerFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/*
 * PUNTO DE ENTRADA A NIVEL DE PROCESO (Application):
 * La anotación @HiltAndroidApp activa Hilt y crea el "cerebro" de la aplicación
 *
 * 1. Inicializa el grafo de dependencias antes de nada
 * 2. Mantiene vivas las instancias globales (Base de Datos, Firebase) mientras la app esté abierta
 * 3. Es el contenedor donde viven los objetos que no mueren al girar la pantalla.
 */

@HiltAndroidApp
class MyApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()


    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "task_alerts_channel",
                "Alertas de tareas",
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }
}
