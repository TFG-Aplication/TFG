package com.asistente.planificador.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.asistente.core.ui.viewmodels.CalendarViewModel
import com.asistente.planificador.ui.components.CalendarView
import com.asistente.planificador.ui.components.FootPage
import com.asistente.planificador.ui.components.HeaderPage
import java.time.YearMonth

@Composable
fun MainCalendar(viewModel: CalendarViewModel) {
    var currentView by remember { mutableStateOf(CalendarView.MONTH) }
    var visibleMonth by remember { mutableStateOf(YearMonth.now()) }
    var currentTab by remember { mutableStateOf("calendar") }

    var monthToJump by remember { mutableStateOf<YearMonth?>(null) }

    var expandedMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            // Header
            Column(
                modifier = Modifier
                    .background(Color(0xFFEFEFEF))
                    .statusBarsPadding()
            ) {
                HeaderPage(
                    yearMonth = visibleMonth,
                    currentView = currentView,
                    onViewChange = { currentView = it },
                    onMonthSelected = { selected ->
                        monthToJump = selected
                    }
                )
            }
        },
        bottomBar = {
            FootPage(currentTab = currentTab, onTabSelected = { currentTab = it })
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Solo mostramos los botones pequeños si el menú está abierto
                if (expandedMenu) {

                    FabMenuItem(label = "Trabajo", icon = Icons.Default.Work) { /* Acción */ }
                    FabMenuItem(label = "Recordatorio", icon = Icons.Default.Lightbulb) { /* Acción */ }
                    FabMenuItem(label = "Cumpleaños", icon = Icons.Default.Cake) { /* Acción */ }
                    FabMenuItem(label = "Actividad", icon = Icons.Default.Assignment) { /* Acción */ }
                    FabMenuItem(label = "Tarea", icon = Icons.Default.CheckCircle) { /* Acción */ }
                }

                // Botón Principal (El que tiene la X o el +)
                FloatingActionButton(
                    onClick = { expandedMenu = !expandedMenu },
                    containerColor = Color(0xFFAC5343),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = if (expandedMenu) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Menu"
                    )
                }
            }
        }
    ) { pad ->
        Box(modifier = Modifier.padding(pad)) {
            if (currentView == CalendarView.MONTH) {
                // Vista Mensual
                Column {
                    DaysOfWeekTitle()
                    CalendarScreen(
                        viewModel = viewModel,
                        onMonthChanged = { visibleMonth = it },
                        jumpToMonth = monthToJump,
                        onJumpFinished = { monthToJump = null }
                    )
                }
            } else {
                // Vista Semanal
                SemanalScreen(
                    viewModel = viewModel,
                    onMonthChanged = { visibleMonth = it },
                    jumpToMonth = monthToJump,
                    onJumpFinished = { monthToJump = null }
                )
            }
        }

        if (expandedMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.75f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        expandedMenu = false
                    }
            )
        }
    }
}

@Composable
fun FabMenuItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onClick() }
            .padding(end = 0.dp)
    ) {
        // El "globo" de texto
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFF3E5E2),
            modifier = Modifier.padding(end = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 23.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(25.dp),
                    tint = Color(0xFFAC5343)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = label,
                    color = Color(0xFFAC5343),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}