package com.asistente.planificador.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asistente.planificador.ui.components.FootPage
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*


// Colores del diseño
val ColorPrimario = Color(0xFFAC5343)
val ColorGrisFondo = Color(0xFFEFEFEF)
val ColorGrisOscuro = Color(0xFF555555)

@Composable
fun CalendarScreen() {
    val coroutineScope = rememberCoroutineScope()
    var showDatePicker by remember { mutableStateOf(false) }

    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }

    // Obtenemos la altura de la pantalla de forma aproximada para las celdas
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val cellHeight = (screenHeight - 300.dp) / 6
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek,
        outDateStyle = OutDateStyle.EndOfGrid
    )

    var currentTab by remember { mutableStateOf("calendar") }

    if (showDatePicker) {
        MonthYearPickerDialog(
            initialMonth = state.firstVisibleMonth.yearMonth,
            onDismiss = { showDatePicker = false },
            onConfirm = { newMonth ->
                coroutineScope.launch { state.scrollToMonth(newMonth) }
                showDatePicker = false
            }
        )
    }

    Scaffold(
        bottomBar = {
            FootPage(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        },floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Aquí abrirás el formulario de eventos */ },
                containerColor = ColorPrimario, // Usamos tu color naranja
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 10.dp) // Pequeño ajuste para que no pegue al footer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir Evento",
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        // Esta línea es opcional, sirve para centrarlo o moverlo a un lado
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ColorGrisFondo)
                    .statusBarsPadding() // El padding va AQUÍ, dentro del fondo gris
            ) {
                CalendarHeader(
                    yearMonth = state.firstVisibleMonth.yearMonth,
                    onMonthClick = { showDatePicker = true }
                )
                DaysOfWeekTitle()
            }

            // Calendario
            HorizontalCalendar(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = state,

                dayContent = { day ->
                    Day(
                        day = day,
                        isSelected = selectedDate == day.date,
                        onClick = { date -> selectedDate = date },
                        cellHeight = cellHeight
                    )
                }
            )
        }
    }
}

@Composable
fun CalendarHeader(yearMonth: YearMonth, onMonthClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp)
    ) {
        // Fila de Iconos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Icon(Icons.Default.Menu, "Menú", tint = ColorPrimario, modifier = Modifier.size(24.dp).clickable { })
                Icon(Icons.Default.Search, "Buscar", tint = ColorPrimario, modifier = Modifier.size(24.dp).clickable { })
                Icon(Icons.Outlined.ChatBubbleOutline, "Notificaciones", tint = ColorPrimario, modifier = Modifier.size(24.dp).clickable { })
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Fila de Mes y Año (Clickable para abrir selector)
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onMonthClick() },
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
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
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
                .fillMaxSize() // Ocupa todo el espacio de la celda estirada
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

            // Este Spacer con weight(1f) es fundamental:
            // Empuja el contenido y hace que la celda "rellene" el hueco blanco
            Spacer(modifier = Modifier.weight(1f))

            // Aquí es donde aparecerán tus rayas de eventos
        }
    }
}

@Composable
fun DaysOfWeekTitle() {
    val days = listOf("L", "M", "X", "J", "V", "S", "D")
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
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

@Composable
fun MonthYearPickerDialog(
    initialMonth: YearMonth,
    onDismiss: () -> Unit,
    onConfirm: (YearMonth) -> Unit
) {
    val meses = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
    val años = (2020..2030).map { it.toString() }

    var selectedMonthIndex by remember { mutableStateOf(initialMonth.monthValue - 1) }
    var selectedYearIndex by remember { mutableStateOf(años.indexOf(initialMonth.year.toString())) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text("Seleccione mes y año",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 18.sp)
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WheelPicker(items = meses, initialIndex = selectedMonthIndex) {
                    selectedMonthIndex = it
                }
                Spacer(modifier = Modifier.width(20.dp))
                WheelPicker(items = años, initialIndex = selectedYearIndex) {
                    selectedYearIndex = it
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val newMonth = YearMonth.of(años[selectedYearIndex].toInt(), selectedMonthIndex + 1)
                onConfirm(newMonth)
            }) {
                Text("Confirmar", color = ColorPrimario, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    )
}

@Composable
fun WheelPicker(
    items: List<String>,
    initialIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val centerIndex = listState.firstVisibleItemIndex
            onItemSelected(centerIndex)
        }
    }

    Box(
        modifier = Modifier.height(150.dp).width(120.dp),
        contentAlignment = Alignment.Center
    ) {
        HorizontalDivider(modifier = Modifier.offset(y = (-25).dp), thickness = 1.dp, color = Color.LightGray)
        HorizontalDivider(modifier = Modifier.offset(y = (25).dp), thickness = 1.dp, color = Color.LightGray)

        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(vertical = 60.dp),
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(items) { index, item ->
                Text(
                    text = item,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = if (listState.firstVisibleItemIndex == index) Color.Black else Color.LightGray
                )
            }
        }
    }
}