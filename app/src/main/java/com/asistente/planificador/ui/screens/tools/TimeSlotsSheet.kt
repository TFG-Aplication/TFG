package com.asistente.planificador.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asistente.core.domain.models.RecurrenceType
import com.asistente.core.domain.models.SlotType
import com.asistente.core.domain.models.Task
import com.asistente.core.domain.models.TimeSlot
import com.asistente.planificador.ui.screens.label
import com.asistente.planificador.ui.screens.shortLabel
import com.asistente.planificador.ui.viewmodels.TimeSlotDetailState
import com.asistente.planificador.ui.viewmodels.toTimeString
import java.text.SimpleDateFormat
import java.util.Locale

private val IosDestructive = Color(0xFFFF3B30)



// ─────────────────────────────────────────────────────────────────────────────
// ENTRY POINT
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSlotDetailSheet(
    state: TimeSlotDetailState,
    onDismiss: () -> Unit,
    onEdit: (TimeSlot) -> Unit,
    onEditTask: (taskId: String) -> Unit,
    onViewTask: (taskId: String) -> Unit,
    onDelete: (TimeSlot) -> Unit,
    onToggleActive: (TimeSlot) -> Unit,
    onOverlappingSlotClick: (TimeSlot) -> Unit
) {
    val sheetState     = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val screenHeight   = LocalConfiguration.current.screenHeightDp.dp
    val maxSheetHeight = screenHeight * 0.75f

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = Color.White,
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle       = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxSheetHeight)
        ) {
            SheetHeader(
                slot       = state.slot,
                onEdit     = { onEdit(state.slot); onDismiss() },
                onEditTask = { state.slot.taskId?.let { onEditTask(it) }; onDismiss() }
            )

            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 8.dp)
            ) {
                DetailInfoSection(slot = state.slot)

                if (state.slot.slotType == SlotType.BLOCKED) {
                    Spacer(Modifier.height(14.dp))
                    ActiveToggleRow(
                        slot           = state.slot,
                        onToggleActive = { onToggleActive(state.slot) }
                    )
                }

                if (state.slot.slotType == SlotType.TASK_BLOCKED && state.associatedTask != null) {
                    Spacer(Modifier.height(20.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
                    Spacer(Modifier.height(20.dp))
                    AssociatedTaskSection(
                        task       = state.associatedTask,
                        onViewTask = { onViewTask(state.associatedTask.id) }
                    )
                }

                if (state.overlappingSlots.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
                    Spacer(Modifier.height(20.dp))
                    OverlappingSlotsSection(
                        overlapping = state.overlappingSlots,
                        onSlotClick = { onDismiss(); onOverlappingSlotClick(it) }
                    )
                }

                if (state.slot.slotType == SlotType.TASK_BLOCKED) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape    = RoundedCornerShape(10.dp),
                        color    = Color(0xFFFFF8E1),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier              = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Info, null,
                                tint = Color(0xFFF57F17), modifier = Modifier.size(14.dp))
                            Text(
                                "Esta franja se gestiona desde su tarea asociada.",
                                fontSize = 14.sp, color = Color(0xFF5D4037)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick  = { onDelete(state.slot); onDismiss() },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(
                        text = when (state.slot.slotType) {
                            SlotType.BLOCKED      -> "Eliminar franja"
                            SlotType.TASK_BLOCKED -> "Eliminar bloqueo de tarea"
                        },
                        color      = IosDestructive,
                        fontSize   = 17.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CABECERA FIJA
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SheetHeader(slot: TimeSlot, onEdit: () -> Unit, onEditTask: () -> Unit) {
    val dotColor   = slot.slotType.dotColor()
    val badgeColor = slot.slotType.badgeColors()

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier         = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFDDDDDD))
            )
        }

        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(dotColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (slot.slotType) {
                        SlotType.BLOCKED      -> Icons.Default.Block
                        SlotType.TASK_BLOCKED -> Icons.Default.CheckCircle
                    },
                    contentDescription = null,
                    tint     = darkenColor(dotColor),
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    slot.slotType.label(),
                    fontSize   = 14.sp, fontWeight = FontWeight.Bold,
                    color      = badgeColor.second
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    slot.name,
                    fontSize   = 22.sp, fontWeight = FontWeight.Bold,
                    color      = Color.Black, maxLines = 2,
                    overflow   = TextOverflow.Ellipsis
                )
            }

            TextButton(
                onClick        = if (slot.slotType == SlotType.BLOCKED) onEdit else onEditTask,
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Text(
                    text = "Editar",
                    color = Color(0xFF38B6FF),
                    fontSize = 17.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// INFO
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DetailInfoSection(slot: TimeSlot) {
    val fmtFull  = SimpleDateFormat("d 'de' MMMM, yyyy", Locale("es", "ES"))
    val fmtShort = SimpleDateFormat("d MMM yyyy",         Locale("es", "ES"))

    SectionTitle("Información")
    Spacer(Modifier.height(12.dp))

    DetailInfoRow(Icons.Default.AccessTime, IconFecha, "Horario",
        "${slot.startMinuteOfDay.toTimeString()} – ${slot.endMinuteOfDay.toTimeString()}")
    Spacer(Modifier.height(10.dp))
    DetailInfoRow(Icons.Default.Repeat, IconRepeticion, "Recurrencia",
        slot.recurrenceType.shortLabel())

    when (slot.recurrenceType) {
        RecurrenceType.SINGLE_DAY -> {
            Spacer(Modifier.height(10.dp))
            DetailInfoRow(Icons.Default.Event, IconFecha, "Fecha",
                slot.rangeStart?.let { fmtFull.format(it) } ?: "—")
        }
        RecurrenceType.DATE_RANGE -> {
            Spacer(Modifier.height(10.dp))
            DetailInfoRow(Icons.Default.DateRange, IconFecha, "Período",
                "${slot.rangeStart?.let { fmtShort.format(it) } ?: "?"} → " +
                        (slot.rangeEnd?.let { fmtShort.format(it) } ?: "?"))
        }
        else -> Unit
    }

    // Días: siempre visible para ambos tipos.
    // Para SINGLE_DAY se deduce el día desde rangeStart; para el resto se usa daysOfWeek.
    val daysToShow: List<Int> = when (slot.recurrenceType) {
        RecurrenceType.SINGLE_DAY -> {
            slot.rangeStart?.let {
                val cal = java.util.Calendar.getInstance().apply { time = it }
                val cd  = cal.get(java.util.Calendar.DAY_OF_WEEK)
                listOf(if (cd == java.util.Calendar.SUNDAY) 7 else cd - 1)
            } ?: slot.daysOfWeek
        }
        else -> slot.daysOfWeek
    }
    if (daysToShow.isNotEmpty()) {
        Spacer(Modifier.height(14.dp))
        DetailDaysRow(
            daysOfWeek = daysToShow,
            dotColor   = slot.slotType.dotColor(),
            isActive   = slot.isActive
        )
    }
}

@Composable
private fun DetailInfoRow(icon: ImageVector, tint: Color, label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp).clip(RoundedCornerShape(8.dp))
                .background(tint.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = darkenColor(tint), modifier = Modifier.size(17.dp))
        }
        Column {
            Text(label, fontSize = 14.sp, color = Terciario, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DetailDaysRow(daysOfWeek: List<Int>, dotColor: Color, isActive: Boolean = true) {
    val days      = listOf(1 to "L", 2 to "M", 3 to "X", 4 to "J", 5 to "V", 6 to "S", 7 to "D")
    val alpha     = if (isActive) 1f else 0.45f
    val bgInactive = Color(0xFFF2F2F2)

    Column {
        Text("Días activos", fontSize = 14.sp, color = Terciario, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            days.forEach { (num, lbl) ->
                val dayActive = daysOfWeek.contains(num)
                Box(
                    modifier = Modifier
                        .weight(1f).aspectRatio(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (dayActive) dotColor.copy(alpha = if (isActive) 0.5f else 0.07f)
                            else bgInactive
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        lbl,
                        fontSize   = 12.sp,
                        lineHeight = 12.sp,
                        fontWeight = if (dayActive) FontWeight.Bold else FontWeight.Normal,
                        color      = if (dayActive) darkenColor(dotColor.copy(alpha = alpha))
                        else Terciario.copy(alpha = alpha)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOGGLE ACTIVO
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ActiveToggleRow(slot: TimeSlot, onToggleActive: () -> Unit) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .clickable { onToggleActive() }
            .padding(vertical = 4.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp).clip(RoundedCornerShape(8.dp))
                .background(
                    if (slot.isActive) Color(0xFF43A047).copy(alpha = 0.12f)
                    else Terciario.copy(alpha = 0.10f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PowerSettingsNew, null,
                tint = if (slot.isActive) Color(0xFF43A047) else Terciario,
                modifier = Modifier.size(17.dp))
        }
        Text(
            if (slot.isActive) "Franja activa" else "Franja inactiva",
            fontSize   = 16.sp, fontWeight = FontWeight.SemiBold,
            color      = if (slot.isActive) Color(0xFF43A047) else Terciario,
            modifier   = Modifier.weight(1f)
        )
        Switch(
            checked         = slot.isActive,
            onCheckedChange = { onToggleActive() },
            colors          = SwitchDefaults.colors(
                checkedTrackColor    = Color(0xFF43A047),
                checkedThumbColor    = Color.White,
                uncheckedTrackColor  = Terciario.copy(alpha = 0.3f),
                uncheckedThumbColor  = Color.White,
                uncheckedBorderColor = Color.Transparent,
                checkedBorderColor   = Color.Transparent
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAREA ASOCIADA
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AssociatedTaskSection(task: Task, onViewTask: () -> Unit) {
    val fmtShort    = SimpleDateFormat("d MMM yyyy", Locale("es", "ES"))
    val accentColor = Color(0xFF38B6FF)

    SectionTitle("Tarea asociada")
    Spacer(Modifier.height(10.dp))

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF8F8F8))
            .clickable { onViewTask() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 36.dp)
                .background(accentColor, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(Icons.Default.CheckCircle, null,
                    tint = darkenColor(accentColor), modifier = Modifier.size(11.dp))
                Text(
                    "TAREA BLOQUEANTE",
                    fontSize = 9.sp, fontWeight = FontWeight.Bold,
                    color = darkenColor(accentColor), letterSpacing = 0.8.sp
                )
            }
            Spacer(Modifier.height(1.dp))
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    task.name,
                    fontSize   = 15.sp, fontWeight = FontWeight.Bold,
                    color      = Color.Black,
                    maxLines   = 1, overflow = TextOverflow.Ellipsis,
                    modifier   = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "${fmtShort.format(task.init_date)} – ${fmtShort.format(task.finish_date)}",
                    fontSize   = 12.sp, fontWeight = FontWeight.Medium,
                    color      = Terciario,
                    maxLines   = 1
                )
            }
        }

        Spacer(Modifier.width(6.dp))
        Icon(Icons.Default.ChevronRight, null,
            tint = Terciario, modifier = Modifier.size(18.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FRANJAS SOLAPADAS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OverlappingSlotsSection(
    overlapping: List<TimeSlot>,
    onSlotClick: (TimeSlot) -> Unit
) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Franjas solapadas", fontSize = 14.sp, fontWeight = FontWeight.Bold,
            color = Terciario, letterSpacing = 0.3.sp)
        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFFFF8E1)) {
            Row(
                modifier              = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(Icons.Default.Warning, null,
                    tint = Color(0xFFF57F17), modifier = Modifier.size(11.dp))
                Text("${overlapping.size}", fontSize = 10.sp,
                    fontWeight = FontWeight.Bold, color = Color(0xFFF57F17))
            }
        }
    }

    Spacer(Modifier.height(10.dp))

    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFFFF8E1),
        modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier              = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Warning, null,
                tint = Color(0xFFF57F17),
                modifier = Modifier.size(16.dp).padding(top = 1.dp))
            Text(
                "En zonas de solapamiento las franjas de tarea tienen prioridad visual. " +
                        "Las franjas manuales siguen activas pero pueden no verse en el calendario.",
                fontSize = 14.sp, color = Color(0xFF5D4037), lineHeight = 20.sp
            )
        }
    }

    Spacer(Modifier.height(10.dp))

    overlapping.forEach { slot ->
        OverlappingSlotRow(slot = slot, onClick = { onSlotClick(slot) })
        Spacer(Modifier.height(6.dp))
    }
}

