package com.asistente.planificador.ui.screens

import SelectionDate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asistente.core.domain.models.RecurrenceType
import com.asistente.core.domain.models.SlotType
import com.asistente.core.domain.models.TimeSlot
import com.asistente.planificador.ui.viewmodels.TimeSlotViewModel
import com.asistente.planificador.ui.viewmodels.toHourMinute
import com.asistente.planificador.ui.viewmodels.toTimeString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSlotForm(
    existingSlot: TimeSlot? = null,     // null = crear, not null = editar
    viewModel: TimeSlotViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()
    val selectionDate = remember { SelectionDate() }

    // Si hay franja para editar, cargarla una vez
    LaunchedEffect(existingSlot) {
        if (existingSlot != null) viewModel.loadForEdit(existingSlot)
    }

    var showStartTimePicker   by remember { mutableStateOf(false) }
    var showEndTimePicker     by remember { mutableStateOf(false) }
    var showRangeStartPicker  by remember { mutableStateOf(false) }
    var showRangeEndPicker    by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(vertical = 8.dp).height(68.dp),
                title = {
                    Box(Modifier.fillMaxHeight(), contentAlignment = Alignment.CenterStart) {
                        Text(
                            if (state.isEditing) "Editar franja" else "Nueva franja",
                            fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.fillMaxHeight().padding(start = 8.dp)) {
                        Icon(Icons.Rounded.Close, null, modifier = Modifier.size(28.dp), tint = Terciario)
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.saveTimeSlot(onSuccess = onBack) },
                        colors = ButtonDefaults.buttonColors(containerColor = Primario, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.padding(end = 16.dp).height(32.dp).align(Alignment.CenterVertically),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            "Guardar", fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                            style = LocalTextStyle.current.copy(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Nombre ────────────────────────────────────────────────────
            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.onNameChanged(it) },
                leadingIcon = { Icon(Icons.Default.Create, null, tint = Primario, modifier = Modifier.size(24.dp)) },
                placeholder = {
                    Text("Nombre de la franja", fontSize = 22.sp, fontWeight = FontWeight.Medium, color = Terciario.copy(0.6f))
                },
                textStyle = LocalTextStyle.current.copy(fontSize = 22.sp, fontWeight = FontWeight.Medium, color = Color.Black),
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Secundario, unfocusedContainerColor = Secundario,
                    focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent,
                    cursorColor = Primario
                )
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(thickness = 0.5.dp)

            // ── Tipo de recurrencia ───────────────────────────────────────
            SectionLabel("Tipo de recurrencia", Icons.Default.Repeat)
            RecurrenceTabs(
                selected = state.recurrenceType,
                onSelected = { viewModel.onRecurrenceTypeChanged(it) }
            )

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(thickness = 0.5.dp)

            // ── Paridad (solo EVEN/ODD) ────────────────────────────────────
            if (state.recurrenceType == RecurrenceType.EVEN_WEEKS ||
                state.recurrenceType == RecurrenceType.ODD_WEEKS) {
                SectionLabel("¿Qué semanas?", Icons.Default.CalendarViewWeek)
                WeekParitySelector(
                    selected = state.recurrenceType,
                    onSelected = { viewModel.onRecurrenceTypeChanged(it) }
                )
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(thickness = 0.5.dp)
            }

            // ── Rango de fechas (solo DATE_RANGE) ──────────────────────────
            if (state.recurrenceType == RecurrenceType.DATE_RANGE) {
                SectionLabel("Período activo", Icons.Default.DateRange)
                DateRangeSelector(
                    startDate = state.rangeStart,
                    endDate   = state.rangeEnd,
                    onStartClick = { showRangeStartPicker = true },
                    onEndClick   = { showRangeEndPicker = true }
                )
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(thickness = 0.5.dp)
            }

            // ── Día único (solo SINGLE_DAY) ────────────────────────────────
            if (state.recurrenceType == RecurrenceType.SINGLE_DAY) {
                SectionLabel("Fecha", Icons.Default.Event)
                SingleDayPicker(
                    date = state.rangeStart,
                    onClick = { showRangeStartPicker = true }
                )
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(thickness = 0.5.dp)
            }

            // ── Días de la semana (no aplica en SINGLE_DAY) ───────────────
            if (state.recurrenceType != RecurrenceType.SINGLE_DAY) {
                SectionLabel("Días de la semana", Icons.Default.ViewWeek)
                DayOfWeekSelector(
                    activeDays = state.daysOfWeek,
                    onDayToggled = { viewModel.onDayToggled(it) }
                )
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(thickness = 0.5.dp)
            }

            // ── Horario ───────────────────────────────────────────────────
            SectionLabel("Horario", Icons.Default.AccessTime)
            TimeRangeSelector(
                startMinute = state.startMinuteOfDay,
                endMinute   = state.endMinuteOfDay,
                onStartClick = { showStartTimePicker = true },
                onEndClick   = { showEndTimePicker = true }
            )

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(thickness = 0.5.dp)

            Spacer(Modifier.height(16.dp))

            // ── Error ──────────────────────────────────────────────────────
            if (state.error != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFEBEE),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, tint = Color(0xFFE53935), modifier = Modifier.size(16.dp))
                        Text(state.error!!, fontSize = 13.sp, color = Color(0xFFE53935))
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }

        // ── Diálogos de hora ───────────────────────────────────────────────
        if (showStartTimePicker) {
            val (h, m) = state.startMinuteOfDay.toHourMinute()
            selectionDate.TimePickerModal(
                initialHour = h, initialMinute = m,
                onDismiss = { showStartTimePicker = false },
                onConfirm = { hour, min -> viewModel.onStartTimeChanged(hour, min); showStartTimePicker = false }
            )
        }
        if (showEndTimePicker) {
            val (h, m) = state.endMinuteOfDay.toHourMinute()
            selectionDate.TimePickerModal(
                initialHour = h, initialMinute = m,
                onDismiss = { showEndTimePicker = false },
                onConfirm = { hour, min -> viewModel.onEndTimeChanged(hour, min); showEndTimePicker = false }
            )
        }
        if (showRangeStartPicker) {
            selectionDate.DatePickerModal(
                initialDateMillis = state.rangeStart?.time,
                onDismiss = { showRangeStartPicker = false },
                onConfirm = { millis -> millis?.let { viewModel.onRangeStartChanged(it) }; showRangeStartPicker = false }
            )
        }
        if (showRangeEndPicker) {
            selectionDate.DatePickerModal(
                initialDateMillis = state.rangeEnd?.time,
                onDismiss = { showRangeEndPicker = false },
                onConfirm = { millis -> millis?.let { viewModel.onRangeEndChanged(it) }; showRangeEndPicker = false }
            )
        }
    }
}

