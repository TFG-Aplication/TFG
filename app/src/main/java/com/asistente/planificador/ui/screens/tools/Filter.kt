package com.asistente.planificador.ui.screens.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.jarjarred.org.antlr.v4.codegen.model.Sync
import com.asistente.core.domain.models.RecurrenceType
import com.asistente.core.domain.models.SlotType
import com.asistente.planificador.ui.screens.Primario
import com.asistente.planificador.ui.screens.Terciario
import com.asistente.planificador.ui.screens.shortLabel
import kotlin.collections.forEach

// ─────────────────────────────────────────────────────────────────────────────
// BARRA BÚSQUEDA + FILTROS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SearchAndFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    activeTypeFilters: Set<SlotType>,
    onTypeFilterToggle: (SlotType) -> Unit,
    activeRecurrenceFilters: Set<RecurrenceType>,
    onRecurrenceFilterToggle: (RecurrenceType) -> Unit,
    activeStatusFilter: Boolean?,
    onStatusFilterChange: (Boolean) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val hasActiveFilters = activeTypeFilters.isNotEmpty() || activeRecurrenceFilters.isNotEmpty() || activeStatusFilter != null

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {

        // ── Campo de búsqueda ──────────────────────────────────────────
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            border = BorderStroke(
                width = if (isFocused) 1.5.dp else 1.dp,
                color = if (isFocused) Primario else Color(0xFFEEEEEE)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.Search, null,
                    tint = if (isFocused) Primario else Terciario,
                    modifier = Modifier.size(18.dp)
                )
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f).onFocusChanged { isFocused = it.isFocused },
                    textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                    singleLine = true,
                    decorationBox = { inner ->
                        if (query.isEmpty()) {
                            Text("Buscar franjas...", fontSize = 14.sp, color = Terciario.copy(alpha = 0.6f))
                        }
                        inner()
                    }
                )
                if (query.isNotEmpty()) {
                    Icon(
                        Icons.Default.Close, "Limpiar",
                        tint = Terciario,
                        modifier = Modifier.size(16.dp).clickable { onQueryChange("") }
                    )
                }
            }
        }

        // ── Fila: botón Filtros fijo + chips activos con scroll ────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Botón FILTROS — siempre visible, nunca desaparece
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (hasActiveFilters) Primario else Color.White,
                border = BorderStroke(
                    1.dp,
                    if (hasActiveFilters) Primario else Color(0xFFDDDDDD)
                ),
                modifier = Modifier.clickable { showFilterSheet = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Filtros",
                        modifier = Modifier.size(14.dp),
                        tint = if (hasActiveFilters) Color.White else Terciario
                    )
                    Text(
                        "Filtros",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasActiveFilters) Color.White else Terciario
                    )
                    if (hasActiveFilters) {
                        val count = activeTypeFilters.size + activeRecurrenceFilters.size + if (activeStatusFilter != null) 1 else 0
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color.White.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$count",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false // <--- Esto elimina el desplazamiento hacia abajo
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

            Spacer(Modifier.width(8.dp))

            // Chips activos con scroll horizontal
            if (hasActiveFilters) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    activeTypeFilters.forEach { type ->
                        val (label, accent, bg) = when (type) {
                            SlotType.BLOCKED      -> Triple("Bloqueada", Color(0xFFE53935), Color(0xFFFFEBEE))
                            SlotType.TASK_BLOCKED -> Triple("Por tarea", Color(0xFF7B1FA2), Color(0xFFF3E5F5))
                        }
                        ActiveFilterChip(label = label, bg = bg, textColor = accent, onRemove = { onTypeFilterToggle(type) })
                    }
                    activeRecurrenceFilters.forEach { type ->
                        ActiveFilterChip(
                            label = type.shortLabel(),
                            bg = Color(0xFFEEEEEE),
                            textColor = Color(0xFF555555),
                            onRemove = { onRecurrenceFilterToggle(type) }
                        )
                    }
                    activeStatusFilter?.let { active ->
                        ActiveFilterChip(
                            label = if (active) "Activas" else "Inactivas",
                            bg = if (active) Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
                            textColor = if (active) Color(0xFF43A047) else Color(0xFF888888),
                            onRemove = { onStatusFilterChange(active) }
                        )
                    }
                }
            } else {
                // Hint cuando no hay filtros
                Text(
                    "Sin filtros activos",
                    fontSize = 11.sp,
                    color = Terciario.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // ── Bottom Sheet de filtros ────────────────────────────────────────
    if (showFilterSheet) {
        FilterBottomSheet(
            activeTypeFilters = activeTypeFilters,
            onTypeFilterToggle = onTypeFilterToggle,
            activeRecurrenceFilters = activeRecurrenceFilters,
            onRecurrenceFilterToggle = onRecurrenceFilterToggle,
            activeStatusFilter = activeStatusFilter,
            onStatusFilterChange = onStatusFilterChange,
            onClearAll = onClearAll,
            onDismiss = { showFilterSheet = false }
        )
    }
}

// ── Chip de filtro activo (con X para quitar) ─────────────────────────────────

@Composable
private fun ActiveFilterChip(
    label: String,
    bg: Color,
    textColor: Color,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bg
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(textColor.copy(alpha = 0.15f), CircleShape)
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Quitar filtro",
                    modifier = Modifier.size(9.dp),
                    tint = textColor
                )
            }
        }
    }
}


