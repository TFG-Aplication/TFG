package com.asistente.planificador.ui.screens


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asistente.core.domain.models.RecurrenceType
import com.asistente.core.domain.models.SlotType
import com.asistente.core.domain.models.TimeSlot
import com.asistente.planificador.ui.screens.tools.SearchAndFilterBar
import com.asistente.planificador.ui.screens.tools.SlotsCarousel
import com.asistente.planificador.ui.viewmodels.TimeSlotViewModel
import com.asistente.planificador.ui.viewmodels.toTimeString
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// SCREEN
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSlotListScreen(
    calendarName: String,
    viewModel: TimeSlotViewModel = hiltViewModel(),
    onNavigateToForm: (TimeSlot?) -> Unit,
    onBack: () -> Unit
) {
    val slots by viewModel.timeSlotList.collectAsStateWithLifecycle()
    val planningEnabled by viewModel.planningEnabled.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var activeTypeFilters by remember { mutableStateOf(emptySet<SlotType>()) }
    var activeRecurrenceFilters by remember { mutableStateOf(emptySet<RecurrenceType>()) }
    var activeStatusFilter by remember { mutableStateOf<Boolean?>(null) } // null=todos, true=activas, false=desactivas

    val filteredSlots = remember(slots, searchQuery, activeTypeFilters, activeRecurrenceFilters, activeStatusFilter) {
        slots.filter { slot ->
            val matchesName = searchQuery.isBlank() || slot.name.contains(searchQuery, ignoreCase = true)
            val matchesType = activeTypeFilters.isEmpty() || slot.slotType in activeTypeFilters
            val matchesRecurrence = activeRecurrenceFilters.isEmpty() || slot.recurrenceType in activeRecurrenceFilters
            val matchesStatus = activeStatusFilter == null || slot.isActive == activeStatusFilter
            matchesName && matchesType && matchesRecurrence && matchesStatus
        }
    }

    Scaffold(
        containerColor = Color(0xFFF9F9F9),
        topBar = {
            // Header personalizado, más grande
            Surface(color = Color.White, shadowElevation = 2.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 4.dp, end = 16.dp, top = 10.dp, bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Cerrar",
                                tint = Terciario,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Franjas horarias",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color.Black,
                                lineHeight = 24.sp
                            )
                            Text(
                                calendarName,
                                fontSize = 13.sp,
                                color = Terciario,
                                fontWeight = FontWeight.Normal
                            )
                        }
                        Button(
                            onClick = { onNavigateToForm(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = Primario),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Añadir", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {

            // ── Buscador + filtros ────────────────────────────────────
            item {
                SearchAndFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    activeTypeFilters = activeTypeFilters,
                    onTypeFilterToggle = { type ->
                        activeTypeFilters = if (type in activeTypeFilters)
                            activeTypeFilters - type else activeTypeFilters + type
                    },
                    activeRecurrenceFilters = activeRecurrenceFilters,
                    onRecurrenceFilterToggle = { type ->
                        activeRecurrenceFilters = if (type in activeRecurrenceFilters)
                            activeRecurrenceFilters - type else activeRecurrenceFilters + type
                    },
                    activeStatusFilter = activeStatusFilter,
                    onStatusFilterChange = { activeStatusFilter = if (activeStatusFilter == it) null else it },
                    onClearAll = { activeTypeFilters = emptySet(); activeRecurrenceFilters = emptySet(); activeStatusFilter = null },
                    modifier = Modifier.padding(horizontal = 16.dp).padding(top = 14.dp, bottom = 6.dp)
                )
            }

            // ── Banner asistente ──────────────────────────────────────
            item {
                PlanningToggleBanner(
                    enabled = planningEnabled,
                    onToggle = { viewModel.togglePlanning() },
                    modifier = Modifier.padding(horizontal = 16.dp).padding(top = 4.dp, bottom = 8.dp)
                )
            }

            // ── Carrusel de franjas ───────────────────────────────────
            item {
                SlotsCarousel(
                    filteredSlots = filteredSlots,
                    allSlots = slots,
                    onEdit = { onNavigateToForm(it) },
                    onDelete = { viewModel.deleteTimeSlot(it.id) },
                    onToggleActive = { viewModel.toggleTimeSlotActive(it.id) },
                    onAdd = { onNavigateToForm(null) },
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }

            // ── Calendario semanal ────────────────────────────────────
            item {
                WeekHeatmap(
                    slots = slots,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}



// ─────────────────────────────────────────────────────────────────────────────
// HEATMAP SEMANAL NAVEGABLE
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WeekHeatmap(slots: List<TimeSlot>, modifier: Modifier = Modifier) {
    val today = LocalDate.now()
    val weekFields = WeekFields.of(Locale("es", "ES"))
    val initialPage = 500

    val pagerState = rememberPagerState(initialPage = initialPage) { 1000 }
    val scope = rememberCoroutineScope()
    var showDatePicker by remember { mutableStateOf(false) }

    val currentWeekOffset = pagerState.currentPage - initialPage
    val mondayOfPage = remember(pagerState.currentPage) {
        today.with(WeekFields.ISO.dayOfWeek(), 1).plusWeeks(currentWeekOffset.toLong())
    }
    val sundayOfPage = mondayOfPage.plusDays(6)
    val isCurrentWeek = currentWeekOffset == 0
    val weekLabel = remember(mondayOfPage) {
        val fmt = DateTimeFormatter.ofPattern("d MMM", Locale("es", "ES"))
        "${mondayOfPage.format(fmt)} – ${sundayOfPage.format(fmt)} ${sundayOfPage.year}"
    }
    val weekNumber = mondayOfPage.get(weekFields.weekOfWeekBasedYear())

    Column(modifier = modifier.fillMaxWidth()) {
        // --- NUEVO TÍTULO SUPERIOR ---
        Text(
            "VISTA SEMANAL",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Terciario,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            tonalElevation = 1.dp,
            border = BorderStroke(1.dp, Color(0xFFF0F0F0))
        ) {
            Column(modifier = Modifier.padding(vertical = 14.dp)) { // Padding horizontal lo maneja el grid

                // Cabecera de navegación
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.ChevronLeft, null, tint = Terciario, modifier = Modifier.size(20.dp))
                    }
                    Column(
                        modifier = Modifier.weight(1f).clickable { showDatePicker = true },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                weekLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrentWeek) Primario else Color.Black
                            )
                            if (isCurrentWeek) {
                                Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFF3E5E2)) {
                                    Text(
                                        "Esta semana",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Primario,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text("Semana $weekNumber", fontSize = 9.sp, color = Terciario)
                    }
                    IconButton(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.ChevronRight, null, tint = Terciario, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(Modifier.height(12.dp))

                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
                    val weekOffset = page - initialPage
                    val monday = today.with(WeekFields.ISO.dayOfWeek(), 1).plusWeeks(weekOffset.toLong())
                    // Pasamos un padding interno para centrar
                    WeekGrid(slots = slots, weekMonday = monday)
                }

                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.Center, // Centramos la leyenda
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(Color(0xFFE53935).copy(alpha = 0.25f), "Bloqueada")
                    Spacer(Modifier.width(12.dp))
                    LegendItem(Color(0xFF7B1FA2).copy(alpha = 0.25f), "Por tarea")
                    Spacer(Modifier.width(12.dp))
                    LegendItem(Color(0xFFF0F0F0), "Disponible")
                }
            }
        }
    }

    if (showDatePicker) {
        val dpState = rememberDatePickerState(
            initialSelectedDateMillis = mondayOfPage.atStartOfDay()
                .toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { millis ->
                        val picked = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneOffset.UTC).toLocalDate()
                        val pickedMonday = picked.with(WeekFields.ISO.dayOfWeek(), 1)
                        val todayMonday  = today.with(WeekFields.ISO.dayOfWeek(), 1)
                        val diffWeeks = java.time.temporal.ChronoUnit.WEEKS.between(todayMonday, pickedMonday).toInt()
                        scope.launch { pagerState.animateScrollToPage(initialPage + diffWeeks) }
                    }
                    showDatePicker = false
                }) { Text("Ir", color = Primario, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = Terciario) }
            },
            shape = RoundedCornerShape(20.dp)
        ) { DatePicker(state = dpState) }
    }
}

