package com.asistente.planificador.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import com.asistente.core.domain.models.SlotType


// ─── Colores del proyecto ───────────────────────────────────────────────────
 val Primario  = Color(0xFFAC5343)
 val Secundario = Color(0xFFEFEFEF)
 val Terciario  = Color(0xFFA6A6A6)

val colorCuarto = Color(0xFFF3E5E2)

// Colores del diseño
val ColorGrisFondo = Color(0xFFEFEFEF)
val ColorGrisOscuro = Color(0xFF555555)

fun darkenColor(color: Color): Color {
    val hsl = FloatArray(3)
    ColorUtils.RGBToHSL(
        (color.red * 255).toInt(),
        (color.green * 255).toInt(),
        (color.blue * 255).toInt(),
        hsl
    )
    hsl[2] = (hsl[2] * 0.45f).coerceIn(0f, 1f)
    return Color(ColorUtils.HSLToColor(hsl))
}


// Colores de iconos temáticos de la app
 val IconFecha      = Color(0xFF38B6FF)
 val IconAlarma     = Color(0xFFFF914D)
 val IconRepeticion = Color(0xFF7ED957)
 val IconFranja     = Color(0xFFFF5757)
 val IconNotas      = Color(0xFFF8CEC4)


@Composable
 fun IosRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    trailingContent: (@Composable () -> Unit)? = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(iconTint.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = darkenColor(iconTint), // ← símbolo oscuro del mismo color
                modifier = Modifier.size(17.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        trailingContent?.invoke()
    }
}

// ── Extensiones SlotType ──────────────────────────────────────────────────────

fun SlotType.dotColor(): Color = when (this) {
    SlotType.BLOCKED      -> Color(0xFFFF5757)
    SlotType.TASK_BLOCKED -> Color(0xFF38B6FF)
}

fun SlotType.badgeColors(): Pair<Color, Color> = when (this) {
    SlotType.BLOCKED      -> Pair(Color(0xFFFF5757).copy(alpha = 0.5f), darkenColor(Color(0xFFFF5757)))
    SlotType.TASK_BLOCKED -> Pair(Color(0xFF38B6FF).copy(alpha = 0.5f), darkenColor(Color(0xFF38B6FF)))
}

val ColorActivo   = Color(0xFF43A047)
val ColorWarning  = Color(0xFFF57F17)
val ColorDestructive = Color(0xFFFF3B30)

val ColorEdit =Color(0xFF38B6FF)