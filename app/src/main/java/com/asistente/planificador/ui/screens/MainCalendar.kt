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
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.composable
import com.asistente.core.domain.models.Calendar
import com.asistente.core.ui.viewmodels.CalendarViewModel
import com.asistente.planificador.ui.components.CalendarView
import com.asistente.planificador.ui.components.FootPage
import com.asistente.planificador.ui.components.HeaderPage
import com.asistente.planificador.ui.viewmodels.ShowCategoriesViewModel
import java.time.YearMonth

val colorCuarto = Color(0xFFF3E5E2)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainCalendar(viewModel: CalendarViewModel, categoriesViewModel: ShowCategoriesViewModel,
                 onNavigateToTask: () -> Unit,
                 onNavigateToCategory: () -> Unit,
                 onNavigateToDetail: (String) -> Unit){
    var currentView by remember { mutableStateOf(CalendarView.MONTH) }
    var visibleMonth by remember { mutableStateOf(YearMonth.now()) }
    var currentTab by remember { mutableStateOf("calendar") }

    var monthToJump by remember { mutableStateOf<YearMonth?>(null) }
    var calenadarToJump by remember { mutableStateOf<Calendar?>(null) }

    var expandedMenu by remember { mutableStateOf(false) }
    var showCategoryShow by remember { mutableStateOf(false) }

    //para el footer
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded // Empieza abajo
        )
    )
    Box(modifier = Modifier.fillMaxSize()) {

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 130.dp,
            sheetContainerColor = Secundario,
            sheetShape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp),
            sheetDragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .width(45.dp)
                        .height(4.dp)
                        .background(Color(0xFFAC5343), RoundedCornerShape(2.dp))
                )
            },

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
                    },
                    onCalendarChanged = { selectedCalendar ->
                        calenadarToJump = selectedCalendar
                    }
                )
            }
        }, sheetContent = {
                // CONTENIDO QUE APARECE AL DESLIZAR
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.6f) // Hasta donde sube
                        .padding(top = 10.dp) // IMPORTANTE: Dejamos espacio para que no choque con los iconos fijos
                        .padding(16.dp)
                ) {
                    AgendaView(onNavigateToDetail = onNavigateToDetail)
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

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            FootPage(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }

        // FLOATING ACTION BUTTON
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 145.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (expandedMenu) {
                    FabMenuItem("Personalizar Categorías", Icons.Default.Bookmarks) { showCategoryShow = true }
                    FabMenuItem("Recordatorio", Icons.Default.Lightbulb) { }
                    FabMenuItem("Cumpleaños", Icons.Default.Cake) { }
                    FabMenuItem("Actividad", Icons.Default.Assignment) { }
                    FabMenuItem("Tarea", Icons.Default.CheckCircle) { onNavigateToTask() }
                }

                FloatingActionButton(
                    onClick = { expandedMenu = !expandedMenu },
                    containerColor = Color(0xFFAC5343),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(if (expandedMenu) Icons.Default.Close else Icons.Default.Add, "Menu")
                }
            }
        }
        if (showCategoryShow) {
            CategoryShow(
                isVisible = showCategoryShow,
                viewModel = categoriesViewModel,
                onBack = { showCategoryShow = false },
                onNavigateToCategory = { onNavigateToCategory() }
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
            color = colorCuarto,
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