@Composable
private fun WeekGrid(slots: List<TimeSlot>, weekMonday: LocalDate) {
    val dayLabels = listOf("L", "M", "X", "J", "V", "S", "D")
    val today = LocalDate.now()
    val hourColumnWidth = 32.dp // Aumentado para mejor legibilidad

    Column(modifier = Modifier.padding(horizontal = 8.dp)) { // Margen pequeño en los bordes del Surface
        // Fila de días
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(hourColumnWidth)) // Espacio de la columna de horas
            dayLabels.forEachIndexed { index, day ->
                val date = weekMonday.plusDays(index.toLong())
                val isToday = date == today
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        day, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        color = if (isToday) Primario else Terciario
                    )
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(if (isToday) Primario else Color.Transparent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${date.dayOfMonth}", fontSize = 9.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                            color = if (isToday) Color.White else Terciario,
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false // Elimina el espacio extra que empuja el número hacia abajo
                                ),
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            )
                        )
                    }
                }
            }
            // ESTE ES EL SECRETO: un spacer igual al de la izquierda para centrar los días
            Spacer(Modifier.width(4.dp))
        }

        Spacer(Modifier.height(8.dp))

        // Grid de horas
        (0..23).forEach { hour ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Columna de hora
                Text(
                    "${hour}h",
                    fontSize = 9.sp,
                    color = Terciario.copy(alpha = 0.7f),
                    modifier = Modifier.width(hourColumnWidth).padding(end = 4.dp),
                    textAlign = TextAlign.End
                )

                // Celdas de días
                Row(modifier = Modifier.weight(1f)) {
                    (1..7).forEach { dayNum ->
                        val date = weekMonday.plusDays((dayNum - 1).toLong())
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(14.dp) // Aumentado de 11 a 14 para que las celdas sean más grandes
                                .padding(horizontal = 1.dp, vertical = 1.dp) // Más aire entre celdas
                                .clip(RoundedCornerShape(3.dp)) // Radio un poco más suave
                                .background(getCellColor(slots, dayNum, hour, date))
                        )
                    }
                }
                // Compensación derecha para que las celdas no toquen el borde y queden centradas respecto a arriba
                Spacer(Modifier.width(4.dp))
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(10.dp, 8.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Text(label, fontSize = 9.sp, color = Terciario, fontWeight = FontWeight.Medium)
    }
}

