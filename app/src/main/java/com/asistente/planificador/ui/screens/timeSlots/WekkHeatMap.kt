package com.asistente.planificador.ui.screens.timeSlots


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.asistente.core.domain.models.RecurrenceType
import com.asistente.core.domain.models.SlotType
import com.asistente.core.domain.models.TimeSlot
import com.asistente.planificador.ui.screens.CellSlotsPickerSheet
import com.asistente.planificador.ui.screens.tools.IconNotas
import com.asistente.planificador.ui.screens.tools.Primario
import com.asistente.planificador.ui.screens.tools.Terciario
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// HEATMAP
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WeekHeatmap(
    slots: List<TimeSlot>,
    onSlotClick: (TimeSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    var pendingCell by remember { mutableStateOf<PendingCell?>(null) }
    val today       = LocalDate.now()
    val weekFields  = WeekFields.of(Locale("es", "ES"))
    val initialPage = 500
    val pagerState  = rememberPagerState(initialPage = initialPage) { 1000 }
    val scope       = rememberCoroutineScope()
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
            modifier       = Modifier.fillMaxWidth(),
            shape          = RoundedCornerShape(18.dp),
            color          = Color.White,
            tonalElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(vertical = 14.dp)) {
                HeatmapNavHeader(
                    weekLabel     = weekLabel,
                    weekNumber    = weekNumber,
                    isCurrentWeek = isCurrentWeek,
                    onPrev        = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                    onNext        = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                    onPickDate    = { showDatePicker = true }
                )

                Spacer(Modifier.height(12.dp))

                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
                    val monday = today.with(WeekFields.ISO.dayOfWeek(), 1)
                        .plusWeeks((page - initialPage).toLong())
                    WeekGrid(
                        slots      = slots,
                        weekMonday = monday,
                        onCellClick = { cellSlots, date, hour ->
                            if (cellSlots.size == 1) onSlotClick(cellSlots.first())
                            else pendingCell = PendingCell(cellSlots, date, hour)
                        }
                    )
                }

                Spacer(Modifier.height(14.dp))
                HeatmapLegend()
            }
        }
    }

    if (showDatePicker) {
        HeatmapDatePicker(
            initialMillis = mondayOfPage.atStartOfDay()
                .toInstant(java.time.ZoneOffset.UTC).toEpochMilli(),
            onConfirm = { millis ->
                val picked = java.time.Instant.ofEpochMilli(millis)
                    .atZone(java.time.ZoneOffset.UTC).toLocalDate()
                val diffWeeks = java.time.temporal.ChronoUnit.WEEKS.between(
                    today.with(WeekFields.ISO.dayOfWeek(), 1),
                    picked.with(WeekFields.ISO.dayOfWeek(), 1)
                ).toInt()
                scope.launch { pagerState.animateScrollToPage(initialPage + diffWeeks) }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    pendingCell?.let { cell ->
        val dayFormatter = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", Locale("es", "ES"))
        val dayLabel = cell.date.format(dayFormatter)
            .replaceFirstChar { it.uppercase() }  // "Miércoles, 29/07/2026"
        val startTime = "%02d:00".format(cell.hourStart)
        val endTime   = "%02d:00".format(cell.hourStart + 1)

        CellSlotsPickerSheet(
            slots     = cell.slots,
            date      = cell.date,
            hour      = cell.hourStart,
            onSelect  = { slot -> pendingCell = null; onSlotClick(slot) },
            onDismiss = { pendingCell = null }
        )
    }
}

// ── Cabecera de navegación ────────────────────────────────────────────────────

@Composable
private fun HeatmapNavHeader(
    weekLabel: String,
    weekNumber: Int,
    isCurrentWeek: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onPickDate: () -> Unit
) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.ChevronLeft, null, tint = Terciario, modifier = Modifier.size(20.dp))
        }
        Column(
            modifier            = Modifier.weight(1f).clickable(onClick = onPickDate),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
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
        IconButton(onClick = onNext, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.ChevronRight, null, tint = Terciario, modifier = Modifier.size(20.dp))
        }
    }
}

// ── Leyenda ───────────────────────────────────────────────────────────────────

