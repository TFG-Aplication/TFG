package com.asistente.planificador.ui.screens

import SelectionDate
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import android.R.attr.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asistente.core.domain.models.RecurrenceType
import com.asistente.core.domain.models.TimeSlot
import com.asistente.planificador.ui.screens.tools.*
import com.asistente.planificador.ui.viewmodels.TimeSlotEvent
import com.asistente.planificador.ui.viewmodels.TimeSlotViewModel
import com.asistente.planificador.ui.viewmodels.toHourMinute
import com.asistente.planificador.ui.viewmodels.toTimeString
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSlotForm(
    editSlotId: String? = null,
    viewModel   : TimeSlotViewModel = hiltViewModel(),
    onBack      : () -> Unit
) {
    val state         by viewModel.formState.collectAsStateWithLifecycle()
    val selectionDate  = remember { SelectionDate() }

    LaunchedEffect(editSlotId) {
        if (editSlotId != null) viewModel.loadForEdit(editSlotId)
        else viewModel.resetForm()
    }

    var showStartTimePicker  by remember { mutableStateOf(false) }
    var showEndTimePicker    by remember { mutableStateOf(false) }
    var showRangeStartPicker by remember { mutableStateOf(false) }
    var showRangeEndPicker   by remember { mutableStateOf(false) }
    var pendingWarnings      by remember { mutableStateOf<List<String>?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is TimeSlotEvent.SaveSuccess      -> onBack()
                is TimeSlotEvent.SaveWithWarnings -> pendingWarnings = event.warnings
                is TimeSlotEvent.Error            -> {}
            }
        }
    }

    Scaffold(
        containerColor = Secundario,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
            ) {
                // ── Barra de navegación (estilo TaskView) ─────────────────────
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón cerrar — izquierda
                    TextButton(
                        onClick        = { viewModel.resetForm(); onBack() },
                        contentPadding = PaddingValues(start = 8.dp, end = 4.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Close, null,
                            tint     = Terciario,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Título centrado
                    Text(
                        text       = if (state.isEditing) "Editar franja manual" else "Nueva franja manual",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 17.sp,
                        color      = Color.Black,
                        modifier   = Modifier.weight(1f),
                        textAlign  = TextAlign.Center
                    )

                    // Guardar — derecha (mismo patrón que EditActionButton pero con Primario)
                    TextButton(
                        onClick        = { viewModel.saveTimeSlot() },
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

                // ── Nombre de la franja (cabecera bajo la nav bar) ────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    // Campo de nombre inline como ItemTitle editable
                    BasicNameField(
                        value       = state.name,
                        onValueChange = { viewModel.onNameChanged(it) },
                        hasError    = state.error != null && state.name.isBlank()
                    )
                }
            }
        }
    ) { pad ->
        Column(
            modifier            = Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // ── Error ─────────────────────────────────────────────────────────
            if (state.error != null) {
                AppBanner(text = state.error!!, style = BannerStyle.WARNING)
            }


            // ── Tipo de recurrencia ───────────────────────────────────────────
            IosGroupCard {
                IosRow(
                    icon            = Icons.Default.Repeat,
                    iconTint        = IconRepeticion,
                    label           = "Tipo de recurrencia",
                    trailingContent = null
                )
                IosDivider()
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    RecurrenceTabs(
                        selected   = state.recurrenceType,
                        onSelected = { viewModel.onRecurrenceTypeChanged(it) }
                    )
                }
            }

            // ── Paridad (EVEN/ODD) ────────────────────────────────────────────
            if (state.recurrenceType == RecurrenceType.EVEN_WEEKS ||
                state.recurrenceType == RecurrenceType.ODD_WEEKS) {
                IosGroupCard {
                    IosRow(
                        icon            = Icons.Default.CalendarViewWeek,
                        iconTint        = IconAlarma,
                        label           = "¿Qué semanas?",
                        trailingContent = null
                    )
                    IosDivider()
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        WeekParitySelector(
                            selected   = state.recurrenceType,
                            onSelected = { viewModel.onRecurrenceTypeChanged(it) }
                        )
                    }
                }
            }

            // ── Rango de fechas ───────────────────────────────────────────────
            if (state.recurrenceType == RecurrenceType.DATE_RANGE) {
                IosGroupCard {
                    IosRow(
                        icon            = Icons.Default.DateRange,
                        iconTint        = IconFecha,
                        label           = "Período activo",
                        trailingContent = null
                    )
                    IosDivider()
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        DateRangeSelector(
                            startDate    = state.rangeStart,
                            endDate      = state.rangeEnd,
                            hasError     = state.error != null && state.rangeStart == null,
                            onStartClick = { showRangeStartPicker = true },
                            onEndClick   = { showRangeEndPicker = true }
                        )
                    }
                }
            }

            // ── Día único ─────────────────────────────────────────────────────
            if (state.recurrenceType == RecurrenceType.SINGLE_DAY) {
                IosGroupCard {
                    IosRow(
                        icon            = Icons.Default.Event,
                        iconTint        = IconFecha,
                        label           = "Fecha",
                        trailingContent = null
                    )
                    IosDivider()
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        SingleDayPicker(
                            date     = state.rangeStart,
                            hasError = state.error != null && state.rangeStart == null,
                            onClick  = { showRangeStartPicker = true }
                        )
                    }
                }
            }

            // ── Días de la semana ─────────────────────────────────────────────
            if (state.recurrenceType != RecurrenceType.SINGLE_DAY) {
                IosGroupCard {
                    IosRow(
                        icon            = Icons.Default.ViewWeek,
                        iconTint        = IconFecha,
                        label           = "Días de la semana",
                        trailingContent = null
                    )
                    IosDivider()
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        DayOfWeekSelector(
                            activeDays   = state.daysOfWeek,
                            hasError     = state.error != null && state.daysOfWeek.isEmpty(),
                            onDayToggled = { viewModel.onDayToggled(it) }
                        )
                    }
                }
            } else {
                val dayLabels = listOf(1 to "L", 2 to "M", 3 to "X", 4 to "J", 5 to "V", 6 to "S", 7 to "D")
                val derivedDay: Int? = state.rangeStart?.let { date ->
                    val cal = java.util.Calendar.getInstance().apply { time = date }
                    // Calendar.DAY_OF_WEEK: 1=Dom,2=Lun...7=Sab → convertimos a tu esquema (1=Lun...7=Dom)
                    val javaDow = cal.get(java.util.Calendar.DAY_OF_WEEK)
                    if (javaDow == java.util.Calendar.SUNDAY) 7 else javaDow - 1
                }
                IosGroupCard {
                    IosRow(
                        icon            = Icons.Default.ViewWeek,
                        iconTint        = IconFecha,
                        label           = "Días de la semana",
                        trailingContent = null
                    )
                    IosDivider()

                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        DayOfWeekSelector(
                            activeDays   = if (derivedDay != null) listOf(derivedDay) else emptyList(),
                            hasError     = false,
                            onDayToggled = { /* solo lectura en SINGLE_DAY */ }
                        )
                    }
                }
            }

            // ── Horario ───────────────────────────────────────────────────────
            IosGroupCard {
                IosRow(
                    icon            = Icons.Default.AccessTime,
                    iconTint        = IconNotas,
                    label           = "Horario",
                    trailingContent = null
                )
                IosDivider()
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    TimeRangeSelector(
                        startMinute  = state.startMinuteOfDay,
                        endMinute    = state.endMinuteOfDay,
                        hasError     = state.error != null && state.startMinuteOfDay >= state.endMinuteOfDay,
                        onStartClick = { showStartTimePicker = true },
                        onEndClick   = { showEndTimePicker = true }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // ── Pickers ───────────────────────────────────────────────────────────
        if (showStartTimePicker) {
            val (h, m) = state.startMinuteOfDay.toHourMinute()
            selectionDate.TimePickerModal(
                initialHour = h, initialMinute = m,
                onDismiss   = { showStartTimePicker = false },
                onConfirm   = { hour, min -> viewModel.onStartTimeChanged(hour, min); showStartTimePicker = false }
            )
        }
        if (showEndTimePicker) {
            val (h, m) = state.endMinuteOfDay.toHourMinute()
            selectionDate.TimePickerModal(
                initialHour = h, initialMinute = m,
                onDismiss   = { showEndTimePicker = false },
                onConfirm   = { hour, min -> viewModel.onEndTimeChanged(hour, min); showEndTimePicker = false }
            )
        }
        if (showRangeStartPicker) {
            selectionDate.DatePickerModal(
                initialDateMillis = state.rangeStart?.time,
                onDismiss         = { showRangeStartPicker = false },
                onConfirm         = { millis -> millis?.let { viewModel.onRangeStartChanged(it) }; showRangeStartPicker = false }
            )
        }
        if (showRangeEndPicker) {
            selectionDate.DatePickerModal(
                initialDateMillis = state.rangeEnd?.time,
                onDismiss         = { showRangeEndPicker = false },
                onConfirm         = { millis -> millis?.let { viewModel.onRangeEndChanged(it) }; showRangeEndPicker = false }
            )
        }

        pendingWarnings?.let { warnings ->
            WarningDialog(
                warnings  = warnings,
                onDismiss = { pendingWarnings = null; onBack() }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPONENTES PRIVADOS
// ─────────────────────────────────────────────────────────────────────────────

// ── Campo de nombre editable (reemplaza OutlinedTextField) ────────────────────
@Composable
private fun BasicNameField(
    value        : String,
    onValueChange: (String) -> Unit,
    hasError     : Boolean
) {
    androidx.compose.foundation.text.BasicTextField(
        value         = value,
        onValueChange = onValueChange,
        textStyle     = androidx.compose.ui.text.TextStyle(
            fontSize   = 22.sp,
            fontWeight = FontWeight.Bold,
            color      = Color.Black
        ),
        singleLine    = true,
        decorationBox = { inner ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        "Nombre de la franja",
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (hasError) ColorDestructive.copy(alpha = 0.5f)
                        else Terciario.copy(alpha = 0.5f)
                    )
                }
                inner()
            }
        }
    )
    // Línea inferior sutil en lugar de borde completo
    HorizontalDivider(
        thickness = if (hasError) 1.5.dp else 0.5.dp,
        color     = if (hasError) ColorDestructive else Terciario.copy(alpha = 0.3f)
    )
}

// ── Selector de recurrencia (tabs) ────────────────────────────────────────────
@Composable
private fun RecurrenceTabs(selected: RecurrenceType, onSelected: (RecurrenceType) -> Unit) {
    val options = listOf(
        RecurrenceType.WEEKLY     to "Semanal",
        RecurrenceType.EVEN_WEEKS to "Par / Impar",
        RecurrenceType.DATE_RANGE to "Rango",
        RecurrenceType.SINGLE_DAY to "Día único"
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
                        .weight(1f).height(34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isSelected) Color.White else Color.Transparent)
                        .clickable { onSelected(type) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        fontSize   = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color      = if (isSelected) Primario else Terciario
                    )
                }
            }
        }
    }
}

