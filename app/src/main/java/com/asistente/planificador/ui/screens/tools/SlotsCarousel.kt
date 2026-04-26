package com.asistente.planificador.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asistente.core.domain.models.SlotType
import com.asistente.core.domain.models.TimeSlot
import com.asistente.planificador.ui.viewmodels.SlotCategoryInfo

private const val PAGE_SIZE = 5

@Composable
fun SlotsCarousel(
    filteredSlots: List<TimeSlot>,
    allSlots: List<TimeSlot>,
    onEdit: (TimeSlot) -> Unit,
    onEditTask: (taskId: String) -> Unit,
    onDelete: (TimeSlot) -> Unit,
    onToggleActive: (TimeSlot) -> Unit,
    onCardClick: (TimeSlot) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
    // Mapa taskId → info de categoría, resuelto en el VM
    categoryByTaskId: Map<String, SlotCategoryInfo?> = emptyMap()
) {
    val sortedSlots = remember(filteredSlots) {
        filteredSlots.sortedWith(compareByDescending { it.enable })
    }

    var expanded     by remember(sortedSlots) { mutableStateOf(false) }
    var visibleCount by remember(sortedSlots) { mutableStateOf(PAGE_SIZE) }

    val toShow   = if (expanded) sortedSlots else sortedSlots.take(visibleCount)
    val hasMore  = !expanded && sortedSlots.size > visibleCount
    val showLess = expanded || visibleCount > PAGE_SIZE

    Column(modifier = modifier.fillMaxWidth()) {

        // ── Cabecera ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "FRANJAS CONFIGURADAS",
                fontSize      = 11.sp, fontWeight = FontWeight.SemiBold,
                color         = Terciario, letterSpacing = 0.8.sp,
                modifier      = Modifier.weight(1f)
            )
            if (allSlots.isNotEmpty()) {
                val activeCount = allSlots.count { it.enable }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFE8F5E9)) {
                        Text(
                            "$activeCount activas",
                            fontSize = 9.sp, fontWeight = FontWeight.Bold,
                            color    = Color(0xFF43A047),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        "${filteredSlots.size}/${allSlots.size}",
                        fontSize = 10.sp, color = Terciario
                    )
                }
            }
        }

        // ── Contenido ─────────────────────────────────────────────────
        when {
            allSlots.isEmpty()      -> EmptySlotState(onAdd = onAdd)
            filteredSlots.isEmpty() -> EmptyFilterState()
            else -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    toShow.forEach { slot ->
                        val catInfo = if (slot.slotType == SlotType.TASK_BLOCKED)
                            slot.taskId?.let { categoryByTaskId[it] } else null

                        SlotCarouselCard(
                            slot           = slot,
                            onEdit         = {
                                when (slot.slotType) {
                                    SlotType.BLOCKED      -> onEdit(slot)
                                    SlotType.TASK_BLOCKED -> slot.taskId?.let { onEditTask(it) }
                                }
                            },
                            onDelete       = { onDelete(slot) },
                            onToggleActive = { onToggleActive(slot) },
                            onCardClick    = { onCardClick(slot) },
                            categoryName   = catInfo?.name,
                            categoryColor  = catInfo?.color
                        )
                    }

                    if (hasMore) {
                        MoreSlotsCard(
                            remaining = sortedSlots.size - visibleCount,
                            expanded  = false,
                            onClick   = {
                                val next = visibleCount + PAGE_SIZE
                                if (next >= sortedSlots.size) expanded = true
                                else visibleCount = next
                            }
                        )
                    }

                    if (showLess) {
                        MoreSlotsCard(
                            remaining = 0,
                            expanded  = true,
                            onClick   = { expanded = false; visibleCount = PAGE_SIZE }
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
        modifier            = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier         = Modifier.size(68.dp).background(Color(0xFFF3E5E2), CircleShape),
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
            onClick  = onAdd,
            colors   = ButtonDefaults.buttonColors(containerColor = Primario),
            shape    = RoundedCornerShape(14.dp),
            modifier = Modifier.height(46.dp)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Nueva franja", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}