private fun getCellColor(slots: List<TimeSlot>, dayNum: Int, hour: Int, date: LocalDate): Color {
    val zone      = java.time.ZoneId.systemDefault()
    val hourStart = hour * 60
    val hourEnd   = hourStart + 60
    val match = slots.firstOrNull { slot ->
        if (!slot.isActive) return@firstOrNull false
        val hourMatches = hourStart < slot.endMinuteOfDay && hourEnd > slot.startMinuteOfDay
        if (!hourMatches) return@firstOrNull false
        when (slot.recurrenceType) {
            RecurrenceType.WEEKLY     -> slot.daysOfWeek.contains(dayNum)
            RecurrenceType.EVEN_WEEKS -> slot.daysOfWeek.contains(dayNum) && date.get(WeekFields.ISO.weekOfWeekBasedYear()) % 2 == 0
            RecurrenceType.ODD_WEEKS  -> slot.daysOfWeek.contains(dayNum) && date.get(WeekFields.ISO.weekOfWeekBasedYear()) % 2 != 0
            RecurrenceType.DATE_RANGE -> {
                val start = slot.rangeStart?.toInstant()?.atZone(zone)?.toLocalDate()
                val end   = slot.rangeEnd?.toInstant()?.atZone(zone)?.toLocalDate()
                start != null && end != null && !date.isBefore(start) && !date.isAfter(end)
            }
            RecurrenceType.SINGLE_DAY -> {
                val target = slot.rangeStart?.toInstant()?.atZone(zone)?.toLocalDate()
                date == target
            }
        }
    } ?: return Color(0xFFF0F0F0)
    return when (match.slotType) {
        SlotType.BLOCKED      -> Color(0xFFE53935).copy(alpha = 0.22f)
        SlotType.TASK_BLOCKED -> Color(0xFF7B1FA2).copy(alpha = 0.22f)
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// BANNER ASISTENTE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PlanningToggleBanner(
    enabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor     = if (enabled) Color(0xFFF3E5E2) else Color(0xFFF0F0F0)
    val textColor   = if (enabled) Primario else Terciario
    val borderColor = if (enabled) Primario.copy(alpha = 0.35f) else Color.Transparent

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(if (enabled) Primario else Terciario.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Extension, null,
                    tint = if (enabled) Color.White else Terciario,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Asistente de planificación", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
                Text(
                    if (enabled) "Activo · aplica solo a franjas activas" else "Desactivado · no se usarán franjas",
                    fontSize = 11.sp, color = textColor.copy(alpha = 0.7f)
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor   = Color.White,
                    checkedTrackColor   = Primario,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Terciario.copy(alpha = 0.35f)
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EXTENSIONES SlotType
// ─────────────────────────────────────────────────────────────────────────────

fun SlotType.dotColor(): Color = when (this) {
    SlotType.BLOCKED      -> Color(0xFFE53935)
    SlotType.TASK_BLOCKED -> Color(0xFF7B1FA2)
}

fun SlotType.badgeColors(): Pair<Color, Color> = when (this) {
    SlotType.BLOCKED      -> Pair(Color(0xFFFFEBEE), Color(0xFFE53935))
    SlotType.TASK_BLOCKED -> Pair(Color(0xFFF3E5F5), Color(0xFF7B1FA2))
}

fun SlotType.label(): String = when (this) {
    SlotType.BLOCKED      -> "Bloqueada"
    SlotType.TASK_BLOCKED -> "Por tarea"
}

fun RecurrenceType.shortLabel(): String = when (this) {
    RecurrenceType.WEEKLY     -> "Semanal"
    RecurrenceType.EVEN_WEEKS -> "S. pares"
    RecurrenceType.ODD_WEEKS  -> "S. impares"
    RecurrenceType.DATE_RANGE -> "Rango"
    RecurrenceType.SINGLE_DAY -> "Día único"
}

private fun buildMetaText(slot: TimeSlot): String {
    val time = "${slot.startMinuteOfDay.toTimeString()}–${slot.endMinuteOfDay.toTimeString()}"
    val rec = when (slot.recurrenceType) {
        RecurrenceType.WEEKLY     -> "Cada semana"
        RecurrenceType.EVEN_WEEKS -> "Semanas pares"
        RecurrenceType.ODD_WEEKS  -> "Semanas impares"
        RecurrenceType.DATE_RANGE -> "Rango de fechas"
        RecurrenceType.SINGLE_DAY -> "Día único"
    }
    return "$time · $rec"
}

