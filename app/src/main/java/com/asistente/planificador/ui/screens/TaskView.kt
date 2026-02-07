package com.asistente.planificador.ui.screens


import android.graphics.Color.parseColor
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
    onBack: () -> Unit
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(horizontal = 24.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- TÍTULO (ESTÁTICO) ---
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Create,
                    contentDescription = null,
                    tint = Primario,
                    modifier = Modifier.size(30.dp).padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = uiState.name.ifBlank { task?.name?: "Sin nombre" },
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (uiState.name.isBlank()) Terciario else Color.Black
                )
            }

            HorizontalDivider(thickness = 0.5.dp)

            // --- CALENDARIO (ESTÁTICO) ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Primario,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = uiState.calendar?.name ?: "Calendario no asignado",
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }

            // --- FECHAS Y HORAS ---
            Column(modifier = Modifier.fillMaxWidth()) {
                // Inicio
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RadioButtonUnchecked, null, tint = Primario, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(24.dp))
                    Text(formatDate(task?.init_date?:uiState.initDate), modifier = Modifier.weight(1f), fontSize = 17.sp)
                    Text(formatTime(task?.init_date?:uiState.initDate), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }

                // Línea de conexión visual
                Box(modifier = Modifier.padding(start = 6.dp).width(1.dp).height(20.dp).background(Primario.copy(alpha = 0.3f)))

                // Fin
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = Primario, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(22.dp))
                    Text(formatDate(task?.finish_date?:uiState.finishDate), modifier = Modifier.weight(1f), fontSize = 17.sp)
                    Text(formatTime(task?.finish_date?:uiState.finishDate), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(thickness = 0.5.dp)

            // CATEGORÍA
            Row(verticalAlignment = Alignment.CenterVertically) {
                val color by produceState(Color.LightGray, task?.categoryId) {
                    value = viewModel.getCategoryColor(task?.categoryId)
                }
                Icon(
                    imageVector = Icons.Default.Bookmarks,
                    contentDescription = null,
                    tint = Primario,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = color,
                    modifier = Modifier.height(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                        Text(
                            text = (category?.name ?: "NINGUNA").uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = darkenColor(color )
                        )
                    }
                }
            }
        }
    }
}