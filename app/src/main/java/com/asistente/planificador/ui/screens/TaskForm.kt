package com.asistente.planificador.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asistente.planificador.ui.viewmodels.TaskViewModel

/*
ACABAR DE HACER
 */

/**
 * CAMBIAR -> FECHAS Y HORAS / ESTADO INDEPENDIENTE PA HECHA Y HORA -> ESTADO FECHA PA VARIABLE
 * TODO EL DIA. SOLO CON ESO DEBERIA DE PODER CREARSE UNA ACT
* */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskForm(
    viewModel: TaskViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val calendars by viewModel.calendarsList.collectAsStateWithLifecycle()

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var expandedCalendarSelector by remember { mutableStateOf(false) }

    // Log para depuración
    LaunchedEffect(calendars) {
        android.util.Log.d("TaskViewModel", "Calendarios recibidos: ${calendars.size}")
    }
    LaunchedEffect(calendars) {
        if (calendars.isNotEmpty()) {
            android.util.Log.d("TaskViewModel", "¡DATO REAL! Nombre: ${calendars.first().name}")
        }
    }

    val selectionDate = remember { SelectionDate() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Tarea", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.Close, "Cerrar") }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.saveTask()
                        onBack()
                    }) {
                        Text("GUARDAR", fontWeight = FontWeight.Bold, color = Color(0xFFAC5343))
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onNameChanged(it) },
                label = { Text("¿Qué hay que hacer?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Selección de Calendario
            ExposedDropdownMenuBox(
                expanded = expandedCalendarSelector,
                onExpandedChange = { expandedCalendarSelector = !expandedCalendarSelector }
            ) {
                OutlinedTextField(
                    // Mostramos el nombre del seleccionado o un aviso si no hay nada
                    value = uiState.calendar?.name ?: "Seleccionar Calendario",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Calendario") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCalendarSelector) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = expandedCalendarSelector,
                    onDismissRequest = { expandedCalendarSelector = false }
                ) {
                    if (calendars.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Cargando calendarios...") },
                            onClick = { },
                            enabled = false
                        )
                    } else {
                        calendars.forEach { cal ->
                            DropdownMenuItem(
                                text = { Text(cal.name) },
                                onClick = {
                                    viewModel.onCalendarChanged(cal)
                                    expandedCalendarSelector = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

            // Selectores de Hora (Igual que antes...)
            ListItem(
                headlineContent = { Text("Empieza") },
                trailingContent = {
                    Text(text = formatTime(uiState.initDate), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFAC5343))
                },
                leadingContent = { Icon(Icons.Default.AccessTime, null) },
                modifier = Modifier.clickable { showStartTimePicker = true }
            )

            ListItem(
                headlineContent = { Text("Termina") },
                trailingContent = {
                    Text(text = formatTime(uiState.finishDate), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFAC5343))
                },
                leadingContent = { Icon(Icons.Default.Timer, null) },
                modifier = Modifier.clickable { showEndTimePicker = true }
            )

            if (uiState.error != null) {
                Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }

        // Diálogos de Hora
        if (showStartTimePicker) {
            val cal = java.util.Calendar.getInstance().apply { time = uiState.initDate }
            selectionDate.TimePickerDialog(
                initialHour = cal.get(java.util.Calendar.HOUR_OF_DAY),
                initialMinute = cal.get(java.util.Calendar.MINUTE),
                onDismiss = { showStartTimePicker = false },
                onConfirm = { h, m ->
                    viewModel.onTimeChanged(h, m, true)
                    showStartTimePicker = false
                }
            )
        }

        if (showEndTimePicker) {
            val cal = java.util.Calendar.getInstance().apply { time = uiState.finishDate }
            selectionDate.TimePickerDialog(
                initialHour = cal.get(java.util.Calendar.HOUR_OF_DAY),
                initialMinute = cal.get(java.util.Calendar.MINUTE),
                onDismiss = { showEndTimePicker = false },
                onConfirm = { h, m ->
                    viewModel.onTimeChanged(h, m, false)
                    showEndTimePicker = false
                }
            )
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