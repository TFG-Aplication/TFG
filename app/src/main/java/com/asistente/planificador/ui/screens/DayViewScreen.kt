package com.asistente.planificador.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.asistente.core.domain.models.Task
import com.asistente.core.ui.viewmodels.CalendarViewModel
import com.asistente.planificador.ui.components.CalendarView
import com.asistente.planificador.ui.components.FootPage
import com.asistente.planificador.ui.components.HeaderPage
import com.asistente.planificador.ui.screens.tools.darkenColor
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DayViewScreen(
    date: LocalDate,
    viewModel: CalendarViewModel,
    onNavigateToDetail: (String) -> Unit
) {
    val initialPage = Int.MAX_VALUE / 2
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = initialPage,
        pageCount = { Int.MAX_VALUE }
    )

    val tasks by viewModel.taskList.collectAsState()
    val zone = ZoneId.systemDefault()

    androidx.compose.foundation.pager.HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        val pageDate = date.plusDays((page - initialPage).toLong())

        val dayTasks = remember(tasks, pageDate) {
            tasks.filter { task ->
                val start = task.init_date?.toInstant()?.atZone(zone)?.toLocalDate()
                val end = task.finish_date?.toInstant()?.atZone(zone)?.toLocalDate()
                start != null && end != null && !pageDate.isBefore(start) && !pageDate.isAfter(end)
            }
        }

        val allDayTasks = dayTasks.filter {
            isAllDayTask(it) || (it.init_date?.toInstant()?.atZone(zone)?.toLocalDate() != it.finish_date?.toInstant()?.atZone(zone)?.toLocalDate())
        }
        val timedTasks = dayTasks.filter { !allDayTasks.contains(it) }

        Column(modifier = Modifier.fillMaxSize()) {
            DayHeaderBand(
                date = pageDate,
                allDayTasks = allDayTasks,
                getTaskColor = { viewModel.getCategoryColor(it) },
                onTaskClick = { onNavigateToDetail(it.id) }
            )
            HourGrid(
                date = pageDate,
                tasks = timedTasks,
                getTaskColor = { viewModel.getCategoryColor(it) },
                onTaskClick = { onNavigateToDetail(it.id) }
            )
        }
    }
}

