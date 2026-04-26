package com.asistente.planificador.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asistente.core.domain.models.RecurrenceType
import com.asistente.core.domain.models.SlotType
import com.asistente.core.domain.models.TimeSlot
import com.asistente.planificador.ui.screens.tools.ColorGrisFondo
import com.asistente.planificador.ui.screens.tools.IconNotas
import com.asistente.planificador.ui.screens.tools.Primario
import com.asistente.planificador.ui.screens.tools.SearchAndFilterBar
import com.asistente.planificador.ui.screens.tools.SlotsCarousel
import com.asistente.planificador.ui.screens.tools.Terciario
import com.asistente.planificador.ui.screens.tools.TimeSlotDetailSheet
import com.asistente.planificador.ui.screens.tools.dotColor
import com.asistente.planificador.ui.viewmodels.TimeSlotEvent
import com.asistente.planificador.ui.viewmodels.TimeSlotViewModel
import kotlinx.coroutines.flow.collectLatest
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
    onNavigateToEditTask: (taskId: String) -> Unit,
    onNavigateToViewTask: (taskId: String) -> Unit,
    onBack: () -> Unit
) {
    val slots       by viewModel.timeSlotList.collectAsStateWithLifecycle()
    val planningEnabled by viewModel.planningEnabled.collectAsStateWithLifecycle()
    val detailState by viewModel.detailState.collectAsStateWithLifecycle()

    var searchQuery            by remember { mutableStateOf("") }
    var activeTypeFilters      by remember { mutableStateOf(emptySet<SlotType>()) }
    var activeRecurrenceFilters by remember { mutableStateOf(emptySet<RecurrenceType>()) }
    var activeStatusFilter     by remember { mutableStateOf<Boolean?>(null) }
    val categoryByTaskId by viewModel.categoryByTaskId.collectAsStateWithLifecycle()

    // ── Dialog de warnings ────────────────────────────────────────────────────
    var pendingWarnings by remember { mutableStateOf<List<String>?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // ── Consumir eventos one-shot del ViewModel ───────────────────────────────
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is TimeSlotEvent.SaveSuccess -> { /* navegación ya hecha en el form */ }
                is TimeSlotEvent.SaveWithWarnings -> pendingWarnings = event.warnings
                is TimeSlotEvent.Error -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message  = event.message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        }
    }

    val filteredSlots = remember(slots, searchQuery, activeTypeFilters, activeRecurrenceFilters, activeStatusFilter) {
        slots.filter { slot ->
            val matchesName       = searchQuery.isBlank() || slot.name.contains(searchQuery, ignoreCase = true)
            val matchesType       = activeTypeFilters.isEmpty() || slot.slotType in activeTypeFilters
            val matchesRecurrence = activeRecurrenceFilters.isEmpty() || slot.recurrenceType in activeRecurrenceFilters
            val matchesStatus     = activeStatusFilter == null || slot.enable == activeStatusFilter
            matchesName && matchesType && matchesRecurrence && matchesStatus
        }
    }

    Scaffold(
        containerColor   = ColorGrisFondo,
        snackbarHost     = { SnackbarHost(snackbarHostState) },
        topBar = {
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
                            Icon(Icons.Rounded.Close, null, tint = Terciario, modifier = Modifier.size(24.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Franjas horarias", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.Black)
                            Text(calendarName, fontSize = 13.sp, color = Terciario)
                        }
                        Button(
                            onClick = { onNavigateToForm(null) },
                            colors  = ButtonDefaults.buttonColors(containerColor = Primario),
                            shape   = RoundedCornerShape(6.dp),
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
            modifier = Modifier.padding(pad).fillMaxSize(),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
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
                    onClearAll = {
                        activeTypeFilters = emptySet()
                        activeRecurrenceFilters = emptySet()
                        activeStatusFilter = null
                    },
                    modifier = Modifier.padding(horizontal = 16.dp).padding(top = 14.dp, bottom = 6.dp)
                )
            }
            item {
                PlanningToggleBanner(
                    enabled  = planningEnabled,
                    onToggle = { viewModel.togglePlanning() },
                    modifier = Modifier.padding(horizontal = 16.dp).padding(top = 4.dp, bottom = 8.dp)
                )
            }
            item {
                SlotsCarousel(
                    filteredSlots  = filteredSlots,
                    allSlots       = slots,
                    onEdit         = { onNavigateToForm(it) },
                    onEditTask     = { taskId -> onNavigateToEditTask(taskId) },
                    onDelete       = { viewModel.deleteTimeSlot(it.id) },
                    onToggleActive = { viewModel.toggleTimeSlotActive(it.id) },
                    onCardClick    = { viewModel.openDetail(it) },
                    onAdd          = { onNavigateToForm(null) },
                    categoryByTaskId = categoryByTaskId,
                    modifier       = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }
            item {
                WeekHeatmap(
                    slots    = slots,
                    onSlotClick = { viewModel.openDetail(it) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }

    // ── Detail bottom sheet ───────────────────────────────────────────────────
    detailState?.let { detail ->
        TimeSlotDetailSheet(
            state    = detail,
            onDismiss = { viewModel.closeDetail() },
            onEdit = { slot ->
                onNavigateToForm(slot)
            },
            onEditTask = { taskId ->
                onNavigateToEditTask(taskId)
                viewModel.closeDetail()
            },
            onViewTask = { taskId -> onNavigateToViewTask(taskId) },
            onDelete = { viewModel.deleteTimeSlot(it.id) },
            onToggleActive = { viewModel.toggleTimeSlotActive(it.id) },
            onOverlappingSlotClick = { viewModel.openDetail(it) }
        )
    }

    // ── Dialog de warnings de solapamiento ────────────────────────────────────
    pendingWarnings?.let { warnings ->
        WarningDialog(
            warnings  = warnings,
            onDismiss = { pendingWarnings = null }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DIALOG DE WARNINGS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
public fun WarningDialog(warnings: List<String>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Color.White,
        shape            = RoundedCornerShape(20.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF8E1)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Warning, null,
                    tint = Color(0xFFF57F17),
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                "Franja guardada con avisos",
                fontWeight = FontWeight.Bold,
                fontSize   = 17.sp,
                textAlign  = TextAlign.Center
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "La franja se ha guardado, pero ten en cuenta lo siguiente:",
                    fontSize  = 13.sp,
                    color     = Terciario,
                    textAlign = TextAlign.Center
                )
                warnings.forEach { warning ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFF8E1)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Warning, null,
                                tint     = Color(0xFFF57F17),
                                modifier = Modifier.size(13.dp).padding(top = 2.dp)
                            )
                            Text(warning, fontSize = 12.sp, color = Color(0xFF5D4037))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors  = ButtonDefaults.buttonColors(containerColor = Primario),
                shape   = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Entendido", fontWeight = FontWeight.Bold)
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// HEATMAP — ahora con onSlotClick en celda
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WeekHeatmap(
    slots: List<TimeSlot>,
    onSlotClick: (TimeSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    val today        = LocalDate.now()
    val weekFields   = WeekFields.of(Locale("es", "ES"))
    val initialPage  = 500
    val pagerState   = rememberPagerState(initialPage = initialPage) { 1000 }
    val scope        = rememberCoroutineScope()
    var showDatePicker by remember { mutableStateOf(false) }

    val currentWeekOffset = pagerState.currentPage - initialPage
    val mondayOfPage = remember(pagerState.currentPage) {
        today.with(WeekFields.ISO.dayOfWeek(), 1).plusWeeks(currentWeekOffset.toLong())
    }
    val sundayOfPage  = mondayOfPage.plusDays(6)
    val isCurrentWeek = currentWeekOffset == 0
    val weekLabel = remember(mondayOfPage) {
        val fmt = DateTimeFormatter.ofPattern("d MMM", Locale("es", "ES"))
        "${mondayOfPage.format(fmt)} – ${sundayOfPage.format(fmt)} ${sundayOfPage.year}"
    }
    val weekNumber = mondayOfPage.get(weekFields.weekOfWeekBasedYear())

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "VISTA SEMANAL", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
            color = Terciario, letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Surface(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(18.dp),
            color     = Color.White,
            tonalElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(vertical = 14.dp)) {

                // Cabecera navegación
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick  = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
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
                                weekLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                color = if (isCurrentWeek) Primario else Color.Black
                            )
                            if (isCurrentWeek) {
                                Surface(shape = RoundedCornerShape(6.dp), color = IconNotas.copy(alpha = 0.5f)) {
                                    Text(
                                        "Esta semana", fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold, color = Primario,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text("Semana $weekNumber", fontSize = 9.sp, color = Terciario)
                    }
                    IconButton(
                        onClick  = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.ChevronRight, null, tint = Terciario, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(Modifier.height(12.dp))

                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
                    val weekOffset = page - initialPage
                    val monday = today.with(WeekFields.ISO.dayOfWeek(), 1).plusWeeks(weekOffset.toLong())
                    WeekGrid(
                        slots       = slots,
                        weekMonday  = monday,
                        onSlotClick = onSlotClick
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Leyenda
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(Color(0xFFFF5757).copy(alpha = 0.25f), "Bloqueada Manualmente")
                    Spacer(Modifier.width(12.dp))
                    LegendItem(Color(0xFF2894e3).copy(alpha = 0.25f), "Bloqueada por Tarea")
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

// ─────────────────────────────────────────────────────────────────────────────
// GRID SEMANAL — celdas clickables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WeekGrid(
    slots: List<TimeSlot>,
    weekMonday: LocalDate,
    onSlotClick: (TimeSlot) -> Unit
) {
    val dayLabels     = listOf("L", "M", "X", "J", "V", "S", "D")
    val today         = LocalDate.now()
    val hourColumnWidth = 32.dp

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        // Cabecera días
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(hourColumnWidth))
            dayLabels.forEachIndexed { index, day ->
                val date    = weekMonday.plusDays(index.toLong())
                val isToday = date == today
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(day, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        color = if (isToday) Primario else Terciario)
                    Box(
                        modifier = Modifier.size(18.dp)
                            .background(if (isToday) Primario else Color.Transparent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${date.dayOfMonth}", fontSize = 9.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                            color = if (isToday) Color.White else Terciario,
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            )
                        )
                    }
                }
            }
            Spacer(Modifier.width(4.dp))
        }

        Spacer(Modifier.height(8.dp))

        // Filas de horas
        (0..23).forEach { hour ->
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${hour}h", fontSize = 9.sp, color = Terciario.copy(alpha = 0.7f),
                    modifier = Modifier.width(hourColumnWidth).padding(end = 4.dp),
                    textAlign = TextAlign.End
                )
                Row(modifier = Modifier.weight(1f)) {
                    (1..7).forEach { dayNum ->
                        val date       = weekMonday.plusDays((dayNum - 1).toLong())
                        val matchSlot  = getSlotForCell(slots, dayNum, hour, date)
                        val cellColor  = if (matchSlot != null)
                            matchSlot.slotType.dotColor().copy(alpha = 0.5f)
                        else Color(0xFFF0F0F0)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(14.dp)
                                .padding(horizontal = 1.dp, vertical = 1.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(cellColor)
                                .then(
                                    if (matchSlot != null)
                                        Modifier.clickable { onSlotClick(matchSlot) }
                                    else Modifier
                                )
                        )
                    }
                }
                Spacer(Modifier.width(4.dp))
            }
        }
    }
}

// ── Devuelve el slot más prioritario para una celda (TASK_BLOCKED > BLOCKED) ──

private fun getSlotForCell(
    slots: List<TimeSlot>,
    dayNum: Int,
    hour: Int,
    date: LocalDate
): TimeSlot? {
    val zone      = java.time.ZoneId.systemDefault()
    val hourStart = hour * 60
    val hourEnd   = hourStart + 60

    val candidates = slots.filter { slot ->
        if (!slot.enable) return@filter false
        val hourMatches = hourStart < slot.endMinuteOfDay && hourEnd > slot.startMinuteOfDay
        if (!hourMatches) return@filter false
        when (slot.recurrenceType) {
            RecurrenceType.WEEKLY     -> slot.daysOfWeek.contains(dayNum)
            RecurrenceType.EVEN_WEEKS -> slot.daysOfWeek.contains(dayNum) &&
                    date.get(WeekFields.ISO.weekOfWeekBasedYear()) % 2 == 0
            RecurrenceType.ODD_WEEKS  -> slot.daysOfWeek.contains(dayNum) &&
                    date.get(WeekFields.ISO.weekOfWeekBasedYear()) % 2 != 0
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
    }

    // TASK_BLOCKED tiene prioridad visual — se pinta primero, pero el click abre el de mayor prioridad
    return candidates.maxByOrNull { if (it.slotType == SlotType.TASK_BLOCKED) 1 else 0 }
}

// ─────────────────────────────────────────────────────────────────────────────
// RESTO DE COMPONENTES
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(10.dp, 8.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Text(label, fontSize = 9.sp, color = Terciario, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun PlanningToggleBanner(enabled: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val textColor   = if (enabled) Primario else Terciario

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        color    = Color.White,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp)
                    .background(if (enabled) Primario else Terciario.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Extension, null,
                    tint = if (enabled) Color.White else Terciario,
                    modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Asistente de planificación", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
                Text(
                    if (enabled) "Activo · aplica solo a franjas activas" else "Desactivado · no se usarán franjas",
                    fontSize = 11.sp, color = textColor.copy(alpha = 0.9f)
                )
            }
            Switch(
                checked  = enabled,
                onCheckedChange = { onToggle() },
                colors   = SwitchDefaults.colors(
                    checkedThumbColor   = Color.White,
                    checkedTrackColor   = Primario,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Terciario.copy(alpha = 0.35f),
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    }
}

// ── Extensiones SlotType ──────────────────────────────────────────────────────

fun SlotType.label(): String = when (this) {
    SlotType.BLOCKED      -> "Bloqueada Manualmente"
    SlotType.TASK_BLOCKED -> "Bloqueada por Tarea"
}

fun RecurrenceType.shortLabel(): String = when (this) {
    RecurrenceType.WEEKLY     -> "Semanal"
    RecurrenceType.EVEN_WEEKS -> "Semanas pares"
    RecurrenceType.ODD_WEEKS  -> "Semanas impares"
    RecurrenceType.DATE_RANGE -> "Rango"
    RecurrenceType.SINGLE_DAY -> "Día único"
}