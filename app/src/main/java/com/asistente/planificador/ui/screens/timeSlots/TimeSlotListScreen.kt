package com.asistente.planificador.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asistente.core.domain.models.RecurrenceType
import com.asistente.core.domain.models.SlotType
import com.asistente.core.domain.models.TimeSlot
import com.asistente.planificador.ui.screens.timeSlots.SlotsCarousel
import com.asistente.planificador.ui.screens.timeSlots.TimeSlotDetailSheet
import com.asistente.planificador.ui.screens.timeSlots.WeekHeatmap
import com.asistente.planificador.ui.screens.tools.*
import com.asistente.planificador.ui.viewmodels.TimeSlotEvent
import com.asistente.planificador.ui.viewmodels.TimeSlotViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSlotListScreen(
    calendarName: String,
    viewModel: TimeSlotViewModel = hiltViewModel(),
    onNavigateToForm: (TimeSlot?) -> Unit,
    onNavigateToEditTask: (taskId: String) -> Unit,
    onNavigateToViewTask: (taskId: String) -> Unit,
    onBack: () -> Unit
) {
    val slots               by viewModel.timeSlotList.collectAsStateWithLifecycle()
    val detailState         by viewModel.detailState.collectAsStateWithLifecycle()
    val categoryByTaskId    by viewModel.categoryByTaskId.collectAsStateWithLifecycle()

    var searchQuery             by remember { mutableStateOf("") }
    var activeTypeFilters       by remember { mutableStateOf(emptySet<SlotType>()) }
    var activeRecurrenceFilters by remember { mutableStateOf(emptySet<RecurrenceType>()) }
    var activeStatusFilter      by remember { mutableStateOf<Boolean?>(null) }
    var pendingWarnings         by remember { mutableStateOf<List<String>?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is TimeSlotEvent.SaveSuccess      -> Unit
                is TimeSlotEvent.SaveWithWarnings -> pendingWarnings = event.warnings
                is TimeSlotEvent.Error            -> scope.launch {
                    snackbarHostState.showSnackbar(event.message, duration = SnackbarDuration.Short)
                }
            }
        }
    }

    val filteredSlots = remember(slots, searchQuery, activeTypeFilters, activeRecurrenceFilters, activeStatusFilter) {
        slots.filter { slot ->
            (searchQuery.isBlank() || slot.name.contains(searchQuery, ignoreCase = true)) &&
                    (activeTypeFilters.isEmpty()       || slot.slotType       in activeTypeFilters) &&
                    (activeRecurrenceFilters.isEmpty() || slot.recurrenceType in activeRecurrenceFilters) &&
                    (activeStatusFilter == null        || slot.enable         == activeStatusFilter)
        }
    }

    Scaffold(
        containerColor = ColorGrisFondo,
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        topBar         = { ListTopBar(calendarName = calendarName, onBack = onBack, onAdd = { onNavigateToForm(null) }) }
    ) { pad ->
        LazyColumn(
            modifier       = Modifier.padding(pad).fillMaxSize(),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            item {
                SearchAndFilterBar(
                    query                    = searchQuery,
                    onQueryChange            = { searchQuery = it },
                    activeTypeFilters        = activeTypeFilters,
                    onTypeFilterToggle       = { type ->
                        activeTypeFilters = if (type in activeTypeFilters) activeTypeFilters - type else activeTypeFilters + type
                    },
                    activeRecurrenceFilters  = activeRecurrenceFilters,
                    onRecurrenceFilterToggle = { type ->
                        activeRecurrenceFilters = if (type in activeRecurrenceFilters) activeRecurrenceFilters - type else activeRecurrenceFilters + type
                    },
                    activeStatusFilter       = activeStatusFilter,
                    onStatusFilterChange     = { activeStatusFilter = if (activeStatusFilter == it) null else it },
                    onClearAll               = {
                        activeTypeFilters       = emptySet()
                        activeRecurrenceFilters = emptySet()
                        activeStatusFilter      = null
                    },
                    modifier = Modifier.padding(horizontal = 16.dp).padding(top = 14.dp, bottom = 6.dp)
                )
            }
            item {
                PlanningToggleBanner(
                    slots        = slots,
                    onDisableAll = { viewModel.disableAllActiveSlots() },
                    onEnableAll  = { viewModel.enableAllInactiveSlots() },
                    modifier     = Modifier.padding(horizontal = 16.dp).padding(top = 4.dp, bottom = 8.dp)
                )
            }
            item {
                SlotsCarousel(
                    filteredSlots    = filteredSlots,
                    allSlots         = slots,
                    onEdit           = { onNavigateToForm(it) },
                    onEditTask       = { onNavigateToEditTask(it) },
                    onDelete         = { viewModel.deleteTimeSlot(it.id) },
                    onToggleActive   = { viewModel.toggleTimeSlotActive(it.id) },
                    onCardClick      = { viewModel.openDetail(it) },
                    onAdd            = { onNavigateToForm(null) },
                    categoryByTaskId = categoryByTaskId,
                    modifier         = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }
            item {
                WeekHeatmap(
                    slots       = slots,
                    onSlotClick = { viewModel.openDetail(it) },
                    modifier    = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }

    detailState?.let { detail ->
        TimeSlotDetailSheet(
            state                  = detail,
            onDismiss              = { viewModel.closeDetail() },
            onEdit                 = { onNavigateToForm(it) },
            onEditTask             = { taskId -> onNavigateToEditTask(taskId); viewModel.closeDetail() },
            onViewTask             = { onNavigateToViewTask(it) },
            onDelete               = { viewModel.deleteTimeSlot(it.id) },
            onToggleActive         = { viewModel.toggleTimeSlotActive(it.id) },
            onOverlappingSlotClick = { viewModel.openDetail(it) }
        )
    }

    pendingWarnings?.let { warnings ->
        WarningDialog(warnings = warnings, onDismiss = { pendingWarnings = null })
    }
}

// ── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun ListTopBar(
    calendarName: String,
    onBack: () -> Unit,
    onAdd: () -> Unit
) {
    Surface(color = Color.White, shadowElevation = 2.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 4.dp, end = 16.dp, top = 10.dp, bottom = 14.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.Close, null, tint = Terciario, modifier = Modifier.size(24.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Franjas horarias", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.Black)
                    Text(calendarName, fontSize = 13.sp, color = Terciario)
                }
                Button(
                    onClick        = onAdd,
                    colors         = ButtonDefaults.buttonColors(containerColor = Primario),
                    shape          = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    modifier       = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Añadir", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}