package com.asistente.planificador.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asistente.core.ui.viewmodels.CalendarViewModel
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
    viewModel: CalendarViewModel,
    onMonthChanged: (YearMonth) -> Unit,
    jumpToMonth: YearMonth? = null,
    onJumpFinished: () -> Unit = {}
) {
    val currentMonth = remember { YearMonth.now() }
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    val state = rememberCalendarState(
        startMonth = currentMonth.minusMonths(100),
        endMonth = currentMonth.plusMonths(100),
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeekFromLocale(),
        outDateStyle = OutDateStyle.EndOfGrid
    )

    // Escucha si el Header mandó una orden de saltar a un mes
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
                onClick = { date -> selectedDate = date },
                cellHeight = 100.dp
            )        }
    )
}
@Composable
fun Day(day: CalendarDay, isSelected: Boolean, onClick: (LocalDate) -> Unit, cellHeight: androidx.compose.ui.unit.Dp) {
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
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(32.dp)
            ) {
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

            Spacer(modifier = Modifier.weight(1f))

            // Aquí es donde aparecerán los eventos
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