@Composable
private fun HeatmapLegend() {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        LegendItem(Color(0xFFFF5757).copy(alpha = 0.25f), "Bloqueada Manualmente")
        Spacer(Modifier.width(12.dp))
        LegendItem(Color(0xFF2894e3).copy(alpha = 0.25f), "Bloqueada por Tarea")
        Spacer(Modifier.width(12.dp))
        LegendItem(Color(0xFFF0F0F0), "Disponible")
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(10.dp, 8.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Text(label, fontSize = 9.sp, color = Terciario, fontWeight = FontWeight.Medium)
    }
}

// ── DatePicker ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeatmapDatePicker(
    initialMillis: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val dpState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { dpState.selectedDateMillis?.let(onConfirm) }) {
                Text("Ir", color = Primario, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Terciario) }
        },
        shape = RoundedCornerShape(20.dp)
    ) { DatePicker(state = dpState) }
}

// ─────────────────────────────────────────────────────────────────────────────
// GRID SEMANAL
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WeekGrid(
    slots: List<TimeSlot>,
    weekMonday: LocalDate,
    onCellClick: (slots: List<TimeSlot>, date: LocalDate, hour: Int) -> Unit
) {
    val today           = LocalDate.now()
    val hourColumnWidth = 32.dp

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        WeekDayHeader(weekMonday = weekMonday, today = today)
        Spacer(Modifier.height(8.dp))
        (0..23).forEach { hour ->
            HourRow(
                hour            = hour,
                hourColumnWidth = hourColumnWidth,
                slots           = slots,
                weekMonday      = weekMonday,
                onCellClick     = onCellClick
            )
        }
    }
}

@Composable
private fun WeekDayHeader(weekMonday: LocalDate, today: LocalDate) {
    val dayLabels = listOf("L", "M", "X", "J", "V", "S", "D")
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.width(32.dp))
        dayLabels.forEachIndexed { index, day ->
            val date    = weekMonday.plusDays(index.toLong())
            val isToday = date == today
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    day, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                    color = if (isToday) Primario else Terciario
                )
                Box(
                    modifier = Modifier.size(18.dp)
                        .background(if (isToday) Primario else Color.Transparent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${date.dayOfMonth}", fontSize = 9.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                        color      = if (isToday) Color.White else Terciario,
                        textAlign  = TextAlign.Center,
                        style      = TextStyle(
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim      = LineHeightStyle.Trim.Both
                            )
                        )
                    )
                }
            }
        }
        Spacer(Modifier.width(4.dp))
    }
}

@Composable
private fun HourRow(
    hour: Int,
    hourColumnWidth: androidx.compose.ui.unit.Dp,
    slots: List<TimeSlot>,
    weekMonday: LocalDate,
    onCellClick: (List<TimeSlot>, LocalDate, Int) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            "${hour}h", fontSize = 9.sp, color = Terciario.copy(alpha = 0.7f),
            modifier  = Modifier.width(hourColumnWidth).padding(end = 4.dp),
            textAlign = TextAlign.End
        )
        Row(modifier = Modifier.weight(1f)) {
            (1..7).forEach { dayNum ->
                val date      = weekMonday.plusDays((dayNum - 1).toLong())
                val cellSlots = getSlotsForCell(slots, dayNum, hour, date)
                val cellColor = when {
                    cellSlots.any { it.slotType == SlotType.TASK_BLOCKED } -> Color(0xFF2894e3).copy(alpha = 0.5f)
                    cellSlots.isNotEmpty()                                  -> Color(0xFFFF5757).copy(alpha = 0.5f)
                    else                                                    -> Color(0xFFF0F0F0)
                }
                Box(
                    modifier = Modifier
                        .weight(1f).height(14.dp)
                        .padding(horizontal = 1.dp, vertical = 1.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(cellColor)
                        .then(
                            if (cellSlots.isNotEmpty()) Modifier.clickable { onCellClick(cellSlots, date, hour) }
                            else Modifier
                        )
                )
            }
        }
        Spacer(Modifier.width(4.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LÓGICA DE CELDAS
// ─────────────────────────────────────────────────────────────────────────────

private fun getSlotsForCell(
    slots: List<TimeSlot>,
    dayNum: Int,
    hour: Int,
    date: LocalDate
): List<TimeSlot> {
    val zone      = java.time.ZoneId.systemDefault()
    val hourStart = hour * 60
    val hourEnd   = hourStart + 60

    return slots.filter { slot ->
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
    }.sortedWith(compareByDescending { it.slotType == SlotType.TASK_BLOCKED })
}

private data class PendingCell(
    val slots: List<TimeSlot>,
    val date: LocalDate,
    val hourStart: Int  // ej: 10 → "10:00"
)