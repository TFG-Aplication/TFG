import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.DialogProperties
import com.asistente.planificador.ui.screens.Primario
import com.asistente.planificador.ui.screens.Secundario

class SelectionDate {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TimePickerModal(
        initialHour: Int,
        initialMinute: Int,
        onDismiss: () -> Unit,
        onConfirm: (Int, Int) -> Unit
    ) {
        val timePickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true
        )
        var showingPicker by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = onDismiss,
            // Quitamos el ancho de plataforma para controlar el tamaño nosotros
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.9f), // Ajuste de margen lateral consistente
            confirmButton = {
                TextButton(onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }) {
                    Text("Confirmar", color = Primario, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(0.6f),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showingPicker = !showingPicker }) {
                        Icon(
                            imageVector = if (showingPicker) Icons.Default.Keyboard else Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = Primario
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = Color.Gray)
                    }
                }
            },
            containerColor = Color.White,
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (showingPicker) {
                        TimePicker(
                            state = timePickerState,
                            colors = TimePickerDefaults.colors(
                                clockDialColor = Secundario,
                                clockDialUnselectedContentColor = Color.Black,
                                selectorColor = Primario,
                                timeSelectorSelectedContainerColor = Primario.copy(alpha = 0.12f),
                                timeSelectorSelectedContentColor = Primario,
                                timeSelectorUnselectedContainerColor = Secundario,
                                timeSelectorUnselectedContentColor = Color.Black
                            )
                        )
                    } else {
                        MaterialTheme(
                            colorScheme = MaterialTheme.colorScheme.copy(
                                primary = Primario,      // Esto suele controlar el borde enfocado y el cursor
                                outline = Primario,      // Esto controla el borde desensufocado en algunas versiones
                                onSurface = Color.Black  // Color del texto
                            )
                        ) {
                            TimeInput(
                                state = timePickerState,
                                colors = TimePickerDefaults.colors(
                                    timeSelectorSelectedContainerColor = Primario.copy(alpha = 0.12f),
                                    timeSelectorSelectedContentColor = Primario,
                                    timeSelectorUnselectedContainerColor = Secundario,
                                    timeSelectorUnselectedContentColor = Color.Black,

                                    )
                            )
                        }
                    }
                }
            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DatePickerModal(
        initialDateMillis: Long?,
        onDismiss: () -> Unit,
        onConfirm: (Long?) -> Unit
    ) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateMillis ?: System.currentTimeMillis()
        )

        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme.copy(
                surface = Primario,
                onSurface = Color.White
            )
        ) {
            DatePickerDialog(
                onDismissRequest = onDismiss,
                // Forzamos el mismo margen que el TimePicker
                properties = DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier.fillMaxWidth(0.9f),
                confirmButton = {
                    TextButton(onClick = { onConfirm(datePickerState.selectedDateMillis) }) {
                        Text("Aceptar", color = Primario, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = Color.Gray)
                    }
                },
                colors = DatePickerDefaults.colors(containerColor = Color.White)
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor = Color.White,
                        // TEXTOS BLANCOS para que se vean sobre el fondo Primario de la cabecera
                        titleContentColor = Primario,
                        headlineContentColor = Primario,
                        navigationContentColor = Primario,

                        weekdayContentColor = Color.Gray,
                        dayContentColor = Color.Black,
                        selectedDayContainerColor = Primario,
                        selectedDayContentColor = Color.White,
                        todayContentColor = Primario,
                        todayDateBorderColor = Primario,

                        yearContentColor = Color.Black,
                        selectedYearContentColor = Color.White,
                        selectedYearContainerColor = Primario,
                        currentYearContentColor = Primario
                    )
                )
            }
        }
    }
}


fun formatTime(date: java.util.Date): String {
    val cal = java.util.Calendar.getInstance().apply { time = date }
    val hours = cal.get(java.util.Calendar.HOUR_OF_DAY)
    val minutes = cal.get(java.util.Calendar.MINUTE)
    // Formatea a HH:mm (por ejemplo: 09:05)
    return String.format("%02d:%02d", hours, minutes)
}

fun formatDate(date: java.util.Date): String {
    val sdf = java.text.SimpleDateFormat("EEE, d MMM yyyy", java.util.Locale("es", "ES"))
    return sdf.format(date).replaceFirstChar { it.uppercase() }
}