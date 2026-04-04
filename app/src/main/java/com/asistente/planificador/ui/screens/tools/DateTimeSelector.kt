import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.asistente.planificador.ui.screens.tools.Primario
import com.asistente.planificador.ui.screens.tools.Secundario

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

@Composable
fun DateTimeSelector(
    initDate: java.util.Date,
    finishDate: java.util.Date,
    isAllDay: Boolean,
    onAllDayChanged: (Boolean) -> Unit,
    onStartDateClick: () -> Unit,
    onStartTimeClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onEndTimeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    ) {
        // --- SWITCH TODO EL DÍA ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = Primario,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Todo el día",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                color = Color.Black
            )
            Switch(
                checked = isAllDay,
                onCheckedChange = onAllDayChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Primario,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFA6A6A6),
                    uncheckedBorderColor = Color.Transparent,
                    checkedBorderColor = Color.Transparent

                )
            )
        }

        // --- FILAS INICIO + FIN con conector visual ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Columna izquierda: círculo + línea punteada + flecha
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(32.dp)
            ) {
                // Círculo inicio
                Icon(
                    imageVector = Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = Primario,
                    modifier = Modifier.size(12.dp)
                )
                // Línea punteada
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .width(1.5.dp)
                            .height(5.dp)
                            .padding(vertical = 1.dp)
                            .background(Primario.copy(alpha = 0.5f))
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                }
                // Flecha fin
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = Primario,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Columna derecha: textos
            Column(modifier = Modifier.weight(1f)) {
                // Fila inicio
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatDate(initDate),
                        modifier = Modifier.weight(1f).clickable { onStartDateClick() },
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    if (!isAllDay) {
                        Text(
                            text = formatTime(initDate),
                            modifier = Modifier.clickable { onStartTimeClick() },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                // Fila fin
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatDate(finishDate),
                        modifier = Modifier.weight(1f).clickable { onEndDateClick() },
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    if (!isAllDay) {
                        Text(
                            text = formatTime(finishDate),
                            modifier = Modifier.clickable { onEndTimeClick() },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
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