// ── Bottom Sheet con todos los filtros ────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    activeTypeFilters: Set<SlotType>,
    onTypeFilterToggle: (SlotType) -> Unit,
    activeRecurrenceFilters: Set<RecurrenceType>,
    onRecurrenceFilterToggle: (RecurrenceType) -> Unit,
    activeStatusFilter: Boolean?,
    onStatusFilterChange: (Boolean) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val hasAny = activeTypeFilters.isNotEmpty() || activeRecurrenceFilters.isNotEmpty() || activeStatusFilter != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Filtros", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black, modifier = Modifier.weight(1f))
                if (hasAny) {
                    TextButton(onClick = { onClearAll(); onDismiss() }) {
                        Text("Limpiar todo", color = Primario, fontSize = 13.sp)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))

            // --- TIPO DE FRANJA (Se mantiene igual) ---
            Text("TIPO DE FRANJA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Terciario, letterSpacing = 0.8.sp)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    SlotType.BLOCKED      to Triple("Bloqueada",  Color(0xFFE53935), Color(0xFFFFEBEE)),
                    SlotType.TASK_BLOCKED to Triple("Por tarea",  Color(0xFF7B1FA2), Color(0xFFF3E5F5))
                ).forEach { (type, triple) ->
                    val (label, accent, bg) = triple
                    val selected = type in activeTypeFilters
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) accent else bg,
                        border = BorderStroke(1.5.dp, if (selected) accent else accent.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f).clickable { onTypeFilterToggle(type) }
                    ) {
                        Column(modifier = Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.size(8.dp).background(if (selected) Color.White else accent, CircleShape))
                            Spacer(Modifier.height(6.dp))
                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                color = if (selected) Color.White else accent, textAlign = TextAlign.Center)
                            if (selected) {
                                Spacer(Modifier.height(3.dp))
                                Icon(Icons.Default.Check, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(10.dp))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- ESTADO (Con Iconos) ---
            Text("ESTADO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Terciario, letterSpacing = 0.8.sp)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    true  to ("Activas" to Icons.Default.PlayArrow),
                    false to ("Inactivas" to Icons.Default.Pause)
                ).forEach { (status, data) ->
                    val (label, icon) = data
                    val selected = activeStatusFilter == status
                    val accent = if (status) Color(0xFF43A047) else Color(0xFF888888)
                    val bg = if (status) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) accent else bg,
                        border = BorderStroke(1.5.dp, if (selected) accent else accent.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f).clickable { onStatusFilterChange(status) }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                icon, null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selected) Color.White else accent
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                label, fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) Color.White else accent
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- RECURRENCIA (Con Iconos) ---
            Text("RECURRENCIA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Terciario, letterSpacing = 0.8.sp)
            Spacer(Modifier.height(10.dp))
            listOf(
                RecurrenceType.WEEKLY     to ("Semanal" to Icons.Default.Sync),
                RecurrenceType.EVEN_WEEKS to ("S. pares" to Icons.Default.DateRange),
                RecurrenceType.ODD_WEEKS  to ("S. impares" to Icons.Default.Event),
                RecurrenceType.DATE_RANGE to ("Rango" to Icons.Default.CalendarMonth),
                RecurrenceType.SINGLE_DAY to ("Día único" to Icons.Default.PushPin)
            ).chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    row.forEach { (type, data) ->
                        val (label, icon) = data
                        val selected = type in activeRecurrenceFilters
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) Color(0xFF444444) else Color(0xFFF5F5F5),
                            border = BorderStroke(1.5.dp, if (selected) Color(0xFF444444) else Color(0xFFDDDDDD)),
                            modifier = Modifier.weight(1f).clickable { onRecurrenceFilterToggle(type) }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    icon, null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (selected) Color.White else Color(0xFF555555)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    label, fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) Color.White else Color(0xFF555555)
                                )
                            }
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primario)
            ) {
                Text("Aplicar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ESTADOS VACÍOS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
public fun EmptyFilterState() {
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