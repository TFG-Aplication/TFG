package com.asistente.planificador.ui.screens.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.Date

// ─── Colores del proyecto ───────────────────────────────────────────────────
private val Primario  = Color(0xFFAC5343)
private val Secundario = Color(0xFFEFEFEF)
private val Terciario  = Color(0xFFA6A6A6)

// ─── Opciones predefinidas ───────────────────────────────────────────────────
private data class AlertOption(val label: String, val minutes: Long)

private val PRESET_OPTIONS = listOf(
    AlertOption("5 minutos antes",  5L),
    AlertOption("15 minutos antes", 15L),
    AlertOption("30 minutos antes", 30L),
    AlertOption("1 hora antes",     60L),
    AlertOption("1 día antes",      1440L),
)

private val UNIT_OPTIONS = listOf("Minutos", "Horas", "Días", "Semanas")

private fun unitToMinutes(value: Long, unit: String): Long = when (unit) {
    "Minutos" -> value
    "Horas"   -> value * 60L
    "Días"    -> value * 1440L
    "Semanas" -> value * 10080L
    else      -> value
}

private fun formatAlertLabel(offsetMinutes: Long): String = when {
    offsetMinutes < 60   -> "${offsetMinutes}m antes"
    offsetMinutes < 1440 -> "${offsetMinutes / 60}h antes"
    offsetMinutes < 10080 -> "${offsetMinutes / 1440}d antes"
    else                 -> "${offsetMinutes / 10080}sem antes"
}

// ─── Componente público ───────────────────────────────────────────────────────

/**
 * Muestra la fila "Agregar alarma" + chips de alertas ya añadidas.
 * Llama a [onAlertsChanged] con la lista actualizada de timestamps absolutos.
 *
 * @param initDate      Fecha de inicio de la tarea (base para calcular timestamps).
 * @param alerts        Lista actual de offsets en minutos (se convierte a timestamps al guardar).
 * @param onAlertsChanged  Callback con la nueva lista de timestamps absolutos en ms.
 */
@Composable
fun AlertSelector(
    initDate: Date,
    alerts: List<Long>,          // offsets en minutos almacenados en el estado UI
    onAlertsChanged: (List<Long>) -> Unit   // devuelve offsets en minutos
) {
    var showPickerDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {

        // ── Fila principal ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 10.dp)
                .clickable { showPickerDialog = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = null,
                tint = Primario,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = if (alerts.isEmpty()) "Agregar alarma" else "Alarmas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (alerts.isEmpty()) Icons.Default.KeyboardArrowRight else Icons.Default.Add,
                contentDescription = null,
                tint = Terciario,
                modifier = Modifier.size(24.dp)
            )
        }

        // ── Chips de alertas añadidas ────────────────────────────────────────
        AnimatedVisibility(visible = alerts.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                alerts.forEach { offsetMinutes ->
                    AlertChip(
                        label = formatAlertLabel(offsetMinutes),
                        onRemove = {
                            onAlertsChanged(alerts.filter { it != offsetMinutes })
                        }
                    )
                }
            }
        }
    }

    // ── Dialog selector ──────────────────────────────────────────────────────
    if (showPickerDialog) {
        AlertPickerDialog(
            currentAlerts = alerts,
            onConfirm = { newOffsets ->
                onAlertsChanged(newOffsets)
                showPickerDialog = false
            },
            onDismiss = { showPickerDialog = false }
        )
    }
}

// ─── Chip individual ─────────────────────────────────────────────────────────
@Composable
private fun AlertChip(label: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.Black,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Eliminar alarma",
            tint = Terciario,
            modifier = Modifier
                .size(18.dp)
                .clickable { onRemove() }
        )
    }
}

// ─── Dialog principal ─────────────────────────────────────────────────────────
@Composable
private fun AlertPickerDialog(
    currentAlerts: List<Long>,
    onConfirm: (List<Long>) -> Unit,
    onDismiss: () -> Unit
) {
    var showCustom by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {

                // Título
                Text(
                    text = "Añadir alarma",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )

                HorizontalDivider(thickness = 0.5.dp, color = Secundario)

                // Opciones preset
                PRESET_OPTIONS.forEach { option ->
                    val alreadyAdded = currentAlerts.contains(option.minutes)
                    PresetRow(
                        label = option.label,
                        alreadyAdded = alreadyAdded,
                        onClick = {
                            if (!alreadyAdded) {
                                onConfirm(currentAlerts + option.minutes)
                            }
                        }
                    )
                }

                HorizontalDivider(thickness = 0.5.dp, color = Secundario)

                // Personalizar
                PresetRow(
                    label = "Personalizar...",
                    alreadyAdded = false,
                    onClick = { showCustom = true }
                )

                // Cancelar
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 16.dp, bottom = 8.dp)
                ) {
                    Text("Cancelar", color = Terciario, fontWeight = FontWeight.Medium)
                }
            }
        }
    }

    // ── Dialog personalizar ─────────────────────────────────────────────────
    if (showCustom) {
        CustomAlertDialog(
            onConfirm = { offsetMinutes ->
                if (!currentAlerts.contains(offsetMinutes)) {
                    onConfirm(currentAlerts + offsetMinutes)
                } else {
                    onConfirm(currentAlerts)
                }
                showCustom = false
            },
            onDismiss = { showCustom = false }
        )
    }
}

// ─── Fila de opción preset ────────────────────────────────────────────────────
@Composable
private fun PresetRow(label: String, alreadyAdded: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !alreadyAdded) { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = if (alreadyAdded) Terciario else Color.Black,
            fontWeight = FontWeight.Normal
        )
        if (alreadyAdded) {
            Text(
                text = "Ya añadida",
                fontSize = 12.sp,
                color = Primario,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─── Dialog personalizar ──────────────────────────────────────────────────────
@Composable
private fun CustomAlertDialog(
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var value by remember { mutableStateOf("30") }
    var selectedUnit by remember { mutableStateOf("Minutos") }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Alarma personalizada",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )

                // Input + selector de unidad
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Número
                    OutlinedTextField(
                        value = value,
                        onValueChange = { new ->
                            if (new.all { it.isDigit() } && new.length <= 4) value = new
                        },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primario,
                            unfocusedBorderColor = Terciario.copy(alpha = 0.5f),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = Primario
                        )
                    )

                    // Dropdown unidad
                    Box(modifier = Modifier.weight(1.2f)) {
                        OutlinedButton(
                            onClick = { expanded = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, Terciario.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = selectedUnit,
                                fontSize = 15.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.Close, // placeholder arrow
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Terciario
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            UNIT_OPTIONS.forEach { unit ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            unit,
                                            color = if (unit == selectedUnit) Primario else Color.Black
                                        )
                                    },
                                    onClick = {
                                        selectedUnit = unit
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Preview del resultado
                val preview = value.toLongOrNull() ?: 0L
                val offsetMinutes = unitToMinutes(preview, selectedUnit)
                Text(
                    text = "Notificación: ${formatAlertLabel(offsetMinutes)}",
                    fontSize = 13.sp,
                    color = Primario,
                    fontWeight = FontWeight.Medium
                )

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = Terciario, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val v = value.toLongOrNull() ?: 0L
                            if (v > 0) onConfirm(unitToMinutes(v, selectedUnit))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primario),
                        shape = RoundedCornerShape(12.dp),
                        enabled = (value.toLongOrNull() ?: 0L) > 0
                    ) {
                        Text("Añadir", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
