package com.asistente.planificador.ui.screens

import CalendarField
import CalendarSelector
import SelectionDate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asistente.planificador.ui.screens.tools.CategoryField
import com.asistente.planificador.ui.screens.tools.CategorySelector
import com.asistente.planificador.ui.viewmodels.ActivityViewModel
import com.asistente.planificador.ui.screens.tools.Primario
import com.asistente.planificador.ui.screens.tools.Secundario
import com.asistente.planificador.ui.screens.tools.Terciario
import formatDate
import formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityForm(
    viewModel: ActivityViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val calendars by viewModel.calendarsList.collectAsStateWithLifecycle()
    val categories by viewModel.categoryList.collectAsStateWithLifecycle()

    var expandedCategorySelector by remember { mutableStateOf(false) }
    var expandedCalendarSelector by remember { mutableStateOf(false) }
    var showEarliestDatePicker by remember { mutableStateOf(false) }
    var showEarliestTimePicker by remember { mutableStateOf(false) }
    var showDeadlineDatePicker by remember { mutableStateOf(false) }
    var showDeadlineTimePicker by remember { mutableStateOf(false) }
    var expandedPriority by remember { mutableStateOf(false) }

    val selectionDate = remember { SelectionDate() }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(vertical = 10.dp).height(72.dp),
                title = {
                    Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.CenterStart) {
                        Text(
                            text = "Nueva Actividad",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 20.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxHeight().padding(start = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Cerrar",
                            modifier = Modifier.size(30.dp),
                            tint = Terciario
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.saveActivity()
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primario,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 0.dp),
                        modifier = Modifier
                            .padding(end = 21.dp, top = 1.dp)
                            .height(28.dp)
                            .align(Alignment.CenterVertically)
                    ) {
                        Text(
                            text = "Guardar",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            style = LocalTextStyle.current.copy(
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Terciario,
                    navigationIconContentColor = Terciario
                )
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(horizontal = 18.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Nombre ──────────────────────────────────────────────────────
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onNameChanged(it) },
                leadingIcon = {
                    Icon(Icons.Default.Create, null, tint = Primario, modifier = Modifier.size(26.dp))
                },
                placeholder = {
                    Text("Agregar título", fontSize = 30.sp, fontWeight = FontWeight.Medium, color = Terciario.copy(alpha = 0.6f))
                },
                textStyle = LocalTextStyle.current.copy(color = Terciario, fontSize = 30.sp, fontWeight = FontWeight.Medium),
                modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Secundario,
                    unfocusedContainerColor = Secundario,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Terciario,
                    focusedTextColor = Terciario,
                    unfocusedTextColor = Terciario
                )
            )

            HorizontalDivider(thickness = 0.5.dp)

            // ── Calendario ──────────────────────────────────────────────────
            CalendarField(
                selectedCalendar = uiState.calendar,
                onClick = { expandedCalendarSelector = true }
            )

            HorizontalDivider(thickness = 0.5.dp)

            // ── Duración ────────────────────────────────────────────────────
            DurationSelector(
                durationMinutes = uiState.durationMinutes,
                onDurationChanged = { viewModel.onDurationChanged(it) }
            )

            HorizontalDivider(thickness = 0.5.dp)

            // ── Fecha más temprana (earliest_start) ─────────────────────────
            SectionDateField(
                icon = Icons.Default.PlayArrow,
                label = "Desde cuándo",
                date = uiState.earliestStart,
                isAllDay = false,
                onDateClick = { showEarliestDatePicker = true },
                onTimeClick = { showEarliestTimePicker = true }
            )

            HorizontalDivider(thickness = 0.5.dp)

            // ── Fecha límite (deadline) ─────────────────────────────────────
            SectionDateField(
                icon = Icons.Default.Flag,
                label = "Fecha límite",
                date = uiState.deadline,
                isAllDay = false,
                onDateClick = { showDeadlineDatePicker = true },
                onTimeClick = { showDeadlineTimePicker = true }
            )

            HorizontalDivider(thickness = 0.5.dp)

            // ── Prioridad ───────────────────────────────────────────────────
            PrioritySelector(
                priority = uiState.priority,
                expanded = expandedPriority,
                onExpandedChange = { expandedPriority = it },
                onPrioritySelected = { viewModel.onPriorityChanged(it) }
            )

            HorizontalDivider(thickness = 0.5.dp)

            // ── Categoría ───────────────────────────────────────────────────
            CategoryField(
                selectedCategory = uiState.category,
                onClick = { expandedCategorySelector = true }
            )

            HorizontalDivider(thickness = 0.5.dp)

            // ── Notas ───────────────────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                    Icon(Icons.Default.Description, null, tint = Primario, modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Agregar descripción", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                }
                OutlinedTextField(
                    value = uiState.notes ?: "",
                    onValueChange = { viewModel.onNotesChanged(it) },
                    placeholder = { Text("Agregar descripción aquí...", color = Terciario.copy(alpha = 0.7f), fontSize = 16.sp) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Secundario,
                        unfocusedContainerColor = Secundario,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = Primario,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
            }

            if (uiState.error != null) {
                Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }

        // ── Diálogos ────────────────────────────────────────────────────────
        if (expandedCategorySelector) {
            CategorySelector(
                categories = categories,
                onCategorySelected = { viewModel.onCategoryChanged(it) },
                onDismiss = { expandedCategorySelector = false }
            )
        }
        if (expandedCalendarSelector) {
            CalendarSelector(
                calendars = calendars,
                onCalendarChanged = { viewModel.onCalendarChanged(it) },
                onDismiss = { expandedCalendarSelector = false },
                selectedCalendar = uiState.calendar
            )
        }

        // Earliest start
        if (showEarliestDatePicker) {
            selectionDate.DatePickerModal(
                initialDateMillis = uiState.earliestStart.time,
                onDismiss = { showEarliestDatePicker = false },
                onConfirm = { millis ->
                    millis?.let { viewModel.onEarliestStartDateChanged(it) }
                    showEarliestDatePicker = false
                    showEarliestTimePicker = true
                }
            )
        }
        if (showEarliestTimePicker) {
            val cal = java.util.Calendar.getInstance().apply { time = uiState.earliestStart }
            selectionDate.TimePickerModal(
                initialHour = cal.get(java.util.Calendar.HOUR_OF_DAY),
                initialMinute = cal.get(java.util.Calendar.MINUTE),
                onDismiss = { showEarliestTimePicker = false },
                onConfirm = { h, m ->
                    viewModel.onEarliestStartTimeChanged(h, m)
                    showEarliestTimePicker = false
                }
            )
        }

        // Deadline
        if (showDeadlineDatePicker) {
            selectionDate.DatePickerModal(
                initialDateMillis = uiState.deadline.time,
                onDismiss = { showDeadlineDatePicker = false },
                onConfirm = { millis ->
                    millis?.let { viewModel.onDeadlineDateChanged(it) }
                    showDeadlineDatePicker = false
                    showDeadlineTimePicker = true
                }
            )
        }
        if (showDeadlineTimePicker) {
            val cal = java.util.Calendar.getInstance().apply { time = uiState.deadline }
            selectionDate.TimePickerModal(
                initialHour = cal.get(java.util.Calendar.HOUR_OF_DAY),
                initialMinute = cal.get(java.util.Calendar.MINUTE),
                onDismiss = { showDeadlineTimePicker = false },
                onConfirm = { h, m ->
                    viewModel.onDeadlineTimeChanged(h, m)
                    showDeadlineTimePicker = false
                }
            )
        }
    }
}

// ── Selector de duración ─────────────────────────────────────────────────────
@Composable
private fun DurationSelector(
    durationMinutes: Long,
    onDurationChanged: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val presets = listOf(
        15L to "15 minutos",
        30L to "30 minutos",
        45L to "45 minutos",
        60L to "1 hora",
        90L to "1 hora 30 min",
        120L to "2 horas",
        180L to "3 horas",
        240L to "4 horas",
        480L to "8 horas"
    )

    fun formatDuration(minutes: Long): String = when {
        minutes < 60 -> "$minutes minutos"
        minutes % 60 == 0L -> "${minutes / 60} hora${if (minutes / 60 > 1) "s" else ""}"
        else -> "${minutes / 60}h ${minutes % 60}min"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 10.dp)
            .clickable { expanded = true },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Timer, null, tint = Primario, modifier = Modifier.size(26.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text("Duración", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.Black, modifier = Modifier.weight(1f))
        Text(formatDuration(durationMinutes), fontSize = 16.sp, color = Terciario, fontWeight = FontWeight.Medium)
        Icon(Icons.Default.KeyboardArrowDown, null, tint = Terciario, modifier = Modifier.size(24.dp))

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            presets.forEach { (minutes, label) ->
                DropdownMenuItem(
                    text = { Text(label, color = if (minutes == durationMinutes) Primario else Color.Black) },
                    onClick = {
                        onDurationChanged(minutes)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ── Campo de fecha individual (sin "todo el día") ────────────────────────────
@Composable
private fun SectionDateField(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    date: java.util.Date,
    isAllDay: Boolean,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Primario, modifier = Modifier.size(26.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.Black, modifier = Modifier.weight(1f))
        Text(
            text = formatDate(date),
            fontSize = 15.sp,
            color = Primario,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onDateClick() }
        )
        if (!isAllDay) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formatTime(date),
                fontSize = 15.sp,
                color = Primario,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onTimeClick() }
            )
        }
    }
}

// ── Selector de prioridad ────────────────────────────────────────────────────
@Composable
private fun PrioritySelector(
    priority: Int,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onPrioritySelected: (Int) -> Unit
) {
    val options = listOf(1 to "Baja", 2 to "Media", 3 to "Alta")
    val priorityColor = when (priority) {
        3 -> Color(0xFFE53935) // rojo — alta
        2 -> Color(0xFFFB8C00) // naranja — media
        else -> Color(0xFF43A047) // verde — baja
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 10.dp)
            .clickable { onExpandedChange(true) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.PriorityHigh, null, tint = Primario, modifier = Modifier.size(26.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text("Prioridad", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.Black, modifier = Modifier.weight(1f))
        Text(
            text = options.first { it.first == priority }.second,
            fontSize = 16.sp,
            color = priorityColor,
            fontWeight = FontWeight.Bold
        )
        Icon(Icons.Default.KeyboardArrowDown, null, tint = Terciario, modifier = Modifier.size(24.dp))

        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            options.forEach { (value, label) ->
                val color = when (value) {
                    3 -> Color(0xFFE53935)
                    2 -> Color(0xFFFB8C00)
                    else -> Color(0xFF43A047)
                }
                DropdownMenuItem(
                    text = { Text(label, color = color, fontWeight = FontWeight.Medium) },
                    onClick = {
                        onPrioritySelected(value)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}