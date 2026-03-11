package com.asistente.planificador.ui.screens.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asistente.core.domain.models.RecurrenceType
import com.asistente.core.domain.models.TimeSlot
import com.asistente.planificador.ui.screens.Primario
import com.asistente.planificador.ui.screens.Terciario
import com.asistente.planificador.ui.screens.badgeColors
import com.asistente.planificador.ui.screens.dotColor
import com.asistente.planificador.ui.screens.label
import com.asistente.planificador.ui.screens.shortLabel
import com.asistente.planificador.ui.viewmodels.toTimeString

@Composable
fun SlotsCarousel(
    filteredSlots: List<TimeSlot>,
    allSlots: List<TimeSlot>,
    onEdit: (TimeSlot) -> Unit,
    onDelete: (TimeSlot) -> Unit,
    onToggleActive: (TimeSlot) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visibleCount by remember { mutableStateOf(5) }
    // Activas primero, inactivas al final
    val sortedSlots = remember(filteredSlots) {
        filteredSlots.sortedWith(compareByDescending { it.isActive })
    }
    val toShow = sortedSlots.take(visibleCount)
    val hasMore = sortedSlots.size > visibleCount

    Column(modifier = modifier.fillMaxWidth()) {

        // Label + contadores
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "FRANJAS CONFIGURADAS",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Terciario,
                letterSpacing = 0.8.sp,
                modifier = Modifier.weight(1f)
            )
            if (allSlots.isNotEmpty()) {
                val activeCount = allSlots.count { it.isActive }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Activas
                    Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFE8F5E9)) {
                        Text(
                            "$activeCount activas",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF43A047),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        "${filteredSlots.size}/${allSlots.size}",
                        fontSize = 10.sp,
                        color = Terciario
                    )
                }
            }
        }

        when {
            allSlots.isEmpty() -> {
                EmptySlotState(onAdd = onAdd)
            }
            filteredSlots.isEmpty() -> {
                EmptyFilterState()
            }
            else -> {
                // Scroll horizontal: 4.5 cards visibles (160dp cada una + gaps)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    toShow.forEach { slot ->
                        SlotCarouselCard(
                            slot = slot,
                            onEdit = { onEdit(slot) },
                            onDelete = { onDelete(slot) },
                            onToggleActive = { onToggleActive(slot) }
                        )
                    }
                    if (hasMore) {
                        MoreSlotsCard(
                            remaining = filteredSlots.size - visibleCount,
                            onClick = { visibleCount += 5 }
                        )
                    }
                }
            }
        }
    }
}

// ── Card "Ver más" ────────────────────────────────────────────────────────────

@Composable
private fun MoreSlotsCard(remaining: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(158.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Color(0xFFE8E8E8))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(18.dp)) //bbb
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFEEEEEE), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "+$remaining",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF888888)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Ver más",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF888888),
                textAlign = TextAlign.Center
            )
            Text(
                "franjas",
                fontSize = 10.sp,
                color = Color(0xFFAAAAAA),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(18.dp))
        }
    }
}

// ── Card compacta del carrusel ────────────────────────────────────────────────

