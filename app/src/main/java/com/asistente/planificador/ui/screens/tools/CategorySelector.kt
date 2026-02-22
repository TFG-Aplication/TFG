package com.asistente.planificador.ui.screens.tools


import android.graphics.Color.parseColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.ColorUtils
import com.asistente.core.domain.models.Category
import com.asistente.planificador.ui.screens.Primario
import com.asistente.planificador.ui.screens.Terciario

@Composable
fun CategoryField(
    selectedCategory: Category?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 10.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Bookmarks,
            contentDescription = null,
            tint = Primario,
            modifier = Modifier.size(26.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "Establecer categoría",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            color = Color.Black
        )

        // estado "Ninguna" (null) (lo permite la bd)
        val backgroundColor = selectedCategory?.let { Color(parseColor(it.color)) } ?: Color.Transparent
        val contentColor = selectedCategory?.let { darkenColor(backgroundColor) } ?: Terciario
        val border = if (selectedCategory == null) BorderStroke(1.dp, Terciario.copy(alpha = 0.5f)) else null

        Surface(
            color = backgroundColor,
            shape = RoundedCornerShape(8.dp),
            border = border,
            modifier = Modifier.wrapContentSize()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedCategory?.name?.uppercase() ?: "NINGUNA",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor
                )
            }
        }
    }
}

@Composable
fun CategorySelector(
    categories: List<Category>,
    onCategorySelected: (Category?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.85f).fillMaxHeight(0.6f),
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Seleccionar Categoría", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // OPCIÓN POR DEFECTO: NINGUNA
                item {
                    Surface(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Terciario.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCategorySelected(null)
                                onDismiss()
                            }
                    ) {
                        Text(
                            text = "NINGUNA",
                            modifier = Modifier.padding(vertical = 12.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Terciario
                        )
                    }
                }

                // LISTA DE CATEGORÍAS
                items(categories) { cat ->
                    val catColor = Color(parseColor(cat.color))
                    Surface(
                        color = catColor,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCategorySelected(cat)
                                onDismiss()
                            }
                    ) {
                        Text(
                            text = cat.name.uppercase(),
                            modifier = Modifier.padding(vertical = 12.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = darkenColor(catColor)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = Primario)
            }
        }
    )
}

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

@Composable
fun CategoryLabel(name: String, colorHex: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(parseColor(colorHex)),
        modifier = Modifier
            .width(100.dp)
            .height(35.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = name.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = darkenColor(Color(parseColor(colorHex)))
            )
        }
    }
}