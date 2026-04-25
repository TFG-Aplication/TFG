package com.asistente.planificador.ui.screens

import CalendarField
import CalendarSelector
import android.graphics.Color.parseColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asistente.planificador.ui.screens.tools.*
import com.asistente.planificador.ui.viewmodels.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryForm(
    categoryId: String? = null,
    viewModel  : CategoryViewModel = hiltViewModel(),
    onBack     : () -> Unit
) {
    val uiState   by viewModel.uiState.collectAsState()
    val calendars by viewModel.calendarsList.collectAsStateWithLifecycle()

    val isEditMode = uiState.isEditMode

    val categoryColors = listOf(
        "#e2a9f1", "#cb6ce6", "#ffde59", "#ff914d",
        "#ff5757", "#ffa7dd", "#5ce1e6", "#ff66c4",
        "#c1ff72", "#7ed957", "#38b6ff", "#b4b4b4"
    )

    var expandedCalendarSelector by remember { mutableStateOf(false) }
    var showDeleteConfirm        by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Secundario,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
            ) {
                // ── Nav bar ───────────────────────────────────────────────────
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.Close, null, tint = Terciario, modifier = Modifier.size(22.dp))
                    }

                    Text(
                        text       = if (isEditMode) "Editar categoría" else "Nueva categoría",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 17.sp,
                        color      = Color.Black,
                        modifier   = Modifier.weight(1f),
                        textAlign  = TextAlign.Center
                    )

                    if (isEditMode) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                Icons.Rounded.Delete, null,
                                tint     = ColorDestructive.copy(alpha = 0.7f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        Spacer(Modifier.size(48.dp))
                    }

                    TextButton(
                        onClick        = {
                            if (isEditMode) viewModel.updateCategory(onSuccess = onBack)
                            else viewModel.saveCategory(onSuccess = onBack)
                        },
                        contentPadding = PaddingValues(end = 12.dp)
                    ) {
                        Text(
                            "Guardar",
                            color      = Primario,
                            fontSize   = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // ── Cabecera expandida ────────────────────────────────────────
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value         = uiState.name,
                        onValueChange = { viewModel.onNameChanged(it) },
                        placeholder   = {
                            Text(
                                "Nombre de la categoría",
                                fontSize   = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Terciario.copy(alpha = 0.5f)
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.Black
                        ),
                        modifier   = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors     = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor   = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor      = Color.Transparent,
                            unfocusedBorderColor    = Color.Transparent,
                            cursorColor             = Primario
                        )
                    )
                    if (uiState.calendar != null) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, null, tint = Terciario, modifier = Modifier.size(12.dp))
                            Text(uiState.calendar!!.name, fontSize = 12.sp, color = Terciario)
                        }
                    }
                }
            }
        }
    ) { pad ->
        Column(
            modifier            = Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Error ─────────────────────────────────────────────────────────
            if (uiState.error != null) {
                AppBanner(text = uiState.error!!, style = BannerStyle.WARNING)
            }

            // ── Calendario ────────────────────────────────────────────────────
            IosGroupCard {
                CalendarField(
                    selectedCalendar = uiState.calendar,
                    onClick          = { expandedCalendarSelector = true }
                )
            }

            // ── Color ─────────────────────────────────────────────────────────
            IosGroupCard {
                IosRow(
                    icon            = Icons.Default.Palette,
                    iconTint        = IconAlarma,
                    label           = "Color de categoría",
                    trailingContent = null
                )
                IosDivider()
                Column(
                    modifier            = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    categoryColors.chunked(6).forEach { rowColors ->
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowColors.forEach { colorItem ->
                                val isSelected = uiState.color == colorItem
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(parseColor(colorItem)))
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) Color.Black.copy(alpha = 0.5f) else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { viewModel.onColorChanged(colorItem) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // ── Selectores ────────────────────────────────────────────────────────
        if (expandedCalendarSelector) {
            CalendarSelector(
                calendars         = calendars,
                onCalendarChanged = { viewModel.onCalendarChanged(it); expandedCalendarSelector = false },
                onDismiss         = { expandedCalendarSelector = false },
                selectedCalendar  = uiState.calendar
            )
        }
        if (showDeleteConfirm) {
            DeleteConfirmDialog(
                title     = "¿Eliminar categoría?",
                onConfirm = { viewModel.deleteCategory(onSuccess = onBack) },
                onDismiss = { showDeleteConfirm = false }
            )
        }
    }
}