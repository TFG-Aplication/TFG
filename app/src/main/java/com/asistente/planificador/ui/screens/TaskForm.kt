package com.asistente.planificador.ui.screens

import CalendarField
import CalendarSelector
import SelectionDate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asistente.planificador.ui.screens.tools.*
import com.asistente.planificador.ui.viewmodels.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskForm(
    taskId   : String? = null,
    viewModel: TaskViewModel = hiltViewModel(),
    onBack   : () -> Unit,
    onDelete : () -> Unit = onBack
) {
    val uiState   by viewModel.uiState.collectAsState()
    val calendars by viewModel.calendarsList.collectAsStateWithLifecycle()
    val category  by viewModel.categoryList.collectAsStateWithLifecycle()

    var showStartTimePicker      by remember { mutableStateOf(false) }
    var showEndTimePicker        by remember { mutableStateOf(false) }
    var expandedCategorySelector by remember { mutableStateOf(false) }
    var expandedCalendarSelector by remember { mutableStateOf(false) }
    var showStartDatePicker      by remember { mutableStateOf(false) }
    var showEndDatePicker        by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val isEditMode    = uiState.isEditMode
    val selectionDate = remember { SelectionDate() }
    val dateFmt       = remember { SimpleDateFormat("d MMM yyyy", Locale("es", "ES")) }
    val timeFmt       = remember { SimpleDateFormat("HH:mm", Locale("es", "ES")) }

    Scaffold(
        containerColor = Secundario,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
            ) {
                // ── Nav bar ───────────────────────────────────────────────────
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.Close, null, tint = Terciario, modifier = Modifier.size(22.dp))
                    }

                    Text(
                        text       = if (isEditMode) "Editar tarea" else "Nueva tarea",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 17.sp,
                        color      = Color.Black,
                        modifier   = Modifier.weight(1f),
                        textAlign  = TextAlign.Center
                    )

                    Spacer(Modifier.weight(1f))
                    if (isEditMode) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Rounded.Delete, null, tint = ColorDestructive.copy(alpha = 0.7f), modifier = Modifier.size(22.dp))
                        }
                        EditActionButton(
                            label          = "Actualizar",
                            contentPadding = PaddingValues(end = 12.dp),
                            onClick        = { viewModel.updateTask(onSuccess = onBack)  }
                        )
                    }
                    else {
                        TextButton(
                            onClick        = { viewModel.saveTask(onSuccess = onBack) },
                            contentPadding = PaddingValues(end = 12.dp)
                        ) {
                            Text(
                                "Guardar",
                                color      = Primario,
                                fontSize   = 17.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                }

                // ── Cabecera expandida ────────────────────────────────────────
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    OutlinedTextField(
                        value         = uiState.name,
                        onValueChange = { viewModel.onNameChanged(it) },
                        placeholder   = {
                            Text(
                                "Nombre de la tarea",
                                fontSize   = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Terciario.copy(alpha = 0.5f)
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.Black
                        ),
                        modifier   = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors     = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor   = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor      = Color.Transparent,
                            unfocusedBorderColor    = Color.Transparent,
                            cursorColor             = Primario
                        )
                    )
                    if (uiState.calendar != null) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, null, tint = Terciario, modifier = Modifier.size(12.dp))
                            Text(uiState.calendar!!.name, fontSize = 12.sp, color = Terciario)
                        }
                    }
                }
            }
        }
    ) { pad ->
        Column(
            modifier            = Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // ── Error ─────────────────────────────────────────────────────────
            if (uiState.error != null) {
                AppBanner(text = uiState.error!!, style = BannerStyle.WARNING)
            }

            // ── Calendario ────────────────────────────────────────────────────
            IosGroupCard {
                CalendarField(
                    selectedCalendar = uiState.calendar,
                    onClick          = { expandedCalendarSelector = true }
                )
            }

            // ── Fechas ────────────────────────────────────────────────────────
            IosGroupCard {
                IosRow(icon = Icons.Default.CalendarMonth, iconTint = IconFecha, label = "Inicio") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { showStartDatePicker = true }, contentPadding = PaddingValues(horizontal = 6.dp)) {
                            Text(dateFmt.format(uiState.initDate), color = Primario, fontSize = 14.sp)
                        }
                        if (!uiState.isAllDay) {
                            TextButton(onClick = { showStartTimePicker = true }, contentPadding = PaddingValues(horizontal = 6.dp)) {
                                Text(timeFmt.format(uiState.initDate), color = Primario, fontSize = 14.sp)
                            }
                        }
                    }
                }
                IosDivider()
                IosRow(icon = Icons.Default.Schedule, iconTint = IconFecha, label = "Fin") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { showEndDatePicker = true }, contentPadding = PaddingValues(horizontal = 6.dp)) {
                            Text(dateFmt.format(uiState.finishDate), color = Primario, fontSize = 14.sp)
                        }
                        if (!uiState.isAllDay) {
                            TextButton(onClick = { showEndTimePicker = true }, contentPadding = PaddingValues(horizontal = 6.dp)) {
                                Text(timeFmt.format(uiState.finishDate), color = Primario, fontSize = 14.sp)
                            }
                        }
                    }
                }
                IosDivider()
                IosRow(icon = Icons.Default.WbSunny, iconTint = IconDiary, label = "Todo el día") {
                    Switch(
                        checked         = uiState.isAllDay,
                        onCheckedChange = { viewModel.allDay(it) },
                        colors          = SwitchDefaults.colors(
                            checkedThumbColor   = Color.White,
                            checkedTrackColor   = Primario,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Terciario.copy(alpha = 0.35f),
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            // ── Categoría ─────────────────────────────────────────────────────
            IosGroupCard {
                CategoryField(
                    selectedCategory = uiState.category,
                    onClick          = { expandedCategorySelector = true }
                )
            }

            // ── Alarmas ───────────────────────────────────────────────────────
            IosGroupCard {
                AlertSelector(
                    initDate        = uiState.initDate,
                    alerts          = uiState.alerts,
                    onAlertsChanged = { viewModel.onAlertsChanged(it) }
                )
            }

            // ── Repetición ────────────────────────────────────────────────────
            IosGroupCard {
                IosRow(icon = Icons.Default.Repeat, iconTint = IconRepeticion, label = "No se repite") {
                    Icon(Icons.Default.ChevronRight, null, tint = Terciario, modifier = Modifier.size(18.dp))
                }
            }

            // ── Bloquear franja ───────────────────────────────────────────────
            IosGroupCard {
                IosRow(icon = Icons.Default.Block, iconTint = IconFranja, label = "Bloquear franja") {
                    Switch(
                        checked         = uiState.blockTimeSlot,
                        onCheckedChange = { viewModel.onBlockTimeSlotChanged(it) },
                        colors          = SwitchDefaults.colors(
                            checkedThumbColor   = Color.White,
                            checkedTrackColor   = Primario,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Terciario.copy(alpha = 0.35f),
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
                if (uiState.blockTimeSlot) {
                    IosDivider()
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        AppBanner(
                            text  = "Reserva este tiempo para que el asistente no planifique nada aquí.",
                            style = BannerStyle.INFO
                        )
                    }
                }
            }

            // ── Notas ─────────────────────────────────────────────────────────
            IosGroupCard {
                IosRow(icon = Icons.Default.Description, iconTint = IconNotas, label = "Notas", trailingContent = null)
                IosDivider()
                OutlinedTextField(
                    value         = uiState.notes,
                    onValueChange = { viewModel.onNoteChanged(it) },
                    placeholder   = {
                        Text("Agregar descripción aquí...", color = Terciario.copy(alpha = 0.7f), fontSize = 15.sp)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor      = Color.Transparent,
                        unfocusedBorderColor    = Color.Transparent,
                        cursorColor             = Primario,
                        focusedTextColor        = Color.Black,
                        unfocusedTextColor      = Color.Black
                    )
                )
            }



            Spacer(Modifier.height(16.dp))
        }

        // ── Selectores / Pickers ──────────────────────────────────────────────
        if (expandedCategorySelector) {
            CategorySelector(
                categories         = category,
                onCategorySelected = { viewModel.onCategoryChanged(it) },
                onDismiss          = { expandedCategorySelector = false }
            )
        }
        if (expandedCalendarSelector) {
            CalendarSelector(
                calendars         = calendars,
                onCalendarChanged = { viewModel.onCalendarChanged(it) },
                onDismiss         = { expandedCalendarSelector = false },
                selectedCalendar  = uiState.calendar
            )
        }
        if (showStartDatePicker) {
            selectionDate.DatePickerModal(
                initialDateMillis = uiState.initDate.time,
                onDismiss         = { showStartDatePicker = false },
                onConfirm         = { millis ->
                    millis?.let { viewModel.onDateChanged(it, true) }
                    showStartDatePicker = false
                    if (!uiState.isAllDay) showStartTimePicker = true
                }
            )
        }
        if (showStartTimePicker) {
            val cal = java.util.Calendar.getInstance().apply { time = uiState.initDate }
            selectionDate.TimePickerModal(
                initialHour   = cal.get(java.util.Calendar.HOUR_OF_DAY),
                initialMinute = cal.get(java.util.Calendar.MINUTE),
                onDismiss     = { showStartTimePicker = false },
                onConfirm     = { h, m -> viewModel.onTimeChanged(h, m, true); showStartTimePicker = false }
            )
        }
        if (showEndDatePicker) {
            selectionDate.DatePickerModal(
                initialDateMillis = uiState.finishDate.time,
                onDismiss         = { showEndDatePicker = false },
                onConfirm         = { millis ->
                    millis?.let { viewModel.onDateChanged(it, false) }
                    showEndDatePicker = false
                    if (!uiState.isAllDay) showEndTimePicker = true
                }
            )
        }
        if (showEndTimePicker) {
            val cal = java.util.Calendar.getInstance().apply { time = uiState.finishDate }
            selectionDate.TimePickerModal(
                initialHour   = cal.get(java.util.Calendar.HOUR_OF_DAY),
                initialMinute = cal.get(java.util.Calendar.MINUTE),
                onDismiss     = { showEndTimePicker = false },
                onConfirm     = { h, m -> viewModel.onTimeChanged(h, m, false); showEndTimePicker = false }
            )
        }
        if (showDeleteConfirm) {
            DeleteConfirmDialog(
                title     = "¿Eliminar tarea?",
                message = "Esta acción no se puede deshacer",
                confirmLabel = "Eliminar",
                onConfirm = { viewModel.deleteTask(onSuccess = onDelete) },
                onDismiss = { showDeleteConfirm = false }
            )
        }
    }
}