// ── Label de sección ──────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, null, tint = Primario, modifier = Modifier.size(22.dp))
        Text(text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
    }
}

// ── Tabs de recurrencia ───────────────────────────────────────────────────────

@Composable
private fun RecurrenceTabs(selected: RecurrenceType, onSelected: (RecurrenceType) -> Unit) {
    val options = listOf(
        RecurrenceType.WEEKLY      to "Semanal",
        RecurrenceType.EVEN_WEEKS  to "Par / Impar",
        RecurrenceType.DATE_RANGE  to "Rango",
        RecurrenceType.SINGLE_DAY  to "Día único"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Secundario, RoundedCornerShape(12.dp))
            .padding(3.dp)
    ) {
        Row {
            options.forEach { (type, label) ->
                val isSelected = selected == type ||
                        (type == RecurrenceType.EVEN_WEEKS && selected == RecurrenceType.ODD_WEEKS)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isSelected) Color.White else Color.Transparent)
                        .clickable { onSelected(type) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) Primario else Terciario
                    )
                }
            }
        }
    }
}

// ── Selector paridad semanas ──────────────────────────────────────────────────

@Composable
private fun WeekParitySelector(selected: RecurrenceType, onSelected: (RecurrenceType) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(
            RecurrenceType.EVEN_WEEKS to Pair("📅", "Semanas pares\n2ª, 4ª... de cada mes"),
            RecurrenceType.ODD_WEEKS  to Pair("📆", "Semanas impares\n1ª, 3ª... de cada mes")
        ).forEach { (type, pair) ->
            val isSelected = selected == type
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelected(type) },
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) Color(0xFFF3E5E2) else Color(0xFFF9F9F9),
                border = if (isSelected)
                    androidx.compose.foundation.BorderStroke(1.5.dp, Primario)
                else
                    androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(pair.first, fontSize = 24.sp)
                    Text(
                        pair.second.split("\n")[0],
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Primario else Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        pair.second.split("\n")[1],
                        fontSize = 11.sp,
                        color = Terciario,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ── Selector rango fechas ─────────────────────────────────────────────────────

@Composable
private fun DateRangeSelector(
    startDate: java.util.Date?,
    endDate: java.util.Date?,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit
) {
    val fmt = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale("es", "ES"))

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DateBox(
            label = "Desde",
            value = startDate?.let { fmt.format(it) } ?: "Seleccionar",
            onClick = onStartClick,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Default.ArrowForward, null, tint = Terciario, modifier = Modifier.size(18.dp))
        DateBox(
            label = "Hasta",
            value = endDate?.let { fmt.format(it) } ?: "Seleccionar",
            onClick = onEndClick,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(8.dp))
    Surface(shape = RoundedCornerShape(10.dp), color = Secundario) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, null, tint = Terciario, modifier = Modifier.size(14.dp))
            Text(
                "Fuera de este período, la franja se ignorará.",
                fontSize = 11.sp, color = Terciario
            )
        }
    }
}

