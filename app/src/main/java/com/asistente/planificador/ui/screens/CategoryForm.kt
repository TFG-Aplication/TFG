package com.asistente.planificador.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asistente.planificador.ui.screens.tools.CalendarField
import com.asistente.planificador.ui.screens.tools.CalendarSelector
import com.asistente.planificador.ui.viewmodels.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryForm(
    categoryId: String? = null,
    viewModel: CategoryViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val calendars by viewModel.calendarsList.collectAsStateWithLifecycle()

    val isEditMode = uiState.isEditMode

    val categoryColors = listOf(
        "#e2a9f1", "#cb6ce6", "#ffde59", "#ff914d",
        "#ff5757", "#ffa7dd", "#5ce1e6", "#ff66c4",
        "#c1ff72", "#7ed957", "#38b6ff", "#b4b4b4"
    )

    var expandedCalendarSelector by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(vertical = 10.dp).height(72.dp),
                title = {
                    Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.CenterStart) {
                        Text(
                            text = if (isEditMode) "Editar Categoría" else "Nueva Categoría",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 20.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxHeight().padding(start = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Cerrar",
                            modifier = Modifier.size(30.dp),
                            tint = Terciario
                        )
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(
                            onClick = {
                                viewModel.deleteCategory(onSuccess = onBack)
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Eliminar",
                                tint = Color.Red.copy(alpha = 0.7f),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (isEditMode) viewModel.updateCategory(onSuccess = onBack)
                            else viewModel.saveCategory(onSuccess = onBack)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primario,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 0.dp),
                        modifier = Modifier
                            .padding(end = 21.dp)
                            .height(28.dp)
                            .align(Alignment.CenterVertically)
                    ) {
                        Text(
                            text = "Guardar",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            style = LocalTextStyle.current.copy(
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Terciario,
                    navigationIconContentColor = Terciario
                ),
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(horizontal = 18.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onNameChanged(it) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Bookmarks,
                        contentDescription = null,
                        tint = Primario,
                        modifier = Modifier.size(30.dp)
                    )
                },
                placeholder = {
                    Text(
                        text = "Agregar nombre",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Medium,
                        color = Terciario.copy(alpha = 0.6f)
                    )
                },
                textStyle = LocalTextStyle.current.copy(
                    color = Terciario,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Secundario,
                    unfocusedContainerColor = Secundario,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Terciario,
                    focusedTextColor = Terciario,
                    unfocusedTextColor = Terciario
                )
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), thickness = 0.5.dp)

            CalendarField(
                selectedCalendar = uiState.calendar,
                onClick = { expandedCalendarSelector = true }
            )

            HorizontalDivider(
                modifier = Modifier.padding(top = 0.dp, bottom = 2.dp),
                thickness = 0.5.dp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(47.dp)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = Primario,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Color de categoría",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                categoryColors.chunked(6).forEach { rowColors ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        rowColors.forEach { colorItem ->
                            val isSelected = uiState.color == colorItem

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(color = Color(parseColor(colorItem)))
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) Color.Black.copy(alpha = 0.5f) else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        viewModel.onColorChanged(colorItem)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Seleccionado",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (expandedCalendarSelector) {
            CalendarSelector(
                calendars = calendars,
                onCalendarChanged = {
                    viewModel.onCalendarChanged(it)
                    expandedCalendarSelector = false
                },
                onDismiss = { expandedCalendarSelector = false },
                selectedCalendar = uiState.calendar
            )
        }

        if (uiState.error != null) {
            Snackbar { Text(uiState.error!!) }
        }
    }
}
