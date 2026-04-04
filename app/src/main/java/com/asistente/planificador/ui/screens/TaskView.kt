package com.asistente.planificador.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.asistente.core.domain.models.Category
import com.asistente.core.domain.models.Task
import com.asistente.planificador.ui.screens.tools.IconAlarma
import com.asistente.planificador.ui.screens.tools.IconFecha
import com.asistente.planificador.ui.screens.tools.IconFranja
import com.asistente.planificador.ui.screens.tools.IconNotas
import com.asistente.planificador.ui.screens.tools.IconRepeticion
import com.asistente.planificador.ui.screens.tools.IosRow
import com.asistente.planificador.ui.screens.tools.Primario
import com.asistente.planificador.ui.screens.tools.Secundario
import com.asistente.planificador.ui.screens.tools.Terciario
import com.asistente.planificador.ui.screens.tools.darkenColor
import com.asistente.planificador.ui.viewmodels.TaskViewModel
import formatDate
import formatTime

private val IosDestructive = Color(0xFFFF3B30)



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskView(
    viewModel: TaskViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToEditTask: (String) -> Unit,
    onDelete: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

// ── Añadir esto ───────────────────────────────────────────────────────────
    var refreshTrigger by remember { mutableStateOf(0) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                refreshTrigger++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
// ─────────────────────────────────────────────────────────────────────────

// Añade refreshTrigger como key aquí
    val task by produceState<Task?>(initialValue = null, uiState, refreshTrigger) {
        value = viewModel.getExpecificTask(uiState.id)
    }
    val category by produceState<Category?>(initialValue = null, task?.categoryId) {
        value = viewModel.getTaskCategory(task?.categoryId)
    }
    val categoryColor by produceState(Color(0xFFF3E5E2), task?.categoryId) {
        value = viewModel.getCategoryColor(task?.categoryId)
    }

    Scaffold(
        containerColor = Secundario,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
            ) {
                // ── Barra navegación ──────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onBack,
                        contentPadding = PaddingValues(start = 8.dp, end = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Volver",
                            tint = Primario,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(text = "Calendario", color = Primario, fontSize = 17.sp)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = { onNavigateToEditTask(uiState.id) },
                        contentPadding = PaddingValues(end = 12.dp)
                    ) {
                        Text(text = "Editar", color = Color(0xFF38B6FF) , fontSize = 17.sp)
                    }
                }

                // ── Cabecera unida ────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = categoryColor,
                        modifier = Modifier.height(26.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = darkenColor(categoryColor),
                                modifier = Modifier.size(13.dp).offset(y = (-1).dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = (category?.name ?: "Sin categoría").uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = darkenColor(categoryColor)
                            )
                        }
                    }
                    Text(
                        text = task?.name ?: "Sin nombre",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Terciario,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = uiState.calendar?.name ?: "Calendario no asignado",
                            fontSize = 14.sp,
                            color = Terciario
                        )
                    }
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = { viewModel.deleteTask(onDelete) },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(
                        text = "Eliminar tarea",
                        color = IosDestructive,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // ── Fechas ────────────────────────────────────────────────────────
            IosGroupCard {
                IosRow(
                    icon = Icons.Default.CalendarMonth,
                    iconTint = IconFecha,
                    label = "Inicio"
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatDate(task?.init_date ?: uiState.initDate),
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        if (!uiState.isAllDay) {
                            Text(
                                text = formatTime(task?.init_date ?: uiState.initDate),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                    }
                }
                IosDivider()
                IosRow(
                    icon = Icons.Default.Schedule,
                    iconTint = IconFecha,
                    label = "Fin"
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatDate(task?.finish_date ?: uiState.finishDate),
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        if (!uiState.isAllDay) {
                            Text(
                                text = formatTime(task?.finish_date ?: uiState.finishDate),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                    }
                }
                if (uiState.isAllDay) {
                    IosDivider()
                    IosRow(
                        icon = Icons.Default.WbSunny,
                        iconTint = IconAlarma,
                        label = "Todo el día"
                    ) {
                        Text(text = "Sí", fontSize = 16.sp, color = Terciario)
                    }
                }
            }

            // ── Alarma · Repetición · Franja ──────────────────────────────────
            IosGroupCard {
                val alertOffsets = task?.alerts
                    ?.map { timestamp -> ((task!!.init_date?.time ?: 0L) - timestamp) / 60_000L }
                    ?.filter { it > 0 }
                    ?: emptyList()

                if (alertOffsets.isEmpty()) {
                    IosRow(
                        icon = Icons.Default.NotificationsNone,
                        iconTint = IconAlarma,
                        label = "Alarma"
                    ) {
                        Text(text = "Sin alarma", fontSize = 16.sp, color = Terciario)
                    }
                } else {
                    // Fila cabecera sin trailing
                    IosRow(
                        icon = Icons.Default.NotificationsNone,
                        iconTint = IconAlarma,
                        label = "Alarma",
                        trailingContent = null
                    )
                    // Una fila por alerta, todas alineadas debajo
                    alertOffsets.forEach { offsetMinutes ->
                        val label = when {
                            offsetMinutes < 60    -> "${offsetMinutes}m antes"
                            offsetMinutes < 1440  -> "${offsetMinutes / 60}h antes"
                            offsetMinutes < 10080 -> "${offsetMinutes / 1440}d antes"
                            else                  -> "${offsetMinutes / 10080}sem antes"
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.width(42.dp))
                            Text(
                                text = label,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                        }
                    }
                }

                IosDivider()
                IosRow(
                    icon = Icons.Default.Repeat,
                    iconTint = IconRepeticion,
                    label = "Repetición"
                ) {
                    Text(text = "Nunca", fontSize = 16.sp, color = Terciario)
                }
                IosDivider()
                IosRow(
                    icon = Icons.Default.Block,
                    iconTint = IconFranja,
                    label = "Franja bloqueada"
                ) {
                    Switch(
                        checked = task?.blockTimeSlot ?: false,
                        onCheckedChange = null,
                        enabled = false,
                        colors = SwitchDefaults.colors(
                            disabledCheckedThumbColor = Color.White,
                            disabledCheckedTrackColor = IconFranja,
                            disabledUncheckedThumbColor = Color.White,
                            disabledUncheckedTrackColor = Secundario,
                            disabledUncheckedBorderColor = Color.Transparent,
                            disabledCheckedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            // ── Notas (solo si hay) ───────────────────────────────────────────
            val notes = task?.notes ?: uiState.notes
            if (!notes.isNullOrBlank()) {
                IosGroupCard {
                    IosRow(
                        icon = Icons.Default.Description,
                        iconTint = IconNotas,
                        label = "Notas",
                        trailingContent = null
                    )
                    IosDivider()
                    Text(
                        text = notes,
                        fontSize = 16.sp,
                        color = Color.Black,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(
                            start = 58.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 14.dp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── Componentes reutilizables ─────────────────────────────────────────────────

@Composable
private fun IosGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White),
        content = content
    )
}

@Composable
private fun IosDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 58.dp),
        thickness = 0.5.dp,
        color = Color(0xFFE5E5EA)
    )
}

