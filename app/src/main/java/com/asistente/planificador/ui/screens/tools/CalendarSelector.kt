import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.asistente.planificador.ui.screens.tools.IconFecha
import com.asistente.planificador.ui.screens.tools.IosRow
import com.asistente.planificador.ui.screens.tools.Primario
import com.asistente.planificador.ui.screens.tools.Terciario
import com.asistente.planificador.ui.screens.tools.TintedIconBox

// ── CalendarField ─────────────────────────────────────────────────────────────

@Composable
fun CalendarField(selectedCalendar: Calendar?, onClick: () -> Unit) {
    IosRow(
        icon     = Icons.Default.CalendarMonth,
        iconTint = IconFecha,
        label    = "Calendario"
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.clickable { onClick() }
        ) {
            Text(
                text     = selectedCalendar?.name ?: "Seleccionar",
                fontSize = 15.sp,
                color    = if (selectedCalendar != null) Primario else Terciario
            )
            Icon(Icons.Default.ChevronRight, null, tint = Terciario, modifier = Modifier.size(18.dp))
        }
    }
}

// ── CalendarSelector ──────────────────────────────────────────────────────────

@Composable
fun CalendarSelector(
    calendars        : List<Calendar>,
    selectedCalendar : Calendar?,
    onCalendarChanged: (Calendar) -> Unit,
    onDismiss        : () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false),
        modifier         = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.7f),
        containerColor   = Color.White,
        shape            = RoundedCornerShape(20.dp),
        title = {
            Text("Mis Calendarios", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        },
        text = {
            Column {
                HorizontalDivider(thickness = 0.5.dp, color = Terciario.copy(alpha = 0.3f))
                LazyColumn(
                    modifier        = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding  = PaddingValues(vertical = 8.dp)
                ) {
                    if (calendars.isEmpty()) {
                        item { Text("No hay calendarios disponibles", modifier = Modifier.padding(16.dp), color = Terciario) }
                    } else {
                        items(calendars) { cal ->
                            Row(
                                modifier          = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCalendarChanged(cal); onDismiss() }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TintedIconBox(icon = Icons.Default.CalendarMonth, tint = IconFecha, boxSize = 32.dp, iconSize = 16.dp)
                                Spacer(Modifier.width(12.dp))
                                Text(cal.name, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                if (selectedCalendar?.id == cal.id) {
                                    Icon(Icons.Default.Check, null, tint = Primario, modifier = Modifier.size(18.dp))
                                }
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = Terciario.copy(alpha = 0.15f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CERRAR", color = Primario, fontWeight = FontWeight.Bold)
            }
        }
    )
}