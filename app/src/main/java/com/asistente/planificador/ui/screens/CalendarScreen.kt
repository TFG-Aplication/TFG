package com.asistente.planificador.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.asistente.core.domain.models.Task
import com.asistente.core.ui.viewmodels.CalendarViewModel
import com.asistente.planificador.ui.screens.tools.darkenColor
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import java.time.LocalDate
import java.time.YearMonth


// Colores del diseño
val ColorPrimario = Color(0xFFAC5343)
val ColorGrisFondo = Color(0xFFEFEFEF)
val ColorGrisOscuro = Color(0xFF555555)

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
    onMonthChanged: (YearMonth) -> Unit,
    jumpToMonth: YearMonth? = null,
    onJumpFinished: () -> Unit = {}

) {
    // las tareas del calendario seleccionado
    val tasks by viewModel.taskList.collectAsState()

    // Agrupamos las tareas por fecha
    val tasksByDate = remember(tasks) {
        taskByDate(tasks)
    }

    val currentMonth = remember { YearMonth.now() }
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }

    val state = rememberCalendarState(
        startMonth = currentMonth.minusMonths(100),
        endMonth = currentMonth.plusMonths(100),
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeekFromLocale(),
        outDateStyle = OutDateStyle.EndOfGrid
    )

    // Sincronización de mes
    LaunchedEffect(jumpToMonth) {
        jumpToMonth?.let {
            state.scrollToMonth(it)
            onJumpFinished()
        }
    }

    LaunchedEffect(state.firstVisibleMonth) {
        onMonthChanged(state.firstVisibleMonth.yearMonth)
    }

    HorizontalCalendar(
        modifier = Modifier.fillMaxSize(),
        state = state,
        dayContent = { day ->
            Day(
                day = day,
                isSelected = selectedDate == day.date,
                tasks = tasksByDate[day.date].orEmpty(),
                getTaskColor = { categoryId -> viewModel.getCategoryColor(categoryId) },
                onClick = { date -> selectedDate = date },
                cellHeight = 100.dp
            )
        }
    )
}

