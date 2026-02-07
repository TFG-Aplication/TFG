package com.asistente.planificador.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.hilt.navigation.compose.hiltViewModel
import com.asistente.core.domain.models.Task
import com.asistente.core.ui.viewmodels.CalendarViewModel
import com.asistente.planificador.ui.screens.tools.darkenColor
import formatTime
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale


@Composable
fun AgendaView(    viewModel: CalendarViewModel = hiltViewModel(),onNavigateToDetail: (String) -> Unit
) {
    // 1. Obtener y formatear la fecha actual (independiente de la tab)
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
    val fechaFormateada = today.format(formatter).replaceFirstChar { it.uppercase() }
    // las tareas del calendario seleccionado
    val tasks by viewModel.taskList.collectAsState()
    val tasksByDate = taskByDate(tasks)
    val taskToday = tasksByDate[today]
    var sortedTask = taskToday?.sortedBy{ task ->
        var initDate = task.first.init_date?.time ?: 0L
        initDate

    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    ) {
        // Título de la Fecha

        Text(
            text = fechaFormateada,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 8.dp),
            color = Color.Black // Tu marrón característico
        )

        Spacer(modifier = Modifier.height(3.dp))


        HorizontalDivider(
            modifier = Modifier.padding(vertical = 15.dp),
            thickness = 1.dp,
            color = Color.LightGray.copy(alpha = 0.4f)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // Toma todo el espacio restante
            contentPadding = PaddingValues(bottom = 160.dp) // Espacio para el footer
        ) {
            if (sortedTask == null || sortedTask.isEmpty()) {
                item {
                    Text(
                        text = "No tienes tareas para hoy.",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            } else {
                // Iteramos sobre las tareas de hoy
                items(sortedTask) { taskPair ->
                    TaskCard(
                        task = taskPair.first,
                        getTaskColor = { categoryId -> viewModel.getCategoryColor(categoryId) },
                        getTaskCategory = { categoryId -> viewModel.getTaskCategory(categoryId) },
                        onTaskClick = { taskId -> onNavigateToDetail(taskId)
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp)) // Espacio entre tarjetas
                }
            }
        }
    }
}

@Composable
fun TaskCard(task: Task, getTaskColor: suspend (String?) -> Color, getTaskCategory: suspend (String?) -> String?, onTaskClick: (String) -> Unit) {
    // Definimos el color de la categoría (puedes sacarlo de task.category si lo tienes)
    val color by produceState(initialValue = colorCuarto, task.categoryId) {
        value = getTaskColor(task.categoryId)
    }
    val nombreCategoria by produceState<String?>(initialValue = null, task.categoryId) {
        value = getTaskCategory(task.categoryId)
    }


    val init = task.init_date?.let { formatTime(it) } ?: "00:00"
    val finish = task.finish_date?.let { formatTime(it) } ?: "00:00"

    val colorText = if(task.finish_date?.after(java.util.Date())?:true) Color.Black else darkenColor(color)

    val durationTask = Duration.between(task.init_date?.toInstant(), task.finish_date?.toInstant()).toDays() + 1

    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onTaskClick(task.id) },
        shape = RoundedCornerShape(12.dp),

        color = Color(0xFFF8F8F8), // Un gris muy sutil
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            // Indicador de color lateral
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 40.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp) // Espacio entre icono y texto
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = darkenColor(color),
                        modifier = Modifier.size(13.dp).offset(y = (-1).dp)

                    )
                    Text(
                        text = (nombreCategoria ?: "Tarea").uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = darkenColor(color),
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 0.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = task.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorText,
                        lineHeight = 12.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    if(durationTask <=1) {
                        Text(
                            text = "$init - $finish",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorText,
                            modifier = Modifier.padding(start = 9.dp)
                        )
                    }
                    else{
                        val numToday = ChronoUnit.DAYS.between(task.init_date?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate(), LocalDate.now()) + 1
                        Text(
                            text = "$init (día $numToday de $durationTask)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorText,
                            modifier = Modifier.padding(start = 9.dp)
                        )
                    }
                }
            }
        }
    }
}