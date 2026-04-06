package com.asistente.planificador.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// ═════════════════════════════════════════════════════════════════════════════
// TIPOGRAFÍA
// ═════════════════════════════════════════════════════════════════════════════

/**
 * Título de sección — etiqueta pequeña en mayúsculas grisácea.
 * Ejemplo: "Información", "Tarea asociada", "Franjas solapadas".
 *
 * @param badge  Número o texto corto que aparece en un chip junto al título (opcional).
 */
@Composable
fun SectionTitle(text: String, badge: String? = null) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text          = text,
            fontSize      = 14.sp,
            fontWeight    = FontWeight.Bold,
            color         = Terciario,
            letterSpacing = 0.3.sp
        )
        if (badge != null) {
            Box(
                modifier         = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFEBEE)),
                contentAlignment = Alignment.Center
            ) {
                Text(badge, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Primario)
            }
        }
    }
}

/**
 * Etiqueta de categoría o tipo — chip coloreado con icono + texto en versalitas.
 * Ejemplo: "TAREA BLOQUEANTE", nombre de categoría en TaskView.
 */
@Composable
fun TypeBadge(
    label     : String,
    icon      : ImageVector,
    background: Color,
    modifier  : Modifier = Modifier
) {
    val tint = darkenColor(background)
    Surface(
        shape    = RoundedCornerShape(6.dp),
        color    = background,
        modifier = modifier.height(26.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = tint,
                modifier           = Modifier
                    .size(13.dp)
                    .offset(y = (-1).dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text       = label.uppercase(),
                fontSize   = 11.sp,
                fontWeight = FontWeight.Bold,
                color      = tint
            )
        }
    }
}

/**
 * Mini etiqueta de tipo inline (usada en filas de solapamiento, etc.).
 * Produce "TAREA BLOQUEANTE" con icono 11 dp.
 */
@Composable
fun InlineTypeLabel(icon: ImageVector, label: String, tint: Color) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(icon, null, tint = darkenColor(tint), modifier = Modifier.size(11.dp))
        Text(
            text          = label.uppercase(),
            fontSize      = 9.sp,
            fontWeight    = FontWeight.Bold,
            color         = darkenColor(tint),
            letterSpacing = 0.8.sp
        )
    }
}

/**
 * Título principal de ítem (nombre de tarea / franja).
 * fontSize 22 sp, Bold, negro.
 */
@Composable
fun ItemTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text       = text,
        fontSize   = 22.sp,
        fontWeight = FontWeight.Bold,
        color      = Color.Black,
        modifier   = modifier
    )
}

/**
 * Subtítulo de fila de detalle.
 * fontSize 16 sp, SemiBold, negro.
 */
@Composable
fun DetailValue(text: String, modifier: Modifier = Modifier) {
    Text(
        text       = text,
        fontSize   = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color      = Color.Black,
        modifier   = modifier
    )
}

/**
 * Etiqueta de campo de detalle (encima del valor).
 * fontSize 14 sp, Medium, Terciario.
 */
@Composable
fun DetailLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text       = text,
        fontSize   = 14.sp,
        fontWeight = FontWeight.Medium,
        color      = Terciario,
        modifier   = modifier
    )
}

/**
 * Texto secundario genérico (trailing en IosRow, valores "Nunca", "Sin alarma"…).
 * fontSize 16 sp, color Terciario.
 */
@Composable
fun TrailingText(text: String, modifier: Modifier = Modifier) {
    Text(text = text, fontSize = 16.sp, color = Terciario, modifier = modifier)
}

/**
 * Meta-texto de hora / recurrencia en filas compactas.
 * fontSize 12 sp, Medium, Terciario.
 */
@Composable
fun MetaText(text: String, modifier: Modifier = Modifier) {
    Text(
        text       = text,
        fontSize   = 12.sp,
        fontWeight = FontWeight.Medium,
        color      = Terciario,
        modifier   = modifier
    )
}