// ── Fila de franja solapada ───────────────────────────────────────────────────

@Composable
private fun OverlappingSlotRow(slot: TimeSlot, onClick: () -> Unit) {
    val dotColor = slot.slotType.dotColor()

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF8F8F8))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Barra lateral de color
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 36.dp)
                .background(dotColor, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Etiqueta de tipo
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(Icons.Default.CheckCircle, null,
                    tint = darkenColor(dotColor), modifier = Modifier.size(11.dp))
                Text(
                    slot.slotType.label().uppercase(),
                    fontSize = 9.sp, fontWeight = FontWeight.Bold,
                    color = darkenColor(dotColor), letterSpacing = 0.8.sp
                )
            }
            Spacer(Modifier.height(1.dp))
            // Nombre · hora · recurrencia en la misma línea
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    slot.name,
                    fontSize   = 15.sp, fontWeight = FontWeight.Bold,
                    color      = Color.Black,
                    maxLines   = 1, overflow = TextOverflow.Ellipsis,
                    modifier   = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "${slot.startMinuteOfDay.toTimeString()} – ${slot.endMinuteOfDay.toTimeString()} · ${slot.recurrenceType.shortLabel()}",
                    fontSize   = 12.sp, fontWeight = FontWeight.Medium,
                    color      = Terciario, maxLines   = 1,
                    softWrap   = false
                )
            }
        }

        Spacer(Modifier.width(6.dp))
        Icon(Icons.Default.ChevronRight, null,
            tint = Terciario, modifier = Modifier.size(18.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String, badge: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold,
            color = Terciario, letterSpacing = 0.3.sp)
        if (badge != null) {
            Box(
                modifier = Modifier.size(18.dp).clip(CircleShape).background(Color(0xFFFFEBEE)),
                contentAlignment = Alignment.Center
            ) {
                Text(badge, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Primario)
            }
        }
    }
}
