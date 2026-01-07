package com.asistente.planificador

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.asistente.planificador.ui.screens.CalendarScreen
import com.asistente.planificador.ui.theme.TrabajoFinDeGradoTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


class MainActivity : ComponentActivity() {
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrabajoFinDeGradoTheme {
                CalendarScreen()
            }
        }
    }
}