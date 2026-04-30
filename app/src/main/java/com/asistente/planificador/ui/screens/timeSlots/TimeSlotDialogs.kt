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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Agrupamos por tipo para las secciones
    val taskBlocked = slots.filter { it.slotType == SlotType.TASK_BLOCKED }
    val manual = slots.filter { it.slotType == SlotType.BLOCKED }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { SheetDragHandle() } // Usamos el componente de tools
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            PickerSheetHeader(slotCount = slots.size)

            SectionDivider() // Usamos el de tools (#EEEEEE, 0.5dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (taskBlocked.isNotEmpty()) {
                    PickerGroupSection(
                        title = "Bloqueadas por tarea",
                        slots = taskBlocked,
                        onSelect = onSelect
                    )
                }

                if (manual.isNotEmpty()) {
                    PickerGroupSection(
                        title = "Bloqueadas manualmente",
                        slots = manual,
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Usamos TintedIconBox definido en tools para consistencia
        TintedIconBox(
            icon = Icons.Default.Layers,
            tint = IconNotas,
            boxSize = 40.dp,
            iconSize = 22.dp,
            cornerRadius = 10.dp
        )
        Column {
            Text(
                "Varias franjas",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
            Text(
                "$slotCount franjas en este bloque",
                fontSize = 13.sp,
                color = Terciario
            )
        }
    }
}

// ── Grupo de slots ────────────────────────────────────────────────────────────

@Composable
private fun PickerGroupSection(
    title: String,
    slots: List<TimeSlot>,
    onSelect: (TimeSlot) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(text = title) // Componente de tools

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            slots.forEach { slot ->
                PickerSlotRow(slot = slot, onClick = { onSelect(slot) })
            }
        }
    }
}

// ── Tarjeta de slot ───────────────────────────────────────────────────────────
@Composable
private fun PickerSlotRow(slot: TimeSlot, onClick: () -> Unit) {
    val dotColor = slot.slotType.dotColor()
    val icon = if (slot.slotType == SlotType.TASK_BLOCKED) Icons.Default.CheckCircle else Icons.Default.Block

    ClickableItemCard(onClick = onClick) {
        ColorAccentBar(color = dotColor) // Barra lateral de 3dp
        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Etiqueta superior (ej: TAREA BLOQUEANTE)
            InlineTypeLabel(icon, slot.slotType.label(), dotColor)

            Spacer(Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    slot.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                // Meta texto con hora y recurrencia
                MetaText(
                    "${slot.startMinuteOfDay.toTimeString()} – ${slot.endMinuteOfDay.toTimeString()} · ${slot.recurrenceType.shortLabel()}"
                )
            }
        }

        Spacer(Modifier.width(6.dp))
        Icon(
            Icons.Default.ChevronRight,
            null,
            tint = Terciario,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SlotMetaItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Icon(icon, null, tint = Terciario, modifier = Modifier.size(11.dp))
        Text(text, fontSize = 12.sp, color = Terciario)
    }
}