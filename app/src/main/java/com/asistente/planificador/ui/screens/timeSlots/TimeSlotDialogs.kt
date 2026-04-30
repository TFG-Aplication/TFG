package com.asistente.planificador.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asistente.core.domain.models.SlotType
import com.asistente.core.domain.models.TimeSlot
import com.asistente.planificador.ui.screens.tools.*
import com.asistente.planificador.ui.viewmodels.toTimeString

// ─────────────────────────────────────────────────────────────────────────────
// WARNING DIALOG
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun WarningDialog(warnings: List<String>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Color.White,
        shape            = RoundedCornerShape(20.dp),
        icon = {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFFFF8E1)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Warning, null, tint = Color(0xFFF57F17), modifier = Modifier.size(26.dp))
            }
        },
        title = {
            Text(
                "Franja guardada con avisos",
                fontWeight = FontWeight.Bold, fontSize = 17.sp, textAlign = TextAlign.Center
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "La franja se ha guardado, pero ten en cuenta lo siguiente:",
                    fontSize = 13.sp, color = Terciario, textAlign = TextAlign.Center
                )
                warnings.forEach { warning ->
                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFFFF8E1)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment     = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Warning, null,
                                tint     = Color(0xFFF57F17),
                                modifier = Modifier.size(13.dp).padding(top = 2.dp)
                            )
                            Text(warning, fontSize = 12.sp, color = Color(0xFF5D4037))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = onDismiss,
                colors   = ButtonDefaults.buttonColors(containerColor = Primario),
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Entendido", fontWeight = FontWeight.Bold) }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// CELL SLOTS PICKER SHEET
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CellSlotsPickerSheet(
    slots: List<TimeSlot>,
    onSelect: (TimeSlot) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState  = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val taskBlocked = slots.filter { it.slotType == SlotType.TASK_BLOCKED }
    val manual      = slots.filter { it.slotType == SlotType.BLOCKED }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = Color.White,
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle       = null
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding()
        ) {
            PickerSheetHeader(slotCount = slots.size)
            HorizontalDivider(color = Color(0xFFF0F0F0))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (taskBlocked.isNotEmpty()) {
                    PickerGroupSection(
                        title    = "Bloqueadas por tarea",
                        icon     = Icons.Default.CheckCircle,
                        color    = Color(0xFF2894e3),
                        slots    = taskBlocked,
                        onSelect = onSelect
                    )
                }
                if (manual.isNotEmpty()) {
                    PickerGroupSection(
                        title    = "Bloqueadas manualmente",
                        icon     = Icons.Default.Block,
                        color    = Color(0xFFFF5757),
                        slots    = manual,
                        onSelect = onSelect
                    )
                }
            }
        }
    }
}

// ── Cabecera del picker ───────────────────────────────────────────────────────

@Composable
private fun PickerSheetHeader(slotCount: Int) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Primario.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Layers, null, tint = Primario, modifier = Modifier.size(22.dp))
        }
        Column {
            Text("Varias franjas", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.Black)
            Text("$slotCount franjas en este bloque horario", fontSize = 13.sp, color = Terciario)
        }
    }
}

// ── Grupo de slots ────────────────────────────────────────────────────────────

@Composable
private fun PickerGroupSection(
    title: String,
    icon: ImageVector,
    color: Color,
    slots: List<TimeSlot>,
    onSelect: (TimeSlot) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier              = Modifier.padding(start = 2.dp)
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(13.dp))
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color, letterSpacing = 0.4.sp)
        }
        slots.forEach { slot ->
            PickerSlotCard(slot = slot, accentColor = color, onClick = { onSelect(slot) })
        }
    }
}

// ── Tarjeta de slot ───────────────────────────────────────────────────────────

@Composable
private fun PickerSlotCard(
    slot: TimeSlot,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick  = onClick,
        shape    = RoundedCornerShape(14.dp),
        color    = Color(0xFFFAFAFA),
        border   = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp).height(36.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor.copy(alpha = 0.7f))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    slot.name,
                    fontSize   = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black,
                    maxLines   = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    SlotMetaItem(Icons.Default.AccessTime, "${slot.startMinuteOfDay.toTimeString()} – ${slot.endMinuteOfDay.toTimeString()}")
                    SlotMetaItem(Icons.Default.Repeat, slot.recurrenceType.shortLabel())
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = Terciario.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun SlotMetaItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Icon(icon, null, tint = Terciario, modifier = Modifier.size(11.dp))
        Text(text, fontSize = 12.sp, color = Terciario)
    }
}