package com.asistente.planificador.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

@Composable
fun SemanalScreen(
    viewModel: CalendarViewModel,
    onMonthChanged: (YearMonth) -> Unit,
    jumpToMonth: YearMonth? = null,
    onJumpFinished: () -> Unit = {}
) {
    val initialPage = 500
    val pagerState = rememberPagerState(pageCount = { 1000 }, initialPage = initialPage)
    val today = LocalDate.now()
    val coroutineScope = rememberCoroutineScope()

    // --- LÓGICA DE SALTO ---
    // Cuando jumpToMonth cambia (desde el Header), movemos el pager
    LaunchedEffect(jumpToMonth) {
        jumpToMonth?.let { targetMonth ->
            val targetDate = targetMonth.atDay(1)
            // Calculamos la diferencia en semanas entre la semana actual y el destino
            val currentWeekStartReal = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
            val targetWeekStart = targetDate.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))

            val weeksDiff = ChronoUnit.WEEKS.between(currentWeekStartReal, targetWeekStart)

            // Hacemos el salto
            pagerState.scrollToPage(initialPage + weeksDiff.toInt())

            // Avisamos al MainCalendar que ya terminó para resetear la variable
            onJumpFinished()
        }
    }

    // Fecha que se muestra actualmente según el scroll del Pager
    val currentWeekStart = remember(pagerState.currentPage) {
        val weeksDiff = (pagerState.currentPage - initialPage).toLong()
        today.plusWeeks(weeksDiff).with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
    }

    // Notificar al Header que el mes cambió al deslizar
    LaunchedEffect(currentWeekStart) {
        onMonthChanged(YearMonth.from(currentWeekStart))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Cabecera de días (L M X...)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ColorGrisFondo)
        ) {
            WeekHeaderRow(currentWeekStart, today)
        }

        // Deslizamiento lateral de semanas
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) { page ->
            val weeksDiff = (page - initialPage).toLong()
            val weekStart = today.plusWeeks(weeksDiff).with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))

            WeeklyTimeGrid(weekStart)
        }
    }
}

@Composable
fun WeekHeaderRow(startOfWeek: LocalDate, realToday: LocalDate) {
    val days = listOf("L", "M", "X", "J", "V", "S", "D")
    Column(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(50.dp))
            days.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (day == "S" || day == "D") Color(0xFF555555) else Color(0xFFAC5343)
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 5.dp)) {
            Spacer(modifier = Modifier.width(50.dp))
            for (i in 0..6) {
                val date = startOfWeek.plusDays(i.toLong())
                val isToday = date == realToday

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (isToday) {
                        Surface(modifier = Modifier.size(30.dp), color = Color(0xFFAC5343), shape = CircleShape) {}
                    }
                    Text(
                        text = date.dayOfMonth.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isToday) Color.White else Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyTimeGrid(startOfWeek: LocalDate) {
    val verticalScrollState = rememberScrollState()
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    val realToday = LocalDate.now()

    // Actualizar hora cada minuto
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(60_000)
        }
    }

    // Auto-scroll a la hora actual al entrar (solo si es la semana actual)
    LaunchedEffect(Unit) {
        if (realToday >= startOfWeek && realToday <= startOfWeek.plusDays(6)) {
            val scroll= (currentTime.hour * 60 * 2.5).toInt()
            verticalScrollState.animateScrollTo(scroll)
        }
    }

    Box(modifier = Modifier.fillMaxSize().verticalScroll(verticalScrollState)) {
        // Cuadrícula semana
        Column {
            for (hour in 0..23) {
                Row(modifier = Modifier.fillMaxWidth().height(60.dp)) {
                    Text(
                        text = String.format("%02d:00", hour),
                        modifier = Modifier
                            .width(50.dp)
                            .fillMaxHeight()
                            .background(ColorGrisFondo)
                            .padding(start = 8.dp, top = 2.dp),

                        fontSize = 13.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (dayIndex in 0..6) {
                            val dateOfColumn = startOfWeek.plusDays(dayIndex.toLong())
                            val isToday = dateOfColumn == realToday
                            Box(modifier = Modifier.weight(1f)
                                .fillMaxHeight()
                                .background(
                                    if (isToday) Color(0xFFfffaf9) else Color.Transparent
                                )
                                .border(0.5.dp, Color(0xFFE0E0E0)))
                        }
                    }
                }
            }
        }

        // Línea tiempo real
        val endOfWeek = startOfWeek.plusDays(6)
        if (!realToday.isBefore(startOfWeek) && !realToday.isAfter(endOfWeek)) {
            val yOffset = (currentTime.hour * 60).dp + (currentTime.minute).dp //calculo de la posición vertical (1 min = 1dp)
            val dayOfWeekIndex = realToday.dayOfWeek.value - 1

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = yOffset - 5.dp)
                    .height(10.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(50.dp))


                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        // 1. Calculamos el ancho de 1 día (la pantalla dividida entre 7)
                        val colWidth = maxWidth / 7
                        // 2. Calculamos cuánto espacio saltar desde la izquierda
                        val startPadding = colWidth * dayOfWeekIndex.toFloat()

                        Box(modifier = Modifier.fillMaxWidth()) {
                            HorizontalDivider(
                                modifier = Modifier
                                    .offset(x = startPadding)
                                    .width(colWidth),
                                thickness = 2.dp,
                                color = ColorPrimario
                            )

                            Surface(
                                modifier = Modifier
                                    .offset(x = startPadding - 5.dp)
                                    .size(10.dp),
                                color = ColorPrimario,
                                shape = CircleShape
                            ) {}
                        }
                    }

                }
            }
        }
    }
}