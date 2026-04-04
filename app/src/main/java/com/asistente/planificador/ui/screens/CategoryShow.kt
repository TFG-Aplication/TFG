package com.asistente.planificador.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asistente.planificador.ui.screens.tools.CategoryLabel
import com.asistente.planificador.ui.screens.tools.Primario
import com.asistente.planificador.ui.viewmodels.ShowCategoriesViewModel

@Composable
fun CategoryShow(
    isVisible: Boolean,
    viewModel: ShowCategoriesViewModel = hiltViewModel(),
    onNavigateToCategory: () -> Unit,
    onNavigateToEditCategory: (String) -> Unit, // Nueva función para editar
    onBack: () -> Unit
) {
    if (!isVisible) return

    val calendars by viewModel.calendarsList.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val selectedId by viewModel.selectedCalendarId.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo oscuro (Scrim)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onBack() }
        )

        var animateTrigger by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { animateTrigger = true }

        AnimatedVisibility(
            visible = animateTrigger,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.55f),
                shape = RoundedCornerShape(topStart = 15.dp, bottomStart = 15.dp),
                color = Color.White,
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars)
                        .padding(top = 16.dp, bottom = 16.dp, start = 12.dp, end = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Categorías",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        if (calendars.isEmpty()) {
                            item {
                                Text("No hay calendarios", color = Color.LightGray, fontSize = 12.sp)
                            }
                        }

                        items(calendars, key = { it.id }) { calendar ->
                            val isExpanded = selectedId == calendar.id

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Cabecera Calendario
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isExpanded) viewModel.selectCalendar("")
                                            else viewModel.selectCalendar(calendar.id)
                                        }
                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = calendar.name,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isExpanded) Primario else Color.DarkGray,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = if (isExpanded) Primario else Color.Gray
                                    )
                                }

                                // Área de Categorías Expandible
                                AnimatedVisibility(
                                    visible = isExpanded,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    val maxHeight = 210.dp

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = maxHeight)
                                            .verticalScroll(rememberScrollState())
                                            .padding(bottom = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        if (categories.isEmpty()) {
                                            Text(
                                                "Sin categorías",
                                                fontSize = 11.sp,
                                                color = Color.LightGray,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }

                                        categories.forEach { category ->
                                            val color = category.color ?: "#BDBDBD"
                                            // Envolvemos CategoryLabel en un Box para detectar el clic de edición
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        onNavigateToEditCategory(category.id)
                                                    }
                                                    .padding(vertical = 2.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CategoryLabel(
                                                    name = category.name,
                                                    colorHex = color
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }
                                    }
                                }

                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = Color.LightGray.copy(alpha = 0.3f),
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { onNavigateToCategory() },
                        colors = ButtonDefaults.buttonColors(containerColor = Primario),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(40.dp)
                    ) {
                        Text("+ Añadir", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}