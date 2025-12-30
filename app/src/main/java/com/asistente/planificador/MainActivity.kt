package com.asistente.planificador

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.asistente.planificador.ui.theme.TrabajoFinDeGradoTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class MainActivity : ComponentActivity() {
    // Inicializamos Firestore
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrabajoFinDeGradoTheme {
                var statusMessage by remember { mutableStateOf("Listo para conectar con Firebase") }
                var isLoading by remember { mutableStateOf(false) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Planificador TFG",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            text = statusMessage,
                            modifier = Modifier.padding(16.dp),
                            color = if (statusMessage.contains("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )

                        Button(
                            onClick = {
                                isLoading = true
                                statusMessage = "Enviando datos..."
                                probarFirebase { resultado ->
                                    statusMessage = resultado
                                    isLoading = false
                                }
                            },
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Probar Conexión a Base de Datos")
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Función para probar que la escritura en Firestore funciona correctamente.
     */
    private fun probarFirebase(onResult: (String) -> Unit) {
        val testData = hashMapOf(
            "titulo" to "Mi primer plan",
            "fecha" to System.currentTimeMillis(),
            "descripcion" to "Si ves esto en la consola, la base de datos funciona"
        )

        db.collection("pruebas")
            .add(testData)
            .addOnSuccessListener { documentReference ->
                Log.d("FIREBASE_TEST", "Éxito: ${documentReference.id}")
                onResult("¡Conectado! Documento creado con ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e("FIREBASE_TEST", "Error al guardar", e)
                onResult("Error: ${e.localizedMessage}. Revisa las Reglas en la Consola.")
            }
    }
}