@Composable
fun Day(
    day: CalendarDay,
    isSelected: Boolean, //usar para ver pantalla dia
    tasks: List<Pair<Task, Int>>,
    getTaskColor: suspend (String?) -> Color,
    onClick: (LocalDate) -> Unit,
    cellHeight: Dp
) {
    val maxVisible = 4

    Box(
        modifier = Modifier
            .height(cellHeight)
            .background(if (day.position == DayPosition.MonthDate) Color.White else Color(0xFFF9F9F9))
            .clickable { onClick(day.date) }
    ) {
        HorizontalDivider(
            modifier = Modifier.align(Alignment.BottomCenter),
            thickness = 0.5.dp,
            color = Color(0xFFE0E0E0)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Número del día
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp)) {
                if(day.date == LocalDate.now()){
                    Box(modifier = Modifier.size(24.dp).background(ColorPrimario, CircleShape))
                }
                Text(
                    text = day.date.dayOfMonth.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = if (day.date == LocalDate.now()) Color.White else if (day.position == DayPosition.MonthDate) Color.Black else Color.LightGray
                )
            }

            // --- INDICADORES DE TAREAS ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                tasks.filter { it.second < maxVisible }.forEach { (task, row) ->
                        val startDate = task.init_date?.toInstant()?.atZone(java.time.ZoneId.systemDefault())?.toLocalDate()
                        val finished = task.finish_date?.toInstant()?.atZone(java.time.ZoneId.systemDefault())?.toLocalDate()
                        val color by produceState(Color.LightGray, task.categoryId) {
                            value = getTaskColor(task.categoryId)
                        }

                        // forma de la tarea segun su duracion
                        val taskShape = when {
                        // Tarea de un solo día
                        startDate == day.date && finished == day.date -> RoundedCornerShape(4.dp)
                        startDate == day.date && day.date.dayOfWeek == java.time.DayOfWeek.SUNDAY -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                        finished == day.date && day.date.dayOfWeek == java.time.DayOfWeek.MONDAY -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 4.dp, bottomEnd = 4.dp)

                            // inicio tarea larga
                        startDate == day.date -> RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                        // fin tarea larga
                        finished == day.date -> RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)

                        day.date.dayOfWeek == java.time.DayOfWeek.MONDAY -> RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                        day.date.dayOfWeek == java.time.DayOfWeek.SUNDAY -> RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)


                            // día intermedio
                        else -> RoundedCornerShape(0.dp)
                    }

                        //margenes laterales en los dias
                        val margenes = when {
                            startDate == day.date && finished == day.date -> Modifier.padding(horizontal = 4.dp)
                            startDate == day.date && day.date.dayOfWeek == java.time.DayOfWeek.SUNDAY -> Modifier.padding(start = 4.dp, end = 4.dp)
                            finished == day.date && day.date.dayOfWeek == java.time.DayOfWeek.MONDAY -> Modifier.padding(start = 4.dp, end = 4.dp)
                            startDate == day.date -> Modifier.padding(start = 4.dp)
                            finished == day.date -> Modifier.padding(end = 4.dp)
                            day.date.dayOfWeek == java.time.DayOfWeek.MONDAY -> Modifier.padding(start = 4.dp)
                            day.date.dayOfWeek == java.time.DayOfWeek.SUNDAY -> Modifier.padding(end = 4.dp)
                            else -> Modifier // Sin padding para que toque los bordes
                        }

                        Box(
                            modifier = Modifier
                                .then(margenes)
                                .fillMaxWidth()
                                .height(10.dp)
                                .offset(y = (row * 12).dp)
                                .background(
                                    color =color,
                                    shape = taskShape
                                ).padding(start = 4.dp),

                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (day.date == startDate || day.date.dayOfWeek == java.time.DayOfWeek.MONDAY) {
                                Text(
                                    text = task.name,
                                    fontSize = 9.sp,
                                    maxLines = 1,
                                    color = darkenColor(color),
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 9.sp,
                                    textAlign = TextAlign.Left,
                                    style = LocalTextStyle.current.copy(
                                        platformStyle = PlatformTextStyle(
                                            includeFontPadding = false // Elimina el espacio extra inferior
                                        )
                                    )
                                )
                            }
                        }
                    }

                val hidden = tasks.count { it.second >= maxVisible }
                if (hidden > 0) {
                    Text(
                        text = "+$hidden",
                        fontSize = 8.sp,
                        color = Terciario,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}

@Composable
fun DaysOfWeekTitle() {
    val days = listOf("L", "M", "X", "J", "V", "S", "D")
    Row(modifier = Modifier
        .fillMaxWidth()
        .background(ColorGrisFondo)
        .padding(vertical = 10.dp)) {
        for (day in days) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = day,
                fontWeight = FontWeight.Bold,
                color = if (day == "S" || day == "D") ColorGrisOscuro else ColorPrimario,
                fontSize = 12.sp
            )
        }
    }
}

fun taskByDate(tasks: List<Task>): Map<LocalDate, List<Pair<Task, Int>>> {
    val result = mutableMapOf<LocalDate, MutableList<Pair<Task, Int>>>()
    val zone = java.time.ZoneId.systemDefault()

    data class TaskRange(
        val task: Task,
        val start: LocalDate,
        val end: LocalDate
    )

    // ordenar x  por fecha de inicio 1º
    // luego las más largas
    val ranges = tasks.mapNotNull { task ->
        val start = task.init_date?.toInstant()?.atZone(zone)?.toLocalDate()
        val end = task.finish_date?.toInstant()?.atZone(zone)?.toLocalDate()
        if (start != null && end != null) TaskRange(task, start, end) else null
    }.sortedWith(
        compareBy<TaskRange> { it.start }
            .thenByDescending { java.time.temporal.ChronoUnit.DAYS.between(it.start, it.end) }
    )

    val rowEndDates = mutableListOf<LocalDate>()

    ranges.forEach { range ->
        var assignedRow = -1

        // 3Buscamos la primera fila libre para toda la duración
        for (i in rowEndDates.indices) {
            if (range.start.isAfter(rowEndDates[i])) {
                assignedRow = i
                rowEndDates[i] = range.end
                break
            }
        }

        // Si no hay fila libre, creamos una nueva
        if (assignedRow == -1) {
            assignedRow = rowEndDates.size
            rowEndDates.add(range.end)
        }

        // Asignamos la tarea a todos los días manteniendo la MISMA fila
        var date = range.start
        while (!date.isAfter(range.end)) {
            result.getOrPut(date) { mutableListOf() }
                .add(range.task to assignedRow)
            date = date.plusDays(1)
        }
    }

    return result
}
