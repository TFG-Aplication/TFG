package com.asistente.planificador.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asistente.core.domain.models.SlotType
import com.asistente.core.domain.models.TimeSlot
import com.asistente.planificador.ui.screens.tools.*

@Composable
fun PlanningToggleBanner(
    slots: List<TimeSlot>,
    onDisableAll: () -> Unit,
    onEnableAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasActive   = slots.any { it.enable && it.slotType == SlotType.BLOCKED }
    val hasInactive = slots.any { !it.enable && it.slotType == SlotType.BLOCKED }

    var confirmAction  by remember { mutableStateOf<(() -> Unit)?>(null) }
    var confirmMessage by remember { mutableStateOf("") }
    var toastMessage   by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            kotlinx.coroutines.delay(4000)
            toastMessage = null
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        color    = Color.White,
    ) {
        Column {
            BannerContent(
                hasActive   = hasActive,
                hasInactive = hasInactive,
                onDisableClick = {
                    if (hasActive) {
                        confirmMessage = "Se desactivarán todas las franjas manuales activas. El asistente dejará de tenerlas en cuenta al planificar."
                        confirmAction  = onDisableAll
                    } else toastMessage = "Todas las franjas ya están inactivas"
                },
                onEnableClick = {
                    if (hasInactive) {
                        confirmMessage = "Se activarán todas las franjas manuales inactivas. El asistente volverá a tenerlas en cuenta al planificar."
                        confirmAction  = onEnableAll
                    } else toastMessage = "Todas las franjas ya están activas"
                }
            )

            if (toastMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AppBanner(text = toastMessage.toString(), style = BannerStyle.WARNING)
                }
            }
        }
    }

    confirmAction?.let { action ->
        BannerConfirmDialog(
            message   = confirmMessage,
            onConfirm = { action(); confirmAction = null },
            onDismiss = { confirmAction = null }
        )
    }
}

// ── Contenido interno del banner ──────────────────────────────────────────────

@Composable
private fun BannerContent(
    hasActive: Boolean,
    hasInactive: Boolean,
    onDisableClick: () -> Unit,
    onEnableClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Primario, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Extension, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Asistente de planificación",
                fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Primario
            )
            Text(
                when {
                    hasActive && hasInactive -> "Estado mixto · algunas franjas manuales están inactivas"
                    hasActive                -> "Activo · todas las franjas manuales están activas"
                    else                     -> "Desactivado · todas las franjas manuales están inactivas"
                },
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium,
                color      = if (hasActive) Primario.copy(alpha = 0.8f) else Terciario.copy(alpha = 0.8f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                BannerActionChip(
                    label     = "Desactivar todas",
                    icon      = Icons.Default.RemoveCircleOutline,
                    active    = hasActive,
                    activeColor = IconAlarma,
                    onClick   = onDisableClick
                )
                BannerActionChip(
                    label     = "Activar todas",
                    icon      = Icons.Default.AddCircleOutline,
                    active    = hasInactive,
                    activeColor = ColorActivo,
                    onClick   = onEnableClick,
                    bold      = false
                )
            }
        }
    }
}

@Composable
private fun BannerActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    bold: Boolean = true
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (active) activeColor.copy(alpha = 0.35f) else ColorGrisFondo
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 11.dp, vertical = 7.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon, null,
                tint     = if (active) darkenColor(activeColor) else ColorGrisOscuro,
                modifier = Modifier.size(16.dp).offset(y = (-0.6).dp)
            )
            Text(
                label,
                fontSize   = 12.sp,
                color      = if (active) darkenColor(activeColor) else ColorGrisOscuro,
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

// ── Dialog de confirmación ────────────────────────────────────────────────────

@Composable
private fun BannerConfirmDialog(
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
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
            Text("¿Confirmar cambio?", fontWeight = FontWeight.Bold, fontSize = 17.sp, textAlign = TextAlign.Center)
        },
        text = {
            Text(message, fontSize = 13.sp, color = Terciario, textAlign = TextAlign.Center)
        },
        confirmButton = {
            Button(
                onClick  = onConfirm,
                colors   = ButtonDefaults.buttonColors(containerColor = Primario),
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Confirmar", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = ColorEdit, fontSize = 17.sp)
            }
        }
    )
}