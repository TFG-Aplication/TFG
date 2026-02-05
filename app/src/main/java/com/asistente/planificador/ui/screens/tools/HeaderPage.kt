package com.asistente.planificador.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*


import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asistente.core.domain.models.Calendar
import com.asistente.core.ui.viewmodels.CalendarViewModel
import com.asistente.planificador.ui.screens.ColorPrimario
import com.asistente.planificador.ui.screens.Primario
import com.asistente.planificador.ui.screens.tools.CalendarSelector


// Definimos un enum para gestionar las vistas
enum class CalendarView(val label: String, val icon: ImageVector) {
    MONTH("Vista Mes", Icons.Default.ViewModule),
    WEEK("Vista Semana", Icons.Default.ViewWeek)
}

@Composable
fun HeaderPage(
    viewModel: CalendarViewModel = hiltViewModel(),
    yearMonth: YearMonth,
    currentView: CalendarView,
    onViewChange: (CalendarView) -> Unit,
    onMonthSelected: (YearMonth) -> Unit,
    onCalendarChanged: (Calendar) -> Unit
) {
    var expandedViewMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Estado para controlar el diálogo de calendarios
    var showCalendarSelector by remember { mutableStateOf(false) }

    // Recolectamos datos del ViewModel
    val calendars by viewModel.calendarsList.collectAsStateWithLifecycle()
    val selectedCalendar by viewModel.selectedCalendar.collectAsStateWithLifecycle()

    // Diálogo de Mes/Año
    if (showDatePicker) {
        MonthYearPickerDialog(
            initialMonth = yearMonth,
            onDismiss = { showDatePicker = false },
            onConfirm = { selectedMonth ->
                onMonthSelected(selectedMonth)
                showDatePicker = false
            }
        )
    }

    if (showCalendarSelector) {
        CalendarSelector(
            calendars = calendars,
            selectedCalendar = selectedCalendar,
            onCalendarChanged = { selectedCalendar ->
                viewModel.onCalendarChanged(selectedCalendar)
                onCalendarChanged(selectedCalendar)
            },
            onDismiss = { showCalendarSelector = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween, // Reparte a los extremos
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selector de Calendario
            Row(
                modifier = Modifier
                    .clickable { showCalendarSelector = true }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedCalendar?.name ?: "Calendario",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp).padding(start = 2.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Box {
                    Icon(
                        imageVector = currentView.icon,
                        contentDescription = "Cambiar Vista",
                        tint = Primario,
                        modifier = Modifier.size(24.dp).clickable { expandedViewMenu = true }
                    )
                    DropdownMenu(
                        expanded = expandedViewMenu,
                        onDismissRequest = { expandedViewMenu = false }
                    ) {
                        CalendarView.values().forEach { view ->
                            DropdownMenuItem(
                                text = { Text(view.label) },
                                onClick = {
                                    onViewChange(view)
                                    expandedViewMenu = false
                                },
                                leadingIcon = { Icon(view.icon, contentDescription = null) }
                            )
                        }
                    }
                }
                Icon(Icons.Default.Search, null, tint = Primario, modifier = Modifier.size(24.dp))
                Icon(Icons.Outlined.ChatBubbleOutline, null, tint = Primario, modifier = Modifier.size(24.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- FILA DE MES Y AÑO ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
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