// ═════════════════════════════════════════════════════════════════════════════
// ICONOS CON FONDO
// ═════════════════════════════════════════════════════════════════════════════

/**
 * Icono cuadrado redondeado con fondo tintado al 18 %.
 * Tamaño configurable; por defecto 32 dp (filas de detalle).
 *
 * Uso:
 *   TintedIconBox(Icons.Default.AccessTime, IconFecha)
 *   TintedIconBox(Icons.Default.Block, IconFranja, boxSize = 44.dp, iconSize = 22.dp, cornerRadius = 11.dp)
 */
@Composable
fun TintedIconBox(
    icon        : ImageVector,
    tint        : Color,
    boxSize     : Dp = 32.dp,
    iconSize    : Dp = 17.dp,
    cornerRadius: Dp = 8.dp,
    modifier    : Modifier = Modifier
) {
    Box(
        modifier         = modifier
            .size(boxSize)
            .clip(RoundedCornerShape(cornerRadius))
            .background(tint.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = darkenColor(tint),
            modifier           = Modifier.size(iconSize)
        )
    }
}


// ═════════════════════════════════════════════════════════════════════════════
// CONTENEDORES / TARJETAS
// ═════════════════════════════════════════════════════════════════════════════

/**
 * Tarjeta de grupo al estilo iOS (fondo blanco, esquinas 12 dp).
 * Equivale al IosGroupCard de TaskView.
 */
@Composable
fun IosGroupCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White),
        content  = content
    )
}

/**
 * Divider iOS dentro de IosGroupCard (sangría izquierda 58 dp).
 */
@Composable
fun IosDivider(startPadding: Dp = 58.dp) {
    HorizontalDivider(
        modifier  = Modifier.padding(start = startPadding),
        thickness = 0.5.dp,
        color     = Color(0xFFE5E5EA)
    )
}

/**
 * Fila de detalle iOS: icono con fondo + etiqueta + contenido trailing.
 *
 * @param trailingContent  Si se pasa null, la fila no muestra trailing (solo cabecera).
 */
@Composable
fun IosRow(
    icon            : ImageVector,
    iconTint        : Color,
    label           : String,
    modifier        : Modifier = Modifier,
    trailingContent : (@Composable () -> Unit)? = { /* vacío por defecto */ }
) {
    Row(
        modifier              = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TintedIconBox(icon = icon, tint = iconTint, boxSize = 32.dp, iconSize = 17.dp)
        Text(
            text       = label,
            fontSize   = 16.sp,
            fontWeight = FontWeight.Medium,
            color      = Color.Black,
            modifier   = Modifier.weight(1f)
        )
        trailingContent?.invoke()
    }
}

/**
 * Barra lateral de color (3 dp de ancho) usada en tarjetas de tarea/franja.
 */
@Composable
fun ColorAccentBar(color: Color, height: Dp = 36.dp) {
    Box(
        modifier = Modifier
            .size(width = 3.dp, height = height)
            .background(color, RoundedCornerShape(2.dp))
    )
}

/**
 * Tarjeta de ítem clicable (fondo #F8F8F8, esquinas 10 dp).
 * Usada para tarea asociada, franjas solapadas, etc.
 * El contenido se coloca dentro del Row ya preparado.
 */
@Composable
fun ClickableItemCard(
    onClick : () -> Unit,
    modifier: Modifier = Modifier,
    content : @Composable RowScope.() -> Unit
) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF8F8F8))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        content           = content
    )
}


// ═════════════════════════════════════════════════════════════════════════════
// BANNERS DE AVISO
// ═════════════════════════════════════════════════════════════════════════════

/**
 * Banner amarillo de advertencia / información.
 *
 * @param style  [BannerStyle.WARNING] (amarillo) o [BannerStyle.INFO] (azul claro).
 */
enum class BannerStyle { WARNING, INFO }