@Composable
private fun SlotCarouselCard(
    slot: TimeSlot,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val isActive   = slot.isActive
    val dotColor   = if (isActive) slot.slotType.dotColor() else Color(0xFFCCCCCC)
    val badgeColor = slot.slotType.badgeColors()
    val alpha      = if (isActive) 1f else 0.5f
    val textGray   = Terciario.copy(alpha = alpha)

    Card(
        modifier = Modifier.width(168.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isActive) Color.White else Color(0xFFF7F7F7)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 2.dp else 0.dp),
        border = if (!isActive) BorderStroke(1.dp, Color(0xFFE8E8E8)) else null
    ) {
        Column(modifier = Modifier.padding(horizontal = 11.dp, vertical = 11.dp)) {

            // ── Fila 1: badges + toggle + menú ────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // dot de color
                Box(modifier = Modifier.size(7.dp).background(dotColor, CircleShape))
                Spacer(Modifier.width(5.dp))
                // badge tipo franja
                Surface(shape = RoundedCornerShape(20.dp), color = badgeColor.first.copy(alpha = alpha)) {
                    Text(
                        slot.slotType.label(), fontSize = 9.sp, fontWeight = FontWeight.SemiBold,
                        color = badgeColor.second.copy(alpha = alpha), maxLines = 1,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(Modifier.width(3.dp))
                // badge recurrencia — ocupa el espacio restante
                Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFF0F0F0).copy(alpha = alpha), modifier = Modifier.weight(1f)) {
                    Text(
                        slot.recurrenceType.shortLabel(), fontSize = 8.sp, fontWeight = FontWeight.SemiBold,
                        color = textGray, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
                Spacer(Modifier.width(4.dp))
                // Botón toggle: icono PowerSettingsNew acorde a la estética roja de la app
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) Primario.copy(alpha = 0.10f) else Color(0xFFEEEEEE)
                        )
                        .clickable { onToggleActive() },

                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = if (isActive) "Desactivar" else "Activar",
                        tint = if (isActive) Primario else Color(0xFFBBBBBB),
                        modifier = Modifier.size(13.dp)
                    )
                }
                Spacer(Modifier.width(2.dp))
                // menú ⋮
                Box {
                    Icon(Icons.Default.MoreVert, null, tint = textGray,
                        modifier = Modifier.size(15.dp).clickable { showMenu = true })
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(if (isActive) "Desactivar" else "Activar", fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.PowerSettingsNew, null,
                                    tint = if (isActive) Terciario else Primario, modifier = Modifier.size(16.dp))
                            },
                            onClick = { showMenu = false; onToggleActive() }
                        )
                        DropdownMenuItem(
                            text = { Text("Editar", fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp)) },
                            onClick = { showMenu = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar", fontSize = 14.sp, color = Color(0xFFE53935)) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFE53935), modifier = Modifier.size(16.dp)) },
                            onClick = { showMenu = false; onDelete() }
                        )
                    }
                }
            }

            Spacer(Modifier.height(9.dp))

            // ── Nombre (2 líneas fijas) ────────────────────────────────
            Text(
                slot.name, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                color = if (isActive) Color(0xFF1A1A1A) else Color(0xFFAAAAAA),
                maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 17.sp, minLines = 2
            )

            Spacer(Modifier.height(7.dp))

            // ── Línea 1: fecha / tipo recurrencia con Icon ─────────────
            val fmtFull  = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale("es", "ES"))
            val fmtShort = java.text.SimpleDateFormat("d MMM", java.util.Locale("es", "ES"))
            val (recIcon, recLabel) = when (slot.recurrenceType) {
                RecurrenceType.SINGLE_DAY -> Pair(Icons.Default.Event,
                    slot.rangeStart?.let { fmtFull.format(it) } ?: "—")
                RecurrenceType.DATE_RANGE -> Pair(Icons.Default.DateRange,
                    "${slot.rangeStart?.let { fmtShort.format(it) } ?: "?"} – ${slot.rangeEnd?.let { fmtShort.format(it) } ?: "?"}")
                RecurrenceType.EVEN_WEEKS -> Pair(Icons.Default.Repeat, "Semanas pares")
                RecurrenceType.ODD_WEEKS  -> Pair(Icons.Default.Repeat, "Semanas impares")
                RecurrenceType.WEEKLY     -> Pair(Icons.Default.Repeat, "Todas las semanas")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(recIcon, null,
                    tint = (if (isActive) Primario else Terciario).copy(alpha = alpha),
                    modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(3.dp))
                Text(recLabel, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = (if (isActive) Primario else Terciario).copy(alpha = alpha),
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Spacer(Modifier.height(3.dp))

            // ── Línea 2: hora ──────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, null, tint = Terciario, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(3.dp))
                Text(
                    "${slot.startMinuteOfDay.toTimeString()} – ${slot.endMinuteOfDay.toTimeString()}",
                    fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Terciario
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Mini timeline ──────────────────────────────────────────
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth().height(5.dp)
                    .clip(RoundedCornerShape(3.dp)).background(Color(0xFFF0F0F0))
            ) {
                val startFrac = slot.startMinuteOfDay / 1440f
                val widthFrac = (slot.endMinuteOfDay - slot.startMinuteOfDay) / 1440f
                Box(modifier = Modifier
                    .offset(x = maxWidth * startFrac)
                    .width(maxWidth * widthFrac)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(dotColor.copy(alpha = alpha)))
            }

            Spacer(Modifier.height(9.dp))

            // ── 7 cuadrados de días — full width, cuadrado perfecto ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                listOf(1 to "L", 2 to "M", 3 to "X", 4 to "J", 5 to "V", 6 to "S", 7 to "D")
                    .forEach { (num, lbl) ->
                        val dayActive = slot.daysOfWeek.contains(num)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    if (dayActive) dotColor.copy(alpha = if (isActive) 0.20f else 0.08f)
                                    else Color(0xFFF2F2F2)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = lbl,
                                fontSize = 9.sp,
                                fontWeight = if (dayActive) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (dayActive) dotColor.copy(alpha = alpha) else Color(0xFFCCCCCC),
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false // Elimina el espacio extra superior/inferior
                                    ),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both
                                    )
                                )
                            )
                        }
                    }
            }
        }
    }
}

@Composable
private fun EmptySlotState(onAdd: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(68.dp).background(Color(0xFFF3E5E2), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("🗓", fontSize = 30.sp)
        }
        Spacer(Modifier.height(14.dp))
        Text("Sin franjas configuradas", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
        Spacer(Modifier.height(6.dp))
        Text(
            "Añade franjas para que el asistente sepa cuándo puede planificar actividades.",
            fontSize = 13.sp, color = Terciario, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = onAdd,
            colors = ButtonDefaults.buttonColors(containerColor = Primario),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.height(46.dp)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Nueva franja", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ESTADOS VACÍOS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyFilterState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(56.dp).background(Color(0xFFF0F0F0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.SearchOff, null, tint = Terciario, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text("Sin resultados", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
        Spacer(Modifier.height(4.dp))
        Text(
            "Prueba con otro nombre o cambia el filtro.",
            fontSize = 13.sp, color = Terciario, textAlign = TextAlign.Center
        )
    }
}