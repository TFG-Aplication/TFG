package com.asistente.planificador

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.asistente.core.data.repository.CalendarRepository
import com.asistente.core.domain.usecase.calendar.CreateCalendar
import com.asistente.core.ui.viewmodels.CalendarViewModel
import com.asistente.planificador.ui.MyApp
import com.asistente.planificador.ui.screens.MainCalendar
import com.asistente.planificador.ui.theme.TrabajoFinDeGradoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Obtenemos la instancia real de Firebase
        val firestoreInstance = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        // 2. Se la pasamos al servicio remoto
        val remoteService = com.asistente.core.data.remote.CalendarRemoteServices(
            firestore = firestoreInstance
        )

        val repository = CalendarRepository(
            localCalendar = MyApp.database.calendarDao(),
            remoteCalendar = remoteService
        )

        val createCalendarUseCase = CreateCalendar(repository)
        val viewModel = CalendarViewModel(createCalendarUseCase, repository)

        setContent {
            TrabajoFinDeGradoTheme {
                MainCalendar(viewModel = viewModel)            }
        }
    }
}