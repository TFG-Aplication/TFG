package com.asistente.planificador.ui.screens.timeSlots

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asistente.core.domain.models.RecurrenceType
import com.asistente.core.domain.models.SlotType
import com.asistente.core.domain.models.TimeSlot
import com.asistente.planificador.ui.screens.label
import com.asistente.planificador.ui.screens.tools.DeleteConfirmDialog
import com.asistente.planificador.ui.screens.tools.Primario
import com.asistente.planificador.ui.screens.tools.Secundario
import com.asistente.planificador.ui.screens.tools.Terciario
import com.asistente.planificador.ui.screens.tools.badgeColors
import com.asistente.planificador.ui.screens.tools.colorCuarto
import com.asistente.planificador.ui.screens.tools.darkenColor
import com.asistente.planificador.ui.screens.tools.dotColor
import com.asistente.planificador.ui.viewmodels.toTimeString
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val CARD_WIDTH  = 160.dp
private val CARD_HEIGHT = 180.dp

// ─────────────────────────────────────────────────────────────────────────────
// CARD PÚBLICA
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SlotCarouselCard(
    slot: TimeSlot,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier,
    categoryName: String? = null,
    categoryColor: Color? = null
) {
    when (slot.slotType) {
        SlotType.BLOCKED ->
            BlockedSlotCard(slot, onEdit, onDelete, onToggleActive, onCardClick, modifier)
        SlotType.TASK_BLOCKED ->
            TaskBlockedSlotCard(slot, onEdit, onDelete, onCardClick, modifier, categoryName, categoryColor)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CARD TIPO 2 — franja manual (BLOCKED)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BlockedSlotCard(
    slot: TimeSlot,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enable   = slot.enable
    val dotColor   = if (enable) slot.slotType.dotColor() else Color(0xFFCCCCCC)
    val badgeColor = slot.slotType.badgeColors()
    val alpha      = if (enable) 1f else 0.45f
    var showMenu   by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    SlotCardContainer(enable = enable, onClick = onCardClick, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 11.dp)
        ) {
            // 1 ── Tipo + menú ─────────────────────────────────────────
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(7.dp).background(badgeColor.first, CircleShape))
                Spacer(Modifier.width(5.dp))
                Text(
                    slot.slotType.label(),
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = badgeColor.second.copy(alpha = alpha),
                    maxLines   = 1,
                    modifier   = Modifier.weight(1f)
                )
                SlotMenuButton(
                    showMenu   = showMenu,
                    onShowMenu = { showMenu = true },
                    onDismiss  = { showMenu = false },
                    menuItems  = listOf(
                        SlotMenuItem(
                            label   = if (enable) "Desactivar" else "Activar",
                            icon    = Icons.Default.PowerSettingsNew,
                            tint    = if (enable) Terciario else Primario,
                            onClick = { showMenu = false; onToggleActive() }
                        ),
                        SlotMenuItem(
                            label   = "Editar",
                            icon    = Icons.Default.Edit,
                            tint    = Color(0xFF1A1A1A),
                            onClick = { showMenu = false; onEdit() }
                        ),
                        SlotMenuItem(
                            label         = "Eliminar",
                            icon          = Icons.Default.Delete,
                            tint          = Color(0xFFE53935),
                            onClick       = { showMenu = false; showDeleteConfirm = true },
                            isDestructive = true
                        )
                    )
                )
            }

            Spacer(Modifier.height(5.dp))

            // 2 ── Título (1 línea, grande) ────────────────────────────
            Text(
                slot.name,
                fontWeight = FontWeight.Bold,
                fontSize   = 20.sp,
                lineHeight = 22.sp,
                color      = if (enable) Color(0xFF1A1A1A) else Color(0xFFAAAAAA),
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            // 3 ── Estado activo/inactivo — solo label pequeña ─────────

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.PowerSettingsNew, null,
                    tint = if (enable) Color(0xFF86cf66) else Terciario.copy(alpha = 0.6f),
                    modifier = Modifier.size(13.dp).offset(y = -0.7.dp)
                )
                Text(
                    if (enable) "ACTIVA" else "INACTIVA",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enable) Color(0xFF86cf66) else Terciario.copy(alpha = 0.6f)
                )
            }
            Spacer(Modifier.height(4.dp))

            // 4 ── Hora ────────────────────────────────────────────────
            SlotTimeRow(slot = slot, alpha = alpha)

            Spacer(Modifier.height(3.dp))

            // 5 ── Recurrencia ─────────────────────────────────────────
            SlotRecurrenceRow(slot = slot, enable = enable, alpha = alpha)

            Spacer(Modifier.weight(1f))

            // 6 ── Mini timeline ───────────────────────────────────────
            SlotTimeline(slot = slot, dotColor = dotColor, alpha = alpha)

            Spacer(Modifier.height(4.dp))

            // 7 ── Días ────────────────────────────────────────────────
            SlotDaysRow(slot = slot, dotColor = dotColor, enable = enable, alpha = alpha)
            if (showDeleteConfirm) {
                DeleteConfirmDialog(
                    title = "¿Eliminar tarea?",
                    onConfirm = { onDelete() },
                    onDismiss = { showDeleteConfirm = false }
                )
            }
        }

    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CARD TIPO 1 — tarea bloqueante (TASK_BLOCKED)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TaskBlockedSlotCard(
    slot: TimeSlot,
    onEditTask: () -> Unit,
    onDelete: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier,
    categoryName: String? = null,
    categoryColor: Color? = null
) {
    val dotColor   = slot.slotType.dotColor()
    val badgeColor = slot.slotType.badgeColors()
    var showMenu   by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val highlightDays: Set<Int> = remember(slot) {
        when (slot.recurrenceType) {
            RecurrenceType.SINGLE_DAY -> {
                slot.rangeStart?.let {
                    val cd = Calendar.getInstance().apply { time = it }.get(Calendar.DAY_OF_WEEK)
                    setOf(if (cd == Calendar.SUNDAY) 7 else cd - 1)
                } ?: emptySet()
            }
            RecurrenceType.TASK_RANGE -> {
                val start = slot.rangeStart ?: return@remember emptySet()
                val end   = slot.rangeEnd   ?: return@remember emptySet()
                val result = mutableSetOf<Int>()
                val cal = Calendar.getInstance().apply { time = start }
                val endDay = Calendar.getInstance().apply {
                    time = end
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                while (true) {
                    val dow = cal.get(Calendar.DAY_OF_WEEK)
                    result.add(if (dow == Calendar.SUNDAY) 7 else dow - 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                    if (cal.timeInMillis >= endDay) break
                    cal.add(Calendar.DAY_OF_MONTH, 1)
                }
                result
            }
            else -> emptySet()
        }
    }

    SlotCardContainer(enable = true, onClick = onCardClick, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 11.dp)
        ) {
            // 1 ── Tipo + menú ─────────────────────────────────────────
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(7.dp).background(badgeColor.first, CircleShape))
                Spacer(Modifier.width(5.dp))
                Text(
                    slot.slotType.label(),
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = badgeColor.second,
                    maxLines   = 1,
                    modifier   = Modifier.weight(1f)
                )
                SlotMenuButton(
                    showMenu   = showMenu,
                    onShowMenu = { showMenu = true },
                    onDismiss  = { showMenu = false },
                    menuItems  = listOf(
                        SlotMenuItem(
                            label   = "Editar tarea",
                            icon    = Icons.Default.Edit,
                            tint    = Color(0xFF1A1A1A),
                            onClick = { showMenu = false; onEditTask() }
                        ),
                        SlotMenuItem(
                            label         = "Eliminar bloqueo",
                            icon          = Icons.Default.LinkOff,
                            tint          = Color(0xFFE53935),
                            onClick       = { showMenu = false;  showDeleteConfirm = true },
                            isDestructive = true
                        )
                    )
                )
            }

            Spacer(Modifier.height(5.dp))

            // 2 ── Título (1 línea, grande) ────────────────────────────
            Text(
                slot.name,
                fontWeight = FontWeight.Bold,
                fontSize   = 20.sp,
                lineHeight = 22.sp,
                color      = Color(0xFF1A1A1A),
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            // 3 ── Categoría como label ────────────────────────────────
            val hasCat     = categoryName != null
            val baseColor  = if (hasCat)   (categoryColor ?: darkenColor(colorCuarto)) else darkenColor(
                colorCuarto
            )
            val labelText  = (categoryName ?: "Sin categoría").uppercase()
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle, null,
                        tint     = baseColor,
                        modifier = Modifier.size(13.dp).offset(y = -0.7.dp)
                    )
                    Text(
                        labelText,
                        fontSize      = 13.sp,
                        fontWeight    = FontWeight.Bold,
                        color         = baseColor,
                        letterSpacing = 0.3.sp,
                        maxLines      = 1,
                        overflow      = TextOverflow.Ellipsis
                    )
                }


            Spacer(Modifier.height(4.dp))

            // 4 ── Hora ────────────────────────────────────────────────
            SlotTimeRow(slot = slot, alpha = 1f)

            Spacer(Modifier.height(3.dp))

            // 5 ── Recurrencia ─────────────────────────────────────────
            SlotRecurrenceRow(slot = slot, enable = true, alpha = 1f)

            Spacer(Modifier.weight(1f))

            // 6 ── Mini timeline ───────────────────────────────────────
            SlotTimeline(slot = slot, dotColor = dotColor, alpha = 1f)

            Spacer(Modifier.height(4.dp))

            // 7 ── Días ────────────────────────────────────────────────
            SlotDaysRow(
                slot         = slot,
                dotColor     = dotColor,
                enable     = true,
                alpha        = 1f,
                highlightDays = highlightDays
            )
        }
        if (showDeleteConfirm) {
            DeleteConfirmDialog(
                title = "¿Eliminar tarea?",
                onConfirm = { onDelete() },
                onDismiss = { showDeleteConfirm = false }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CONTENEDOR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SlotCardContainer(
    enable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier  = modifier
            .width(CARD_WIDTH)
            .height(CARD_HEIGHT)
            .clickable { onClick() },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (enable) Color.White else Color(0xFFF7F7F7)
        )
    ) { content() }
}

