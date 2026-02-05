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
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.models.Task
import com.asistente.core.ui.viewmodels.CalendarViewModel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
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
    // Obtenemos las tareas del calendario seleccionado
    val tasks by viewModel.taskList.collectAsState()

    // Agrupamos las tareas por fecha para un acceso rápido (O(1))
    val tasksByDate = remember(tasks) {
            tasks.groupBy { task ->
                task.init_date?.let { date ->
                    date.toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                }
            }
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
    isSelected: Boolean,
    tasks: List<Task>, // lista de tareas
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
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Número del día
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp)) {
                if (isSelected) {
                    Box(modifier = Modifier.fillMaxSize().background(ColorPrimario, CircleShape))
                }
                Text(
                    text = day.date.dayOfMonth.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else if (day.position == DayPosition.MonthDate) Color.Black else Color.LightGray
                )
            }

            // --- INDICADORES DE TAREAS ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)

            ) {
                // Pintamos hasta 3 tareas para no saturar la celda
                tasks.take(3).forEach { task ->

                    val color by produceState(initialValue = colorCuarto, task.categoryId) {
                        value = getTaskColor(task.categoryId)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(
                                color =color,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = task.name,
                            fontSize = 8.sp,
                            maxLines = 1,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            style = LocalTextStyle.current.copy(
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false // Elimina el espacio extra inferior
                                ), baselineShift = BaselineShift(0.2f)
                            )  )
                    }
                }
                if (tasks.size > 3) {
                    Text(
                        text = "+${tasks.size - 3}",
                        fontSize = 12.sp,
                        color = Terciario,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
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