// ── Selector día único ────────────────────────────────────────────────────────

@Composable
private fun SingleDayPicker(date: java.util.Date?, onClick: () -> Unit) {
    val fmt = java.text.SimpleDateFormat("d 'de' MMMM, yyyy", java.util.Locale("es", "ES"))
    DateBox(
        label = "Día seleccionado",
        value = date?.let { fmt.format(it) } ?: "Toca para seleccionar",
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        highlighted = date != null
    )
}

@Composable
private fun DateBox(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = if (highlighted) Color(0xFFF3E5E2) else Secundario,
        border = if (highlighted)
            androidx.compose.foundation.BorderStroke(1.5.dp, Primario) else null
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                label.uppercase(),
                fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                color = if (highlighted) Primario else Terciario,
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                value,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (highlighted) Primario else Color.Black
            )
        }
    }
}

// ── Selector días de la semana ────────────────────────────────────────────────

@Composable
private fun DayOfWeekSelector(activeDays: List<Int>, onDayToggled: (Int) -> Unit) {
    val days = listOf(1 to "L", 2 to "M", 3 to "X", 4 to "J", 5 to "V", 6 to "S", 7 to "D")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        days.forEach { (num, label) ->
            val active = activeDays.contains(num)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (active) Color(0xFFF3E5E2) else Secundario)
                    .then(
                        if (active) Modifier.border(1.5.dp, Primario, RoundedCornerShape(10.dp))
                        else Modifier
                    )
                    .clickable { onDayToggled(num) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (active) Primario else Terciario
                )
            }
        }
    }
}

// ── Selector hora inicio/fin ──────────────────────────────────────────────────

@Composable
private fun TimeRangeSelector(
    startMinute: Int,
    endMinute: Int,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimePickerBox(label = "Inicio", time = startMinute.toTimeString(), onClick = onStartClick, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ArrowForward, null, tint = Terciario, modifier = Modifier.size(18.dp))
        TimePickerBox(label = "Fin", time = endMinute.toTimeString(), onClick = onEndClick, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TimePickerBox(label: String, time: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = Secundario
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Terciario, letterSpacing = 0.5.sp)
            Text(time, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Primario)
        }
    }
}