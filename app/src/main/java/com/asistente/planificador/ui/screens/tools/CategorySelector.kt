package com.asistente.planificador.ui.screens.tools

import android.graphics.Color.parseColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.asistente.core.domain.models.Category

// ── CategoryField ─────────────────────────────────────────────────────────────

@Composable
fun CategoryField(selectedCategory: Category?, onClick: () -> Unit) {
    val backgroundColor = selectedCategory?.let { Color(parseColor(it.color)) } ?: Color.Transparent
    val contentColor    = selectedCategory?.let { darkenColor(backgroundColor) } ?: Terciario
    val border          = if (selectedCategory == null) BorderStroke(1.dp, Terciario.copy(alpha = 0.5f)) else null

    IosRow(
        icon     = Icons.Default.Bookmarks,
        iconTint = IconCategory,
        label    = "Categoría"
    ) {
        Surface(
            color    = backgroundColor,
            shape    = RoundedCornerShape(8.dp),
            border   = border,
            modifier = Modifier.clickable { onClick() }
        ) {
            Row(
                modifier          = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text       = selectedCategory?.name?.uppercase() ?: "NINGUNA",
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color      = contentColor
                )
                Spacer(Modifier.width(3.dp))
                Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(14.dp), tint = contentColor)
            }
        }
    }
}

// ── CategorySelector ──────────────────────────────────────────────────────────

@Composable
fun CategorySelector(
    categories        : List<Category>,
    onCategorySelected: (Category?) -> Unit,
    onDismiss         : () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false),
        modifier         = Modifier.fillMaxWidth(0.85f).fillMaxHeight(0.6f),
        containerColor   = Color.White,
        shape            = RoundedCornerShape(20.dp),
        title = { Text("Seleccionar Categoría", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                item {
                    Surface(
                        color    = Color.Transparent,
                        shape    = RoundedCornerShape(12.dp),
                        border   = BorderStroke(1.dp, Terciario.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().clickable { onCategorySelected(null); onDismiss() }
                    ) {
                        Text("NINGUNA", modifier = Modifier.padding(vertical = 12.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Terciario)
                    }
                }
                items(categories) { cat ->
                    val catColor = Color(parseColor(cat.color))
                    Surface(
                        color    = catColor,
                        shape    = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().clickable { onCategorySelected(cat); onDismiss() }
                    ) {
                        Text(cat.name.uppercase(), modifier = Modifier.padding(vertical = 12.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = darkenColor(catColor))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("CANCELAR", color = Primario) } }
    )
}

@Composable
fun CategoryLabel(name: String, colorHex: String) {
    val color = Color(parseColor(colorHex))
    Surface(shape = RoundedCornerShape(8.dp), color = color, modifier = Modifier.width(100.dp).height(35.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Text(name.uppercase(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = darkenColor(color))
        }
    }
}