@Composable
fun AppBanner(
    text    : String,
    style   : BannerStyle = BannerStyle.WARNING,
    modifier: Modifier    = Modifier
) {
    val (bg, iconTint, textColor, icon) = when (style) {
        BannerStyle.WARNING -> BannerTokens(
            bg        = Color(0xFFFFF8E1),
            iconTint  = Color(0xFFF57F17),
            textColor = Color(0xFF5D4037),
            icon      = Icons.Default.Warning
        )
        BannerStyle.INFO -> BannerTokens(
            bg        = Color(0xFFE3F2FD),
            iconTint  = Color(0xFF1565C0),
            textColor = Color(0xFF0D47A1),
            icon      = Icons.Default.Info
        )
    }

    Surface(
        shape    = RoundedCornerShape(10.dp),
        color    = bg,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon, null,
                tint     = iconTint,
                modifier = Modifier.size(16.dp).padding(top = 1.dp)
            )
            Text(
                text       = text,
                fontSize   = 14.sp,
                color      = textColor,
                lineHeight = 20.sp
            )
        }
    }
}

// Data class interna para desempaquetar los tokens de banner
private data class BannerTokens(
    val bg       : Color,
    val iconTint : Color,
    val textColor: Color,
    val icon     : ImageVector
)

/**
 * Chip de contador de advertencia (ej. "⚠ 3" junto a "Franjas solapadas").
 */
@Composable
fun WarningCountChip(count: Int) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFFFF8E1)) {
        Row(
            modifier              = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                Icons.Default.Warning, null,
                tint     = ColorWarning,
                modifier = Modifier.size(11.dp)
            )
            Text(
                "$count",
                fontSize   = 10.sp,
                fontWeight = FontWeight.Bold,
                color      = ColorWarning
            )
        }
    }
}


// ═════════════════════════════════════════════════════════════════════════════
// BOTÓN DESTRUCTIVO Y EDICIÓN
// ═════════════════════════════════════════════════════════════════════════════

/**
 * Botón de texto rojo "Eliminar …" que se coloca en el pie de pantallas y sheets.
 *
 * Envuelve el llamador en un Box con navigationBarsPadding y fondo blanco,
 * igual que en TaskView y TimeSlotDetailSheet.
 */
@Composable
fun DestructiveFooterButton(label: String, onClick: () -> Unit) {
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        TextButton(
            onClick  = onClick,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(
                text       = label,
                color      = ColorDestructive,
                fontSize   = 17.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

/**
 * Botón "Editar" de navegación — esquina superior derecha de TopBar o cabecera de sheet.
 * Texto azul [AppColors.Role.edit], sin padding extra ni contenedor.
 */
@Composable
fun EditActionButton(
    label          : String  = "Editar",
    onClick        : () -> Unit,
    contentPadding : PaddingValues = PaddingValues(horizontal = 4.dp)
) {
    TextButton(
        onClick        = onClick,
        contentPadding = contentPadding
    ) {
        Text(
            text       = label,
            color      = ColorEdit,
            fontSize   = 17.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// SEPARADORES DE SECCIÓN
// ═════════════════════════════════════════════════════════════════════════════

/**
 * Divisor fino entre secciones de un sheet o pantalla (0.5 dp, #EEEEEE).
 */
@Composable
fun SectionDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE), modifier = modifier)
}

/**
 * Bloque estándar de separación + divider + separación entre secciones de contenido.
 * Evita repetir siempre el triplete Spacer / Divider / Spacer.
 */
@Composable
fun SectionSpacer(top: Dp = 20.dp, bottom: Dp = 20.dp) {
    Spacer(Modifier.height(top))
    SectionDivider()
    Spacer(Modifier.height(bottom))
}


// ═════════════════════════════════════════════════════════════════════════════
// DRAG HANDLE (bottom sheets)
// ═════════════════════════════════════════════════════════════════════════════

/**
 * Pastilla gris centrada que indica que el sheet es arrastrable.
 */
@Composable
fun SheetDragHandle() {
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFDDDDDD))
        )
    }
}