package com.asistente.planificador.ui.screens.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asistente.core.domain.models.RecurrenceType
import com.asistente.core.domain.models.SlotType
import com.asistente.planificador.ui.screens.shortLabel

// ─────────────────────────────────────────────────────────────────────────────
// BARRA BÚSQUEDA + FILTROS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SearchAndFilterBar(
    query                   : String,
    onQueryChange           : (String) -> Unit,
    activeTypeFilters       : Set<SlotType>,
    onTypeFilterToggle      : (SlotType) -> Unit,
    activeRecurrenceFilters : Set<RecurrenceType>,
    onRecurrenceFilterToggle: (RecurrenceType) -> Unit,
    activeStatusFilter      : Boolean?,
    onStatusFilterChange    : (Boolean) -> Unit,
    onClearAll              : () -> Unit,
    modifier                : Modifier = Modifier
) {
    var isFocused       by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val hasActiveFilters = activeTypeFilters.isNotEmpty()
            || activeRecurrenceFilters.isNotEmpty()
            || activeStatusFilter != null

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {

        // ── Campo de búsqueda ─────────────────────────────────────────────────
        Surface(
            shape    = RoundedCornerShape(14.dp),
            color    = Color.White,
            border   = BorderStroke(1.dp, if (isFocused) Primario else Color(0xFFEEEEEE)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.Search, null,
                    tint     = if (isFocused) Primario else Terciario,
                    modifier = Modifier.size(18.dp)
                )
                BasicTextField(
                    value         = query,
                    onValueChange = onQueryChange,
                    modifier      = Modifier.weight(1f).onFocusChanged { isFocused = it.isFocused },
                    textStyle     = TextStyle(fontSize = 14.sp, color = Color.Black),
                    singleLine    = true,
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
                        tint     = Terciario,
                        modifier = Modifier.size(16.dp).clickable { onQueryChange("") }
                    )
                }
            }
        }

        // ── Fila de filtros ───────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.fillMaxWidth()
        ) {
            // Botón "Filtros"
            Surface(
                shape    = RoundedCornerShape(6.dp),
                color    = if (hasActiveFilters) Primario else Color.White,
                modifier = Modifier.clickable { showFilterSheet = true }
            ) {
                Row(
                    modifier              = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        Icons.Default.FilterList, "Filtros",
                        modifier = Modifier.size(14.dp),
                        tint     = if (hasActiveFilters) Color.White else Terciario
                    )
                    Text(
                        "Filtros",
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (hasActiveFilters) Color.White else Terciario
                    )
                    if (hasActiveFilters) {
                        val count = activeTypeFilters.size +
                                activeRecurrenceFilters.size +
                                if (activeStatusFilter != null) 1 else 0
                        Box(
                            modifier         = Modifier
                                .size(16.dp)
                                .background(Color.White.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$count",
                                fontSize  = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color     = Color.White,
                                style     = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // Chips de filtros activos o texto vacío
            if (hasActiveFilters) {
                Row(
                    modifier              = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    activeTypeFilters.forEach { type ->
                        val (bg, textColor) = type.badgeColors()
                        ActiveFilterChip(
                            label     = when (type) {
                                SlotType.BLOCKED      -> "Bloqueada Manualmente"
                                SlotType.TASK_BLOCKED -> "Bloqueada por tarea"
                            },
                            bg        = bg,
                            textColor = textColor,
                            onRemove  = { onTypeFilterToggle(type) }
                        )
                    }
                    activeRecurrenceFilters.forEach { type ->
                        ActiveFilterChip(
                            label     = type.shortLabel(),
                            bg        = colorCuarto,
                            textColor = darkenColor(colorCuarto),
                            onRemove  = { onRecurrenceFilterToggle(type) }
                        )
                    }
                    activeStatusFilter?.let { active ->
                        ActiveFilterChip(
                            label     = if (active) "Activas" else "Inactivas",
                            bg        = if (active) ColorActivo.copy(alpha = 0.25f)
                            else IconAlarma.copy(alpha = 0.35f),
                            textColor = if (active) darkenColor(ColorActivo) else darkenColor(IconAlarma),
                            onRemove  = { onStatusFilterChange(active) }
                        )
                    }
                }
            } else {
                Text("Sin filtros activos", fontSize = 13.sp, fontWeight = FontWeight.Medium,
                    color = Terciario.copy(alpha = 0.8f))
            }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            activeTypeFilters        = activeTypeFilters,
            onTypeFilterToggle       = onTypeFilterToggle,
            activeRecurrenceFilters  = activeRecurrenceFilters,
            onRecurrenceFilterToggle = onRecurrenceFilterToggle,
            activeStatusFilter       = activeStatusFilter,
            onStatusFilterChange     = onStatusFilterChange,
            onClearAll               = onClearAll,
            onDismiss                = { showFilterSheet = false }
        )
    }
}

// ── Chip de filtro activo (con X para quitar) ─────────────────────────────────

@Composable
private fun ActiveFilterChip(
    label    : String,
    bg       : Color,
    textColor: Color,
    onRemove : () -> Unit
) {
    Surface(shape = RoundedCornerShape(6.dp), color = bg) {
        Row(
            modifier              = Modifier.padding(start = 10.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(textColor.copy(alpha = 0.15f), CircleShape)
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Close, "Quitar filtro",
                    modifier = Modifier.size(9.dp),
                    tint     = textColor
                )
            }
        }
    }
}

// ── Bottom Sheet con todos los filtros ────────────────────────────────────────

// ── Bottom Sheet con todos los filtros ────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    activeTypeFilters       : Set<SlotType>,
    onTypeFilterToggle      : (SlotType) -> Unit,
    activeRecurrenceFilters : Set<RecurrenceType>,
    onRecurrenceFilterToggle: (RecurrenceType) -> Unit,
    activeStatusFilter      : Boolean?,
    onStatusFilterChange    : (Boolean) -> Unit,
    onClearAll              : () -> Unit,
    onDismiss               : () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val hasAny     = activeTypeFilters.isNotEmpty()
            || activeRecurrenceFilters.isNotEmpty()
            || activeStatusFilter != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = Color.White,
        shape            = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        // 1. CONTROL DE LA BARRA SUPERIOR
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 56.dp, height = 4.dp)
                    .background(
                        color = Primario,
                        shape = CircleShape
                    )
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 24.dp, top = 12.dp)) {

            // ── Título + limpiar ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Filtros",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = { if (hasAny) { onClearAll(); onDismiss() } },
                    enabled = hasAny,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = ColorDestructive,
                        disabledContentColor = Color.Transparent
                    )
                ) {
                    Text("Limpiar todo", fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Tipo de franja ────────────────────────────────────────────────
            SectionTitle("Tipo de franja")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    SlotType.BLOCKED      to (Icons.Default.Block       to "Bloqueada Manualmente"),
                    SlotType.TASK_BLOCKED to (Icons.Default.CheckCircle to "Bloqueada por Tarea")
                ).forEach { (type, iconLabel) ->
                    val (icon, label)   = iconLabel
                    val (bg, textColor) = type.badgeColors()   // design system
                    val selected        = type in activeTypeFilters

                    Surface(
                        shape    = RoundedCornerShape(12.dp),
                        color    = if (selected) bg else ColorGrisFondo,
                        modifier = Modifier.weight(1f).height(50.dp).clickable { onTypeFilterToggle(type) }
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                icon, null,
                                modifier = Modifier.size(16.dp).offset(y = (-0.7).dp),
                                tint     = if (selected) textColor else ColorGrisOscuro
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                label,
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color      = if (selected) textColor else ColorGrisOscuro
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Estado ────────────────────────────────────────────────────────
            SectionTitle("Estado")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    true  to Triple("Activas",   Icons.Default.PlayArrow, ColorActivo),
                    false to Triple("Inactivas", Icons.Default.Pause,     IconAlarma)
                ).forEach { (status, triple) ->
                    val (label, icon, accent) = triple
                    val selected = activeStatusFilter == status

                    Surface(
                        shape    = RoundedCornerShape(12.dp),
                        color    = if (selected) accent.copy(alpha = 0.35f) else ColorGrisFondo,
                        modifier = Modifier.weight(1f).height(46.dp).clickable { onStatusFilterChange(status) }
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                icon, null,
                                modifier = Modifier.size(16.dp).offset(y = (-0.7).dp),
                                tint     = if (selected) darkenColor(accent) else ColorGrisOscuro
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                label,
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color      = if (selected) darkenColor(accent) else ColorGrisOscuro
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Recurrencia ───────────────────────────────────────────────────
            SectionTitle("Recurrencia")
            Spacer(Modifier.height(10.dp))

            val recurrences = listOf(
                RecurrenceType.WEEKLY     to ("Semanal"         to Icons.Default.Sync),
                RecurrenceType.EVEN_WEEKS to ("Semanas pares"   to Icons.Default.DateRange),
                RecurrenceType.ODD_WEEKS  to ("Semanas impares" to Icons.Default.Event),
                RecurrenceType.DATE_RANGE to ("Rango"           to Icons.Default.CalendarMonth),
                RecurrenceType.SINGLE_DAY to ("Día único"       to Icons.Default.PushPin)
            )

            recurrences.chunked(2).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier              = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    row.forEach { (type, labelIcon) ->
                        val (label, icon) = labelIcon
                        val selected      = type in activeRecurrenceFilters

                        Surface(
                            shape    = RoundedCornerShape(12.dp),
                            color    = if (selected) colorCuarto else ColorGrisFondo,
                            modifier = Modifier.weight(1f).height(44.dp).clickable { onRecurrenceFilterToggle(type) }
                        ) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    icon, null,
                                    modifier = Modifier.size(14.dp).offset(y = (-0.7).dp),
                                    tint     = if (selected) darkenColor(colorCuarto) else ColorGrisOscuro
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    label,
                                    fontSize   = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color      = if (selected) darkenColor(colorCuarto) else ColorGrisOscuro
                                )
                            }
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Botón aplicar ─────────────────────────────────────────────────
            Button(
                onClick  = onDismiss,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Primario)
            ) {
                Text("Aplicar", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ESTADOS VACÍOS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun EmptyFilterState() {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier         = Modifier.size(56.dp).background(ColorGrisFondo, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.SearchOff, null, tint = Terciario, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text("Sin resultados", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
        Spacer(Modifier.height(4.dp))
        Text(
            "Prueba con otro nombre o cambia el filtro.",
            fontSize  = 13.sp, color = Terciario, textAlign = TextAlign.Center
        )
    }
}