@Composable
private fun DayHeaderBand(
    date: LocalDate,
    allDayTasks: List<Task>,
    getTaskColor: suspend (String?) -> Color,
    onTaskClick: (Task) -> Unit
) {
    val isToday = date == LocalDate.now()
    val dayAbbr = date.dayOfWeek
        .getDisplayName(java.time.format.TextStyle.SHORT, Locale("es"))
        .uppercase().take(3)
    val dayNum = date.dayOfMonth

    var isExpanded by remember { mutableStateOf(false) }
    val limit = 2
    val hasMore = allDayTasks.size > limit
    val displayedTasks = if (isExpanded || !hasMore) allDayTasks else allDayTasks.take(limit)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Secundario)
            .padding(top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // ── Columna izquierda: abreviatura + número ──
        Column(
            modifier = Modifier.width(56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = dayAbbr,
                fontSize = 11.sp,
                color = Primario,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 14.sp
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(32.dp)
            ) {
                if (isToday) Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Primario, CircleShape)
                )
                Text(
                    text = dayNum.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isToday) Color.White else Primario
                )
            }

            // Flecha SOLO cuando hay más de 2 tareas, alineada con el "+X"
            if (hasMore) {
                Spacer(modifier = Modifier.height(15.dp)) // ← empuja la flecha hacia abajo
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier
                        .size(28.dp) // ← más grande (era 20.dp)
                        .clickable { isExpanded = !isExpanded },
                    tint = Terciario
                )
            }
        }

        // ── Columna derecha: tareas + expansor ──
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            when {
                // Sin tareas: spacer mínimo para mantener altura del número
                allDayTasks.isEmpty() -> {
                    Spacer(modifier = Modifier.height(32.dp))
                }
                // Con tareas
                else -> {
                    displayedTasks.forEach { task ->
                        AllDayTaskChip(task, getTaskColor, onTaskClick)
                    }

                    // "+X" alineado verticalmente con la flecha de la columna izquierda
                    if (hasMore) {
                        Text(
                            text = if (isExpanded) "Ver menos" else "+${allDayTasks.size - limit}",
                            fontSize = 11.sp,
                            color = Terciario,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .clickable { isExpanded = !isExpanded }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HourGrid(
    date: LocalDate,
    tasks: List<Task>,
    getTaskColor: suspend (String?) -> Color,
    onTaskClick: (Task) -> Unit
) {
    val hourHeightDp = 58.dp
    val labelWidthDp = 56.dp
    val zone = ZoneId.systemDefault()
    val density = LocalDensity.current
    val hourHeightPx = with(density) { hourHeightDp.toPx() }
    val initialScrollPx = (maxOf(0, java.time.LocalTime.now().hour - 1) * hourHeightPx).toInt()
    val scrollState = rememberScrollState(initial = initialScrollPx)
    val columns = remember(tasks) { assignColumns(tasks, zone) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Etiquetas de hora — alineadas con la línea (TopEnd con offset negativo para centrar en la línea)
        // Etiquetas de hora — TopEnd con offset negativo para alinear con la línea
        Column(
            modifier = Modifier
                .width(labelWidthDp)
                .height(hourHeightDp * 24)
                .background(Secundario)
        ) {
            for (hour in 0..23) {
                Box(
                    modifier = Modifier
                        .height(hourHeightDp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "%d:00".format(hour),
                        fontSize = 11.sp,                    // ← más pequeño
                        color = Terciario,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .offset(y = (-7).dp)             // ← sube el texto para centrar sobre la línea
                    )
                }
            }
        }

        // Rejilla — fondo blanco puro
        Box(
            modifier = Modifier
                .weight(1f)
                .height(hourHeightDp * 24)
                .background(Color.White) // ← fondo blanco
        ) {
            // Líneas de separación más gruesas y en color Terciario, alineadas con las etiquetas
            for (hour in 0..23) {
                Box(
                    modifier = Modifier
                        .offset(y = hourHeightDp * hour)
                        .fillMaxWidth()
                        .height(1.5.dp) // ← más gruesas
                        .background(Secundario) // ← color Terciario semitransparente
                )
            }

            // Línea de hora actual
            if (date == LocalDate.now()) {
                val now = java.time.LocalTime.now()
                val nowOffset: Dp = hourHeightDp * (now.hour + now.minute / 60f)

                Box(
                    modifier = Modifier
                        .offset(y = nowOffset)
                        .fillMaxWidth()
                        .zIndex(2f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(Primario)
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .offset(x = (-5).dp, y = (-4).dp)
                            .background(Primario, CircleShape)
                    )
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val totalWidth = maxWidth
                columns.forEach { (task, col, totalCols) ->
                    TaskBlockInGrid(
                        task = task,
                        col = col,
                        totalCols = totalCols,
                        totalWidth = totalWidth,
                        hourHeightDp = hourHeightDp,
                        zone = zone,
                        getTaskColor = getTaskColor,
                        onTaskClick = onTaskClick,
                        verticalOffset = 0.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun AllDayTaskChip(
    task: Task,
    getTaskColor: suspend (String?) -> Color,
    onTaskClick: (Task) -> Unit
) {
    val color by produceState(Primario, task.categoryId) {
        value = getTaskColor(task.categoryId)
    }

    // En la foto el texto es blanco sobre fondo de color
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .clickable { onTaskClick(task) }
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = task.name,
            fontSize = 13.sp,
            color = darkenColor(color),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}



@Composable
private fun TaskBlockInGrid(
    task: Task,
    col: Int,
    totalCols: Int,
    totalWidth: Dp,
    hourHeightDp: Dp,
    zone: ZoneId,
    getTaskColor: suspend (String?) -> Color,
    onTaskClick: (Task) -> Unit,
    verticalOffset: Dp = 0.dp
) {
    val color by produceState(Color.LightGray, task.categoryId) {
        value = getTaskColor(task.categoryId)
    }
    val textColor = darkenColor(color)

    val startInst = task.init_date?.toInstant()?.atZone(zone)?.toLocalTime() ?: return
    val endInst = task.finish_date?.toInstant()?.atZone(zone)?.toLocalTime() ?: return

    val startFraction = startInst.hour + startInst.minute / 60f
    val endFraction = endInst.hour + endInst.minute / 60f
    val durationH = (endFraction - startFraction).coerceAtLeast(0.25f)

    // El ancho se calcula sobre el total disponible en la columna blanca
    val blockWidth = totalWidth / totalCols
    val blockLeft = blockWidth * col

    Box(
        modifier = Modifier
            .offset(x = blockLeft, y = (hourHeightDp * startFraction) + verticalOffset)
            .width(blockWidth)
            .height(hourHeightDp * durationH)
            .padding(horizontal = 1.dp, vertical = 0.5.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .clickable { onTaskClick(task) }
            .padding(4.dp)
    ) {
        Text(
            text = task.name,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Bloque individual de tarea con hora
// ─────────────────────────────────────────────────────────────



// ─────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────

private fun isAllDayTask(task: Task): Boolean {
    val zone = ZoneId.systemDefault()
    val start = task.init_date?.toInstant()?.atZone(zone)
    val end = task.finish_date?.toInstant()?.atZone(zone)
    return start != null && end != null &&
            start.hour == 0 && start.minute == 0 &&
            (end.hour == 23 && end.minute >= 58 || end.hour == 0 && end.minute == 0)
}

private fun assignColumns(
    tasks: List<Task>,
    zone: ZoneId
): List<Triple<Task, Int, Int>> {
    data class Slot(val task: Task, var col: Int)

    val sorted = tasks.sortedBy { it.init_date }
    val slots = mutableListOf<Slot>()
    val groups = mutableListOf<MutableList<Slot>>()

    sorted.forEach { task ->
        val taskStart = task.init_date?.toInstant()?.atZone(zone)?.toLocalTime()
        val taskEnd = task.finish_date?.toInstant()?.atZone(zone)?.toLocalTime()

        val overlapping = groups.lastOrNull { group ->
            group.any { slot ->
                val s = slot.task.init_date?.toInstant()?.atZone(zone)?.toLocalTime()
                val e = slot.task.finish_date?.toInstant()?.atZone(zone)?.toLocalTime()
                s != null && e != null && taskStart != null && taskEnd != null &&
                        taskStart.isBefore(e) && taskEnd.isAfter(s)
            }
        }

        val usedCols = overlapping?.map { it.col }?.toSet() ?: emptySet()
        val col = (0..10).first { it !in usedCols }
        val slot = Slot(task, col)
        slots.add(slot)

        if (overlapping != null) overlapping.add(slot)
        else groups.add(mutableListOf(slot))
    }

    return slots.map { slot ->
        val group = groups.first { it.contains(slot) }
        Triple(slot.task, slot.col, group.maxOf { it.col } + 1)
    }
}