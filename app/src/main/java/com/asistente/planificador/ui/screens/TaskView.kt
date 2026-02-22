package com.asistente.planificador.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.asistente.core.domain.models.Category
import com.asistente.core.domain.models.Task
import com.asistente.planificador.ui.screens.tools.darkenColor
import com.asistente.planificador.ui.viewmodels.TaskViewModel
import formatDate
import formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskView(
    viewModel: TaskViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val task by produceState<Task?>(initialValue = null, uiState) {
        value = viewModel.getExpecificTask(uiState.id)
    }
    val category by produceState<Category?>(initialValue = null, task?.categoryId) {
        value = viewModel.getTaskCategory(task?.categoryId)
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(vertical = 10.dp).height(72.dp),
                title = {
                    Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.CenterStart) {
                        Text(
                            text = "Detalles de Tarea",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 20.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxHeight().padding(start = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Cerrar",
                            modifier = Modifier.size(30.dp),
                            tint = Terciario
                        )
                    }
                },
                actions = {
                    // Botón editar
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = Primario,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    // Botón borrar
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Borrar",
                            tint = Primario,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(horizontal = 24.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- TÍTULO con categoría a la izquierda ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Categoría como chip a la izquierda del título
                val color by produceState(Color.LightGray, task?.categoryId) {
                    value = viewModel.getCategoryColor(task?.categoryId)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = color,
                    modifier = Modifier.height(28.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Text(
                            text = (category?.name ?: "NINGUNA").uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = darkenColor(color)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = uiState.name.ifBlank { task?.name ?: "Sin nombre" },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // --- CALENDARIO ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Primario,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = uiState.calendar?.name ?: "Calendario no asignado",
                    fontSize = 17.sp,
                    color = Color.Black
                )
            }

            // --- FECHAS Y HORAS ---
            Row(modifier = Modifier.fillMaxWidth()) {
                // Columna izquierda: círculo + línea + flecha
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(26.dp)
                ) {
                    Icon(
                        Icons.Default.RadioButtonUnchecked,
                        null,
                        tint = Primario,
                        modifier = Modifier.size(12.dp)
                    )
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .width(1.5.dp)
                                .height(5.dp)
                                .background(Primario.copy(alpha = 0.4f))
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                    Icon(
                        Icons.Default.ArrowDownward,
                        null,
                        tint = Primario,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Columna derecha: textos
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDate(task?.init_date ?: uiState.initDate),
                            modifier = Modifier.weight(1f),
                            fontSize = 17.sp
                        )
                        if (!uiState.isAllDay) {
                            Text(
                                text = formatTime(task?.init_date ?: uiState.initDate),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDate(task?.finish_date ?: uiState.finishDate),
                            modifier = Modifier.weight(1f),
                            fontSize = 17.sp
                        )
                        if (!uiState.isAllDay) {
                            Text(
                                text = formatTime(task?.finish_date ?: uiState.finishDate),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // --- ALARMA (estática por ahora) ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = null,
                    tint = Primario,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Sin alarma", fontSize = 17.sp, color = Terciario)
            }

            // --- REPETICIÓN (estática por ahora) ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = null,
                    tint = Primario,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "No se repite", fontSize = 17.sp, color = Terciario)
            }

            // --- NOTAS (solo si hay) ---
            val notes = task?.notes ?: uiState.notes
            if (!notes.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = Primario,
                        modifier = Modifier.size(26.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = notes,
                        fontSize = 17.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}