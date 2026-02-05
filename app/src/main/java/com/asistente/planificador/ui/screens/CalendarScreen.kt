package com.asistente.planificador.ui.screens

import android.graphics.drawable.shapes.OvalShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.models.Task
import com.asistente.core.ui.viewmodels.CalendarViewModel
import com.asistente.planificador.ui.screens.tools.darkenColor
import com.google.type.DayOfWeek
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import java.time.LocalDate
import java.time.YearMonth
import kotlin.collections.groupBy


// Colores del diseño
val ColorPrimario = Color(0xFFAC5343)
val ColorGrisFondo = Color(0xFFEFEFEF)
val ColorGrisOscuro = Color(0xFF555555)

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
    onMonthChanged: (YearMonth) -> Unit,
    jumpToMonth: YearMonth? = null,
    onJumpFinished: () -> Unit = {},

) {
    // las tareas del calendario seleccionado
    val tasks by viewModel.taskList.collectAsState()

    // Agrupamos las tareas por fecha
    val tasksByDate = taskByDate(tasks)

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
            val dayTasks = tasksByDate[day.date] ?: emptyList()

            Day(
                day = day,
                isSelected = selectedDate == day.date,
                tasks = dayTasks,
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
    tasks: List<Pair<Task, Int>>, // lista de tareas
    getTaskColor: suspend (String?) -> Color,
    onClick: (LocalDate) -> Unit,
    cellHeight: Dp
) {
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
                    .padding(vertical = 2.dp)
                    .weight(1f),

            ) {
                // num de representaciones
                val numTaskPerDay = when {
                    tasks.size <= 5 -> tasks.size
                    else -> 4
                }
                tasks.take(numTaskPerDay).forEach { task ->
                    val startDate = task.first.init_date?.toInstant()?.atZone(java.time.ZoneId.systemDefault())?.toLocalDate()
                    val finished = task.first.finish_date?.toInstant()?.atZone(java.time.ZoneId.systemDefault())?.toLocalDate()
                    val color by produceState(initialValue = colorCuarto, task.first.categoryId) {
                        value = getTaskColor(task.first.categoryId)
                    }

                    // forma de la tarea segun su duracion
                    val taskShape = when {
                    // Tarea de un solo día
                    startDate == day.date && finished == day.date -> RoundedCornerShape(4.dp)
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
                            .offset(y=(task.second*12).dp)
                            .background(
                                color =color,
                                shape = taskShape
                            ),

                        contentAlignment = Alignment.Center
                    ) {
                        if(day.date == startDate || day.date.dayOfWeek == java.time.DayOfWeek.MONDAY) {
                            Text(
                                text = task.first.name,
                                fontSize = 8.sp,
                                maxLines = 1,
                                color = darkenColor(color),
                                fontWeight = FontWeight.Bold,
                                lineHeight = 9.sp,
                                textAlign = TextAlign.Center,
                                style = LocalTextStyle.current.copy(
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false // Elimina el espacio extra inferior
                                    )
                                )
                            )
                        }
                    }
                }
                if (tasks.size > 5) {
                    Text(
                        text = "+${tasks.size - 5}",
                        fontSize = 12.sp,
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

fun taskByDate(tasks: List<Task>): Map<LocalDate, List<Pair<Task, Int>>>{
    val map = mutableMapOf<LocalDate, MutableList<Pair<Task, Int>>>()
    // ordena para que las tareas mas largas se ven las primeras
    var sortedTask = tasks.sortedBy{ task ->
        var initDate = task.init_date?.time ?: 0L
        initDate

    }
    sortedTask.forEach { task ->
        val startDate = task.init_date?.toInstant()?.atZone(java.time.ZoneId.systemDefault())?.toLocalDate()
        val finishDate = task.finish_date?.toInstant()?.atZone(java.time.ZoneId.systemDefault())?.toLocalDate()
        // por cada dia que dure la tarea A se añade al diccionario dia, tarea A


        if (startDate != null && finishDate != null) {

            var dateIter: LocalDate = startDate
            while( !dateIter.isAfter(finishDate)) {
                //ver si esa tarea empieza o no -> si empieza asignar primer row libre -> si continua asignar el mismo
                if(dateIter == startDate){
                    // buscar el primer row libre
                    var row = 0
                    while (true) {
                        val same = map[dateIter]?.any { it.second == row }
                        if (same == true) {
                            row++
                        }
                        else{
                            break

                        }
                    }
                    map.getOrPut(dateIter) { mutableListOf() }.add(Pair(task, row))

                }
                else { // una tarea q no empieza el mismo dia del iter es q como min empieza el dia anterior
                    // ver q valor tenia el dia anterior
                   map[dateIter.minusDays(1)]?.find {it.first == task}?.second?.let { row ->
                       map.getOrPut(dateIter) { mutableListOf() }.add(Pair(task, row))
                   }

                }


                dateIter = dateIter.plusDays(1)
            }
            }
        }
    // tareas ordenadas por tamaño y row
    return map.mapValues { entry -> entry.value.sortedBy { it.second } }
    }

