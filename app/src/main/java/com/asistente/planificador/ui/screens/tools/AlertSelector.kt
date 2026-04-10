package com.asistente.planificador.ui.screens.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
    offsetMinutes < 60    -> "${offsetMinutes}m antes"
    offsetMinutes < 1440  -> "${offsetMinutes / 60}h antes"
    offsetMinutes < 10080 -> "${offsetMinutes / 1440}d antes"
    else                  -> "${offsetMinutes / 10080}sem antes"
}

@Composable
fun AlertSelector(
    initDate        : Date,
    alerts          : List<Long>,
    onAlertsChanged : (List<Long>) -> Unit
) {
    var showPickerDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        IosRow(
            icon     = Icons.Default.NotificationsNone,
            iconTint = IconAlarma,
            label    = if (alerts.isEmpty()) "Agregar alarma" else "Alarmas"
        ) {
            IconButton(onClick = { showPickerDialog = true }) {
                Icon(
                    if (alerts.isEmpty()) Icons.Default.Add else Icons.Default.Add,
                    null,
                    tint     = Terciario,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        AnimatedVisibility(visible = alerts.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
            Column(
                modifier            = Modifier.fillMaxWidth().padding(start = 58.dp, end = 16.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                alerts.forEach { offsetMinutes ->
                    Row(
                        modifier          = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text       = formatAlertLabel(offsetMinutes),
                            fontSize   = 15.sp,
                            color      = Color.Black,
                            modifier   = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Default.Close, null,
                            tint     = Terciario,
                            modifier = Modifier.size(16.dp).clickable {
                                onAlertsChanged(alerts.filter { it != offsetMinutes })
                            }
                        )
                    }
                    if (offsetMinutes != alerts.last()) {
                        IosDivider(startPadding = 0.dp)
                    }
                }
            }
        }
    }

    if (showPickerDialog) {
        AlertPickerDialog(
            currentAlerts = alerts,
            onConfirm     = { newOffsets -> onAlertsChanged(newOffsets); showPickerDialog = false },
            onDismiss     = { showPickerDialog = false }
        )
    }
}

@Composable
private fun AlertPickerDialog(
    currentAlerts: List<Long>,
    onConfirm    : (List<Long>) -> Unit,
    onDismiss    : () -> Unit
) {
    var showCustom by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape          = RoundedCornerShape(20.dp),
            color          = Color.White,
            tonalElevation = 4.dp,
            modifier       = Modifier.fillMaxWidth().wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    "Añadir alarma",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp,
                    color      = Color.Black,
                    modifier   = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
                HorizontalDivider(thickness = 0.5.dp, color = Secundario)
                PRESET_OPTIONS.forEach { option ->
                    val alreadyAdded = currentAlerts.contains(option.minutes)
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !alreadyAdded) {
                                if (!alreadyAdded) onConfirm(currentAlerts + option.minutes)
                            }
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(option.label, fontSize = 16.sp, color = if (alreadyAdded) Terciario else Color.Black)
                        if (alreadyAdded) Text("Ya añadida", fontSize = 12.sp, color = Primario, fontWeight = FontWeight.Medium)
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Secundario)
                }
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .clickable { showCustom = true }
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Personalizar...", fontSize = 16.sp, color = Color.Black)
                    Icon(Icons.Default.ChevronRight, null, tint = Terciario, modifier = Modifier.size(18.dp))
                }
                TextButton(
                    onClick  = onDismiss,
                    modifier = Modifier.align(Alignment.End).padding(end = 16.dp, bottom = 8.dp)
                ) {
                    Text("Cancelar", color = Terciario, fontWeight = FontWeight.Medium)
                }
            }
        }
    }

    if (showCustom) {
        CustomAlertDialog(
            onConfirm = { offsetMinutes ->
                onConfirm(if (!currentAlerts.contains(offsetMinutes)) currentAlerts + offsetMinutes else currentAlerts)
                showCustom = false
            },
            onDismiss = { showCustom = false }
        )
    }
}

@Composable
private fun CustomAlertDialog(onConfirm: (Long) -> Unit, onDismiss: () -> Unit) {
    var value        by remember { mutableStateOf("30") }
    var selectedUnit by remember { mutableStateOf("Minutos") }
    var expanded     by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = Color.White, tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text("Alarma personalizada", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value         = value,
                        onValueChange = { new -> if (new.all { it.isDigit() } && new.length <= 4) value = new },
                        singleLine    = true,
                        modifier      = Modifier.weight(1f),
                        shape         = RoundedCornerShape(12.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Primario,
                            unfocusedBorderColor = Terciario.copy(alpha = 0.5f),
                            focusedTextColor     = Color.Black,
                            unfocusedTextColor   = Color.Black,
                            cursorColor          = Primario
                        )
                    )
                    Box(modifier = Modifier.weight(1.2f)) {
                        OutlinedButton(
                            onClick  = { expanded = true },
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                            border   = androidx.compose.foundation.BorderStroke(1.dp, Terciario.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(selectedUnit, fontSize = 15.sp, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(16.dp), tint = Terciario)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White)) {
                            UNIT_OPTIONS.forEach { unit ->
                                DropdownMenuItem(
                                    text    = { Text(unit, color = if (unit == selectedUnit) Primario else Color.Black) },
                                    onClick = { selectedUnit = unit; expanded = false }
                                )
                            }
                        }
                    }
                }
                val preview       = value.toLongOrNull() ?: 0L
                val offsetMinutes = unitToMinutes(preview, selectedUnit)
                Text("Notificación: ${formatAlertLabel(offsetMinutes)}", fontSize = 13.sp, color = Primario, fontWeight = FontWeight.Medium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = Terciario, fontWeight = FontWeight.Medium) }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick  = { val v = value.toLongOrNull() ?: 0L; if (v > 0) onConfirm(unitToMinutes(v, selectedUnit)) },
                        colors   = ButtonDefaults.buttonColors(containerColor = Primario),
                        shape    = RoundedCornerShape(12.dp),
                        enabled  = (value.toLongOrNull() ?: 0L) > 0
                    ) { Text("Añadir", color = Color.White, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}