// ── Selector par / impar ──────────────────────────────────────────────────────
@Composable
private fun WeekParitySelector(selected: RecurrenceType, onSelected: (RecurrenceType) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(
            RecurrenceType.EVEN_WEEKS to Triple(Icons.Default.DateRange, "Semanas pares",   "2ª, 4ª... de cada mes"),
            RecurrenceType.ODD_WEEKS  to Triple(Icons.Default.Event, "Semanas impares", "1ª, 3ª... de cada mes")
        ).forEach { (type, triple) ->
            val (emoji, title, subtitle) = triple
            val isSelected = selected == type
            Surface(
                modifier = Modifier.weight(1f).clickable { onSelected(type) },
                shape    = RoundedCornerShape(14.dp),
                color    = if (isSelected) IconAlarma.copy(alpha = 0.15f) else ColorGrisFondo,

            ) {
                Column(
                    modifier            = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        emoji, null,
                        modifier = Modifier.size(24.dp),
                        tint     = if (isSelected) darkenColor(IconAlarma) else ColorGrisOscuro
                    )
                    Text(
                        title, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        color     = if (isSelected) darkenColor(IconAlarma) else ColorGrisOscuro,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        subtitle, fontSize = 11.sp,
                        color     = if (isSelected) darkenColor(IconAlarma) else ColorGrisOscuro,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ── Selector rango de fechas ──────────────────────────────────────────────────

@Composable
private fun DateRangeSelector(
    startDate   : java.util.Date?,
    endDate     : java.util.Date?,
    hasError    : Boolean,
    onStartClick: () -> Unit,
    onEndClick  : () -> Unit
) {
    val fmt = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale("es", "ES"))

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            DateBox(
                label       = "Desde",
                value       = startDate?.let { fmt.format(it) } ?: "Seleccionar",
                onClick     = onStartClick,
                highlighted = startDate != null,
                hasError    = hasError && startDate == null,
                modifier    = Modifier.weight(1f)
            )

            Icon(Icons.Default.ArrowForward, null, tint = Terciario, modifier = Modifier.size(18.dp))

            DateBox(
                label       = "Hasta",
                value       = endDate?.let { fmt.format(it) } ?: "Seleccionar",
                onClick     = onEndClick,
                highlighted = endDate != null,
                hasError    = false,
                modifier    = Modifier.weight(1f)
            )
        }

        // El banner ahora tiene su propio lugar en la columna y no se solapa
        AppBanner(
            text  = "Fuera de este período, la franja se ignorará.",
            style = BannerStyle.INFO
        )
    }
}
// ── Selector día único ────────────────────────────────────────────────────────
@Composable
private fun SingleDayPicker(
    date    : java.util.Date?,
    hasError: Boolean,
    onClick : () -> Unit
) {
    val fmt = java.text.SimpleDateFormat("d 'de' MMMM, yyyy", java.util.Locale("es", "ES"))
    DateBox(
        label       = "Día seleccionado",
        value       = date?.let { fmt.format(it) } ?: "Toca para seleccionar",
        onClick     = onClick,
        highlighted = date != null,
        hasError    = hasError,
        modifier    = Modifier.fillMaxWidth()
    )
}

// ── Caja de fecha clicable ────────────────────────────────────────────────────
@Composable
private fun DateBox(
    label      : String,
    value      : String,
    onClick    : () -> Unit,
    highlighted: Boolean,
    hasError   : Boolean,
    modifier   : Modifier = Modifier
) {
    val accentColor = when {
        hasError    -> ColorDestructive
        highlighted -> darkenColor(IconFecha)
        else        -> null
    }
    val bgColor = when {
        hasError    -> ColorDestructive.copy(alpha = 0.08f)
        highlighted -> ColorGrisFondo
        else        -> null
    }
    Surface(
        modifier = modifier.clickable { onClick() },
        shape    = RoundedCornerShape(14.dp),
        color    = bgColor ?: ColorGrisFondo,
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                label.uppercase(),
                fontSize      = 10.sp, fontWeight = FontWeight.SemiBold,
                color         = accentColor ?: Terciario,
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                value,
                fontSize   = 15.sp, fontWeight = FontWeight.SemiBold,
                color      = accentColor ?: Color.Black
            )
        }
    }
}

