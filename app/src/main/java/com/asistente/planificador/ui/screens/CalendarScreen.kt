package com.asistente.planificador.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

// Colores del diseño
val ColorPrimario = Color(0xFFAC5343)
val ColorGrisFondo = Color(0xFFF2F2F2)
val ColorGrisOscuro = Color(0xFF555555)
@Composable
fun CalendarScreen() {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }

    // Estado para la fecha seleccionada
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ColorGrisFondo) // Fondo igual al pie de página
        ) {
            CalendarHeader(state.firstVisibleMonth.yearMonth)
            DaysOfWeekTitle()
        }

        // Calendario con scroll horizontal
        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                Day(
                    day = day,
                    isSelected = selectedDate == day.date,
                    onClick = { date -> selectedDate = date }
                )
            }
        )

        // Panel inferior (Espacio para tareas del día)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorGrisFondo),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = "TODAY",
                modifier = Modifier.padding(top = 20.dp),
                color = ColorPrimario,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun CalendarHeader(yearMonth: YearMonth) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menú",
                    tint = ColorPrimario, // Aquí aplicas tu color anaranjado
                    modifier = Modifier.size(24.dp).clickable { /* Menu */ }
                )
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = ColorPrimario,
                    modifier = Modifier.size(24.dp).clickable { /* Buscar */ }
                )
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir",
                    tint = ColorPrimario,
                    modifier = Modifier.size(28.dp).clickable { /* Añadir */ }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Segunda línea: Mes y Año juntos en la misma línea y en Mayúsculas
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = yearMonth.month.getDisplayName(TextStyle.FULL, Locale("es", "ES")).uppercase(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = yearMonth.year.toString(),
                fontSize = 18.sp, // Un poco más pequeño que el mes
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 4.dp) // Alineación visual con la base del texto grande
            )
        }
    }
}

@Composable
fun Day(day: CalendarDay, isSelected: Boolean, onClick: (LocalDate) -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(
                if (day.position != DayPosition.MonthDate) ColorGrisFondo.copy(alpha = 0.5f)
                else Color.Transparent
            )
            .clickable { onClick(day.date) },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(ColorPrimario, shape = CircleShape)
            )
        }

        Text(
            text = day.date.dayOfMonth.toString(),
            color = when {
                isSelected -> Color.White
                day.position == DayPosition.MonthDate -> Color.Black
                else -> Color.LightGray
            },
            fontWeight = if (day.position == DayPosition.MonthDate) FontWeight.Medium else FontWeight.Normal
        )

        // Línea divisoria inferior
        HorizontalDivider(
            modifier = Modifier.align(Alignment.BottomCenter),
            thickness = 0.5.dp,
            color = Color.LightGray.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun DaysOfWeekTitle() {
    val days = listOf("L", "M", "X", "J", "V", "S", "D")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        for (day in days) {
            // Verificamos si es Sábado o Domingo
            val textColor = if (day == "S" || day == "D") {
                ColorGrisOscuro
            } else {
                ColorPrimario
            }

            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = day,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 12.sp
            )
        }
    }
}