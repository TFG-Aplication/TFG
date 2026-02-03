package com.asistente.planificador.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.asistente.core.domain.models.Calendar


@Composable
fun CalendarField(
    selectedCalendar: Calendar?,
    onClick: () -> Unit

){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(47.dp) // Altura similar a un TextField
            .padding(horizontal = 10.dp) // Alinea el inicio con el TextField de arriba
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = Primario,
            modifier = Modifier.size(30.dp)
        )

        Spacer(modifier = Modifier.width(16.dp)) // Espacio estándar entre icono y texto

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Calendario",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                lineHeight = 10.sp
            )
            Text(
                text = selectedCalendar?.name ?: "Seleccionar calendario",
                fontSize = 16.sp,
                color = Terciario
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Terciario,
            modifier = Modifier.size(28.dp)
        )
    }

}


@Composable
fun CalendarSelector (
    calendars: List<Calendar>,
    selectedCalendar: Calendar?,
    onCalendarChanged: (Calendar) -> Unit,
    onDismiss: () -> Unit,


) {
    AlertDialog(
        onDismissRequest = { onDismiss },
        properties = DialogProperties(usePlatformDefaultWidth = false), // Para que pueda ser casi pantalla completa
        modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.7f), // Tamaño superpuesto
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                "Mis Calendarios",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column {
                HorizontalDivider(thickness = 0.5.dp, color = Terciario.copy(alpha = 0.3f))
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    if (calendars.isEmpty()) {
                        item {
                            Text("No hay calendarios disponibles",
                                modifier = Modifier.padding(16.dp), color = Terciario)
                        }
                    } else {
                        items(calendars) { cal ->
                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onCalendarChanged(cal)
                                        onDismiss()
                                    },
                                headlineContent = { Text(cal.name, fontWeight = FontWeight.Medium) },
                                leadingContent = {
                                    Icon(Icons.Default.Circle, null,
                                        tint = Primario, modifier = Modifier.size(12.dp))
                                },
                                trailingContent = {
                                    if (selectedCalendar?.id == cal.id) {
                                        Icon(Icons.Default.Check, null, tint = Primario)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onDismiss }) {
                Text("CERRAR", color = Primario, fontWeight = FontWeight.Bold)
            }
        }
    )
}

