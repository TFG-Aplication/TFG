package com.asistente.planificador.ui.screens

import DateTimeSelector
import SelectionDate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asistente.planificador.ui.screens.tools.AlertSelector
import com.asistente.planificador.ui.screens.tools.CalendarField
import com.asistente.planificador.ui.screens.tools.CalendarSelector
import com.asistente.planificador.ui.screens.tools.CategoryField
import com.asistente.planificador.ui.screens.tools.CategorySelector
import com.asistente.planificador.ui.viewmodels.TaskViewModel

val Primario = Color(0xFFAC5343)
val Secundario = Color(0xFFEFEFEF)
val Terciario = Color(0xFFA6A6A6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskForm(
    taskId: String? = null,
    viewModel: TaskViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val calendars by viewModel.calendarsList.collectAsStateWithLifecycle()
    val category by viewModel.categoryList.collectAsStateWithLifecycle()
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var expandedCategorySelector by remember { mutableStateOf(false) }
    var expandedCalendarSelector by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val isEditMode = uiState.isEditMode

    val selectionDate = remember { SelectionDate() }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .height(72.dp),
                title = {
                    Box(
                        modifier = Modifier.fillMaxHeight(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = if (isEditMode) "Editar Tarea" else "Nueva Tarea",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 20.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(start = 14.dp)
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
                    if (isEditMode) {
                        IconButton(
                            onClick = {
                                viewModel.deleteTask(onSuccess = onBack)
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Eliminar",
                                tint = Color.Red.copy(alpha = 0.7f),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    // Botón guardar / actualizar
                    Button(
                        onClick = {
                            if (isEditMode) viewModel.updateTask(onSuccess = onBack)
                            else viewModel.saveTask(onSuccess = onBack)
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
                ),
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
            // Nombre
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onNameChanged(it) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = null,
                        tint = Primario,
                        modifier = Modifier.size(26.dp)
                    )
                },
                placeholder = {
                    Text(
                        text = "Agregar título",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Medium,
                        color = Terciario.copy(alpha = 0.6f)
                    )
                },
                textStyle = LocalTextStyle.current.copy(
                    color = Terciario,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
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

            // Selector de calendario
            CalendarField(
                selectedCalendar = uiState.calendar,
                onClick = { expandedCalendarSelector = true }
            )

            HorizontalDivider(thickness = 0.5.dp)

            // Fecha y hora
            DateTimeSelector(
                initDate = uiState.initDate,
                finishDate = uiState.finishDate,
                isAllDay = uiState.isAllDay,
                onAllDayChanged = { viewModel.allDay(it) },
                onStartDateClick = { showStartDatePicker = true },
                onStartTimeClick = { showStartTimePicker = true },
                onEndDateClick = { showEndDatePicker = true },
                onEndTimeClick = { showEndTimePicker = true }
            )

            HorizontalDivider(thickness = 0.5.dp)

            // Selector de categorías
            CategoryField(
                selectedCategory = uiState.category,
                onClick = { expandedCategorySelector = true }
            )

            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            HorizontalDivider(thickness = 0.5.dp)

            // Alarmas
            AlertSelector(
                initDate = uiState.initDate,
                alerts = uiState.alerts,
                onAlertsChanged = { viewModel.onAlertsChanged(it) }
            )

            HorizontalDivider(thickness = 0.5.dp)

            // Repetición
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 10.dp)
                    .clickable { /* TODO: abrir selector de repetición */ },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = null,
                    tint = Primario,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "No se repite",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    color = Color.Black
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Terciario,
                    modifier = Modifier.size(24.dp)
                )
            }

            HorizontalDivider(thickness = 0.5.dp)

            // Bloquear franja
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = null,
                    tint = Primario,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bloquear franja",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = "Reserva este tiempo para que el asistente no planifique nada aquí.",
                        fontSize = 12.sp,
                        color = Terciario,
                        lineHeight = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = uiState.blockTimeSlot,
                    onCheckedChange = { viewModel.onBlockTimeSlotChanged(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Primario,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Terciario.copy(alpha = 0.35f)
                    )
                )
            }

            HorizontalDivider(thickness = 0.5.dp)

            // Notas
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = Primario,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Agregar descripción",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.onNoteChanged(it) },
                    placeholder = {
                        Text(
                            text = "Agregar descripción aquí...",
                            color = Terciario.copy(alpha = 0.7f),
                            fontSize = 16.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
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
        }

        // Diálogo categoría
        if (expandedCategorySelector) {
            CategorySelector(
                categories = category,
                onCategorySelected = { viewModel.onCategoryChanged(it) },
                onDismiss = { expandedCategorySelector = false }
            )
        }

        // Diálogo calendario
        if (expandedCalendarSelector) {
            CalendarSelector(
                calendars = calendars,
                onCalendarChanged = { viewModel.onCalendarChanged(it) },
                onDismiss = { expandedCalendarSelector = false },
                selectedCalendar = uiState.calendar
            )
        }

        // Diálogos de fecha/hora — inicio
        if (showStartDatePicker) {
            selectionDate.DatePickerModal(
                initialDateMillis = uiState.initDate.time,
                onDismiss = { showStartDatePicker = false },
                onConfirm = { millis ->
                    millis?.let { viewModel.onDateChanged(it, true) }
                    showStartDatePicker = false
                    if (!uiState.isAllDay) showStartTimePicker = true
                }
            )
        }

        if (showStartTimePicker) {
            val cal = java.util.Calendar.getInstance().apply { time = uiState.initDate }
            selectionDate.TimePickerModal(
                initialHour = cal.get(java.util.Calendar.HOUR_OF_DAY),
                initialMinute = cal.get(java.util.Calendar.MINUTE),
                onDismiss = { showStartTimePicker = false },
                onConfirm = { h, m ->
                    viewModel.onTimeChanged(h, m, true)
                    showStartTimePicker = false
                }
            )
        }

        // Diálogos de fecha/hora — fin
        if (showEndDatePicker) {
            selectionDate.DatePickerModal(
                initialDateMillis = uiState.finishDate.time,
                onDismiss = { showEndDatePicker = false },
                onConfirm = { millis ->
                    millis?.let { viewModel.onDateChanged(it, false) }
                    showEndDatePicker = false
                    if (!uiState.isAllDay) showEndTimePicker = true
                }
            )
        }

        if (showEndTimePicker) {
            val cal = java.util.Calendar.getInstance().apply { time = uiState.finishDate }
            selectionDate.TimePickerModal(
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

    if (uiState.error != null) {
        Text(
            text = uiState.error!!,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}