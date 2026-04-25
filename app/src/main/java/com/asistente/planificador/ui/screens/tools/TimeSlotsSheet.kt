package com.asistente.planificador.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    var showDeleteConfirm by remember { mutableStateOf(false) }

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

            SectionDivider()

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
                    SectionSpacer()
                    AssociatedTaskSection(
                        task       = state.associatedTask,
                        onViewTask = { onViewTask(state.associatedTask.id) }
                    )
                }

                if (state.overlappingSlots.isNotEmpty()) {
                    SectionSpacer()
                    OverlappingSlotsSection(
                        overlapping = state.overlappingSlots,
                        onSlotClick = { onDismiss(); onOverlappingSlotClick(it) }
                    )
                }

                if (state.slot.slotType == SlotType.TASK_BLOCKED) {
                    Spacer(Modifier.height(12.dp))
                    AppBanner(
                        text  = "Esta franja se gestiona desde su tarea asociada.",
                        style = BannerStyle.INFO
                    )
                }
            }

            if (showDeleteConfirm) {
                DeleteConfirmDialog(
                    title     = when (state.slot.slotType) {
                        SlotType.BLOCKED      -> "¿Eliminar franja?"
                        SlotType.TASK_BLOCKED -> "¿Eliminar bloqueo de tarea?"
                    },
                    onConfirm = { onDelete(state.slot); onDismiss() },
                    onDismiss = { showDeleteConfirm = false }
                )
            }

            SectionDivider()
            DestructiveFooterButton(
                label   = when (state.slot.slotType) {
                    SlotType.BLOCKED      -> "Eliminar franja"
                    SlotType.TASK_BLOCKED -> "Eliminar bloqueo de tarea"
                },
                onClick = { showDeleteConfirm = true }   // ← antes era onDelete + onDismiss directamente
            )
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
        SheetDragHandle()

        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icono grande con fondo (44 dp, corner 11 dp)
            TintedIconBox(
                icon         = when (slot.slotType) {
                    SlotType.BLOCKED      -> Icons.Default.Block
                    SlotType.TASK_BLOCKED -> Icons.Default.CheckCircle
                },
                tint         = dotColor,
                boxSize      = 44.dp,
                iconSize     = 22.dp,
                cornerRadius = 11.dp
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    slot.slotType.label(),
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = badgeColor.second
                )
                Spacer(Modifier.height(2.dp))
                ItemTitle(text = slot.name)
            }

            EditActionButton(
                onClick = if (slot.slotType == SlotType.BLOCKED) onEdit else onEditTask
            )
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

// ── Fila de un campo de detalle (icono + label + valor) ──────────────────────
@Composable
private fun DetailInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TintedIconBox(icon = icon, tint = tint)
        Column {
            DetailLabel(label)
            DetailValue(value)
        }
    }
}

// ── Grid de días ─────────────────────────────────────────────────────────────
@Composable
private fun DetailDaysRow(daysOfWeek: List<Int>, dotColor: Color, isActive: Boolean = true) {
    val days       = listOf(1 to "L", 2 to "M", 3 to "X", 4 to "J", 5 to "V", 6 to "S", 7 to "D")
    val alpha      = if (isActive) 1f else 0.45f
    val bgInactive = Color(0xFFF2F2F2)

    Column {
        DetailLabel("Días activos")
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
                    if (slot.isActive) ColorActivo.copy(alpha = 0.12f)
                    else Terciario.copy(alpha = 0.10f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.PowerSettingsNew, null,
                tint     = if (slot.isActive) ColorActivo else Terciario,
                modifier = Modifier.size(17.dp)
            )
        }
        Text(
            if (slot.isActive) "Franja activa" else "Franja inactiva",
            fontSize   = 16.sp, fontWeight = FontWeight.SemiBold,
            color      = if (slot.isActive) ColorActivo else Terciario,
            modifier   = Modifier.weight(1f)
        )
        Switch(
            checked         = slot.isActive,
            onCheckedChange = { onToggleActive() },
            colors          = SwitchDefaults.colors(
                checkedTrackColor    = ColorActivo,
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
    val fmtShort = SimpleDateFormat("d MMM yyyy", Locale("es", "ES"))

    SectionTitle("Tarea asociada")
    Spacer(Modifier.height(10.dp))

    ClickableItemCard(onClick = onViewTask) {
        ColorAccentBar(color = Primario)
        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            InlineTypeLabel(Icons.Default.CheckCircle, "Tarea bloqueante", Primario)
            Spacer(Modifier.height(1.dp))
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    task.name,
                    fontSize   = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black,
                    maxLines   = 1, overflow = TextOverflow.Ellipsis,
                    modifier   = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                MetaText("${fmtShort.format(task.init_date)} – ${fmtShort.format(task.finish_date)}")
            }
        }

        Spacer(Modifier.width(6.dp))
        Icon(Icons.Default.ChevronRight, null, tint = Terciario, modifier = Modifier.size(18.dp))
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
        SectionTitle("Franjas solapadas")
        WarningCountChip(overlapping.size)
    }

    Spacer(Modifier.height(10.dp))

    AppBanner(
        text  = "En zonas de solapamiento las franjas de tarea tienen prioridad visual. " +
                "Las franjas manuales siguen activas pero pueden no verse en el calendario.",
        style = BannerStyle.WARNING
    )

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

    ClickableItemCard(onClick = onClick) {
        ColorAccentBar(color = dotColor)
        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            InlineTypeLabel(Icons.Default.CheckCircle, slot.slotType.label(), dotColor)
            Spacer(Modifier.height(1.dp))
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    slot.name,
                    fontSize   = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black,
                    maxLines   = 1, overflow = TextOverflow.Ellipsis,
                    modifier   = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                MetaText(
                    "${slot.startMinuteOfDay.toTimeString()} – ${slot.endMinuteOfDay.toTimeString()} · ${slot.recurrenceType.shortLabel()}",
                    modifier = Modifier  // softWrap false si lo necesitas
                )
            }
        }

        Spacer(Modifier.width(6.dp))
        Icon(Icons.Default.ChevronRight, null, tint = Terciario, modifier = Modifier.size(18.dp))
    }
}