package com.asistente.planificador.ui.screens.tools
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DeleteConfirmDialog(
    title       : String,
    message     : String = "Esta acción no se puede deshacer.",
    confirmLabel: String = "Eliminar",
    onConfirm   : () -> Unit,
    onDismiss   : () -> Unit
) {
    AlertDialog(
        onDismissRequest   = onDismiss,
        containerColor     = Color.White,
        shape              = RoundedCornerShape(16.dp),
        icon               = {
            Box(
                modifier         = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ColorDestructive.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = null,
                    tint               = ColorDestructive,
                    modifier           = Modifier.size(22.dp)
                )
            }
        },
        title = {
            Text(
                text      = title,
                fontSize  = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color.Black,
                textAlign  = TextAlign.Center
            )
        },
        text = {
            Text(
                text      = message,
                fontSize  = 14.sp,
                color     = Terciario,
                textAlign = TextAlign.Center
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = ColorEdit, fontSize = 17.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(); onDismiss() }) {
                Text(
                    confirmLabel,
                    color      = ColorDestructive,
                    fontSize   = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}