// ── Selector de días de la semana ─────────────────────────────────────────────
@Composable
private fun DayOfWeekSelector(
    activeDays  : List<Int>,
    hasError    : Boolean,
    onDayToggled: (Int) -> Unit
) {
    val days = listOf(1 to "L", 2 to "M", 3 to "X", 4 to "J", 5 to "V", 6 to "S", 7 to "D")
    Column {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            days.forEach { (num, label) ->
                val active = activeDays.contains(num)
                Box(
                    modifier = Modifier
                        .weight(1f).height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when {
                                active   -> IconFecha.copy(alpha = 0.15f)
                                hasError -> ColorDestructive.copy(alpha = 0.07f)
                                else     -> ColorGrisFondo
                            }
                        )
                        .clickable { onDayToggled(num) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        fontSize   = 13.sp, fontWeight = FontWeight.Bold,
                        color      = when {
                            active   -> darkenColor(IconFecha)
                            hasError -> ColorDestructive.copy(alpha = 0.5f)
                            else     -> Terciario
                        }
                    )
                }
            }
        }
        if (hasError) {
            Spacer(Modifier.height(6.dp))
            Text(
                "Selecciona al menos un día",
                fontSize = 11.sp,
                color    = ColorDestructive
            )
        }
    }
}

// ── Selector de hora (inicio / fin) ──────────────────────────────────────────
@Composable
private fun TimeRangeSelector(
    startMinute : Int,
    endMinute   : Int,
    hasError    : Boolean,
    onStartClick: () -> Unit,
    onEndClick  : () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        TimePickerBox(
            label    = "Inicio",
            time     = startMinute.toTimeString(),
            hasError = hasError,
            onClick  = onStartClick,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Default.ArrowForward, null, tint = Terciario, modifier = Modifier.size(18.dp))
        TimePickerBox(
            label    = "Fin",
            time     = endMinute.toTimeString(),
            hasError = hasError,
            onClick  = onEndClick,
            modifier = Modifier.weight(1f)
        )
    }
}

// ── Caja de hora clicable ─────────────────────────────────────────────────────
@Composable
private fun TimePickerBox(
    label   : String,
    time    : String,
    hasError: Boolean,
    onClick : () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape    = RoundedCornerShape(14.dp),
        color    = if (hasError) ColorDestructive.copy(alpha = 0.08f)
        else ColorGrisFondo

    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                label.uppercase(),
                fontSize      = 10.sp, fontWeight = FontWeight.SemiBold,
                color         = if (hasError) ColorDestructive else Primario,
                letterSpacing = 0.5.sp
            )
            Text(
                time,
                fontSize   = 28.sp, fontWeight = FontWeight.Bold,
                color      = if (hasError) ColorDestructive else Primario
            )
        }
    }
}