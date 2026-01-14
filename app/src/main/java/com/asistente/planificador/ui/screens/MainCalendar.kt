package com.asistente.planificador.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
            FloatingActionButton(
                onClick = { /* Abrir formulario */ },
                containerColor = Color(0xFFAC5343),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir")
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
    }
}