// ─────────────────────────────────────────────────────────────────────────────
// MENÚ ⋮ COMPARTIDO
// ─────────────────────────────────────────────────────────────────────────────

private data class SlotMenuItem(
    val label: String,
    val icon: ImageVector,
    val tint: Color,
    val onClick: () -> Unit,
    val isDestructive: Boolean = false
)

@Composable
private fun SlotMenuButton(
    showMenu: Boolean,
    onShowMenu: () -> Unit,
    onDismiss: () -> Unit,
    menuItems: List<SlotMenuItem>
) {
    Box {
        Icon(
            Icons.Default.MoreVert, contentDescription = null,
            tint     = Terciario,
            modifier = Modifier.size(16.dp).clickable { onShowMenu() }
        )
        DropdownMenu(
            expanded         = showMenu,
            onDismissRequest = onDismiss,
            modifier         = Modifier.background(Color.White)
        ) {
            menuItems.forEachIndexed { index, item ->
                if (item.isDestructive && index > 0) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color     = Color(0xFFF0F0F0),
                        modifier  = Modifier.padding(horizontal = 8.dp)
                    )
                }
                DropdownMenuItem(
                    text = {
                        Text(
                            item.label,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color      = if (item.isDestructive) Color(0xFFE53935)
                            else Color(0xFF1A1A1A)
                        )
                    },
                    leadingIcon = {
                        Icon(item.icon, null, tint = item.tint, modifier = Modifier.size(18.dp))
                    },
                    onClick = item.onClick
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SUBCOMPONENTES
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SlotRecurrenceRow(slot: TimeSlot, enable: Boolean, alpha: Float) {
    val fmtFull  = SimpleDateFormat("d MMM yyyy", Locale("es", "ES"))
    val fmtShort = SimpleDateFormat("d MMM",      Locale("es", "ES"))

    val (recIcon, recLabel) = when (slot.recurrenceType) {
        RecurrenceType.SINGLE_DAY ->
            Icons.Default.Event to (slot.rangeStart?.let { fmtFull.format(it) } ?: "—")
        RecurrenceType.DATE_RANGE ->
            Icons.Default.DateRange to
                    "${slot.rangeStart?.let { fmtShort.format(it) } ?: "?"} – " +
                    (slot.rangeEnd?.let { fmtShort.format(it) } ?: "?")
        RecurrenceType.EVEN_WEEKS -> Icons.Default.Repeat to "Semanas pares"
        RecurrenceType.ODD_WEEKS  -> Icons.Default.Repeat to "Semanas impares"
        RecurrenceType.WEEKLY     -> Icons.Default.Repeat to "Todas las semanas"
        RecurrenceType.TASK_RANGE ->
            Icons.Default.DateRange to
                    "${slot.rangeStart?.let { fmtShort.format(it) } ?: "?"} – " +
                    (slot.rangeEnd?.let { fmtShort.format(it) } ?: "?")
    }

    val color = (if (enable) Primario else Terciario).copy(alpha = alpha)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(recIcon, null, tint = color, modifier = Modifier.size(13.dp).offset(y = -0.7.dp))
        Spacer(Modifier.width(4.dp))
        Text(
            recLabel,
            fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            color    = color, maxLines = 1, overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SlotTimeRow(slot: TimeSlot, alpha: Float) {
    val color = Color(0xFF1A1A1A).copy(alpha = alpha)
    val fmtTime = SimpleDateFormat("HH:mm", Locale("es", "ES"))

    val timeLabel = if (slot.recurrenceType == RecurrenceType.TASK_RANGE) {
        val start = slot.rangeStart?.let { fmtTime.format(it) } ?: slot.startMinuteOfDay.toTimeString()
        val end   = slot.rangeEnd?.let   { fmtTime.format(it) } ?: slot.endMinuteOfDay.toTimeString()
        "$start – $end"
    } else {
        "${slot.startMinuteOfDay.toTimeString()} – ${slot.endMinuteOfDay.toTimeString()}"
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.AccessTime, null,
            tint = color, modifier = Modifier.size(13.dp).offset(y = -0.7.dp))
        Spacer(Modifier.width(4.dp))
        Text(timeLabel, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun SlotTimeline(slot: TimeSlot, dotColor: Color, alpha: Float) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth().height(4.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Secundario)
    ) {
        val startFrac = slot.startMinuteOfDay / 1440f
        val widthFrac = (slot.endMinuteOfDay - slot.startMinuteOfDay) / 1440f
        Box(
            modifier = Modifier
                .offset(x = maxWidth * startFrac)
                .width(maxWidth * widthFrac)
                .fillMaxHeight()
                .clip(RoundedCornerShape(3.dp))
                .background(darkenColor(dotColor.copy(alpha = alpha)))
        )
    }
}

@Composable
private fun SlotDaysRow(
    slot: TimeSlot,
    dotColor: Color,
    enable: Boolean,
    alpha: Float,
    highlightDays: Set<Int> = emptySet()   // ← antes era highlightDay: Int?
) {
    val days = listOf(1 to "L", 2 to "M", 3 to "X", 4 to "J", 5 to "V", 6 to "S", 7 to "D")
    val hasHighlight = highlightDays.isNotEmpty()

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        days.forEach { (num, lbl) ->
            val isHighlighted = hasHighlight && num in highlightDays
            val isDimmed      = hasHighlight && num !in highlightDays
            val dayActive     = if (!hasHighlight) slot.daysOfWeek.contains(num) else isHighlighted

            val bgColor = when {
                isHighlighted -> dotColor.copy(alpha = 0.5f)
                isDimmed      -> Color(0xFFF2F2F2)
                dayActive     -> dotColor.copy(alpha = if (enable) 0.5f else 0.07f)
                else          -> Secundario
            }
            val textColor = when {
                isHighlighted -> darkenColor(dotColor)
                isDimmed      -> Color(0xFFCCCCCC)
                dayActive     -> darkenColor(dotColor.copy(alpha = alpha))
                else          -> Terciario.copy(alpha = alpha)
            }

            Box(
                modifier = Modifier
                    .weight(1f).aspectRatio(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    lbl, fontSize = 8.sp, lineHeight = 8.sp,
                    fontWeight = if (isHighlighted || dayActive) FontWeight.Bold else FontWeight.Normal,
                    color      = textColor
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CARD "VER MÁS / VER MENOS"
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MoreSlotsCard(remaining: Int, expanded: Boolean, onClick: () -> Unit) {
    Card(
        modifier  = Modifier
            .width(CARD_WIDTH)
            .height(CARD_HEIGHT)
            .clickable { onClick() },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Secundario),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier            = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier         = Modifier.size(40.dp).background(darkenColor(Secundario), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint     = Terciario,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            if (!expanded) {
                Text(
                    "+$remaining",
                    fontSize   = 14.sp, fontWeight = FontWeight.Bold,
                    color      = Terciario
                )
                Spacer(Modifier.height(2.dp))
            }
            Text(
                if (expanded) "Ver menos" else "Ver más",
                fontSize   = 11.sp, fontWeight = FontWeight.SemiBold,
                color      = Terciario
            )
            Text("franjas", fontSize = 9.sp, color = Terciario)
        }
    }
}