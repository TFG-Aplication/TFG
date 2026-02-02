package com.asistente.planificador.ui.screens

import SelectionDate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asistente.planificador.ui.viewmodels.TaskViewModel


/**
 * CAMBIAR -> FECHAS Y HORAS / ESTADO INDEPENDIENTE PA HECHA Y HORA -> ESTADO FECHA PA VARIABLE
 * TODO EL DIA. SOLO CON ESO DEBERIA DE PODER CREARSE UNA ACT
* */
val Primario = Color(0xFFAC5343)
val Secundario = Color(0xFFEFEFEF)
val Terciario = Color(0xFFA6A6A6)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskForm(
    viewModel: TaskViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val calendars by viewModel.calendarsList.collectAsStateWithLifecycle()
    val category by viewModel.categoryList.collectAsStateWithLifecycle()
    var showCalendarDialog by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var expandedCategorySelector by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val selectionDate = remember { SelectionDate() }

    Scaffold(
        containerColor = Color.White,

        topBar = {
            TopAppBar(
                modifier = Modifier.padding(vertical = 10.dp).height(72.dp),
                title = {
                    // Envolvemos en un Box para asegurar el centrado vertical si el texto es pequeño
                    Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.CenterStart) {
                        Text(
                            text = "Nueva Tarea",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 20.sp // Ajusta según necesites
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxHeight().padding(start = 14.dp) // Alinea al centro de la barra
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
                    Button(
                        onClick = {
                            viewModel.saveTask()
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primario,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 0.dp),
                        modifier = Modifier
                            .padding(end = 15.dp)
                            .height(28.dp)
                            .align(Alignment.CenterVertically)
                    ) {
                        Text(
                            text = "Guardar", // Como en tu imagen
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
            // Nombre
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onNameChanged(it) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = null,
                        tint = Primario,
                        modifier = Modifier.size(30.dp)

                    )
                },
                placeholder = {
                    Text(
                        text = "Agregar título",
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

            // cambiar calendario
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(47.dp) // Altura similar a un TextField
                    .padding(horizontal = 10.dp) // Alinea el inicio con el TextField de arriba
                    .clickable { showCalendarDialog = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Primario,
                    modifier = Modifier.size(30.dp)
                )

                Spacer(modifier = Modifier.width(16.dp)) // Espacio estándar entre icono y texto

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Calendario",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        lineHeight = 10.sp
                    )
                    Text(
                        text = uiState.calendar?.name ?: "Seleccionar calendario",
                        fontSize = 16.sp,
                        color = Terciario
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Terciario,
                    modifier = Modifier.size(28.dp)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(top = 0.dp, bottom = 2.dp), thickness = 0.5.dp)

            // Horas
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp) // Alineación con el resto de iconos
            ) {
                // Fila de Inicio
                // --- FILA DE INICIO ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Iconos y línea (se mantienen igual)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = Primario, modifier = Modifier.size(14.dp))
                        Box(modifier = Modifier.width(1.dp).height(20.dp).padding(vertical = 2.dp)) {
                            VerticalDivider(color = Primario.copy(alpha = 0.5f), thickness = 1.dp)
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // FECHA (Click para calendario)
                    Text(
                        text = formatDate(uiState.initDate),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showStartDatePicker = true }, // SOLO CALENDARIO
                        fontSize = 16.sp,
                        color = Color.Black
                    )

                    // HORA (Click para reloj)
                    Text(
                        text = formatTime(uiState.initDate),
                        modifier = Modifier.clickable { showStartTimePicker = true }, // SOLO RELOJ
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

            // --- FILA DE FIN ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Primario, modifier = Modifier.size(16.dp))
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // FECHA (Click para calendario)
                    Text(
                        text = formatDate(uiState.finishDate),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showEndDatePicker = true }, // SOLO CALENDARIO
                        fontSize = 16.sp,
                        color = Color.Black
                    )

                    // HORA (Click para reloj)
                    Text(
                        text = formatTime(uiState.finishDate),
                        modifier = Modifier.clickable { showEndTimePicker = true }, // SOLO RELOJ
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), thickness = 0.5.dp)

            //  SELECTOR DE CATEGORÍAS
            ExposedDropdownMenuBox(
                expanded = expandedCategorySelector,
                onExpandedChange = { expandedCategorySelector = !expandedCategorySelector }
            ) {
                OutlinedTextField(
                    value = uiState.category?.name ?: "Seleccionar Categoría",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    leadingIcon = { Icon(Icons.Default.Category, null) }, // Cambié el icono a uno de categoría
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategorySelector) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expandedCategorySelector,
                    onDismissRequest = { expandedCategorySelector = false }
                ) {
                    if (category.isEmpty()) {
                        DropdownMenuItem(text = { Text("Cargando categorías...") }, onClick = { }, enabled = false)
                    } else {
                        category.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    viewModel.onCategoryChanged(cat)
                                    expandedCategorySelector = false
                                }
                            )
                        }
                    }
                }
            }

            if (uiState.error != null) {
                Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }

        // dialogo del calendario

        if (showCalendarDialog) {
            AlertDialog(
                onDismissRequest = { showCalendarDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false), // Para que pueda ser casi pantalla completa
                modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.7f), // Tamaño superpuesto
                containerColor = Color.White,
                shape = RoundedCornerShape(28.dp),
                title = {
                    Text(
                        "Mis Calendarios",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column {
                        HorizontalDivider(thickness = 0.5.dp, color = Terciario.copy(alpha = 0.3f))
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            if (calendars.isEmpty()) {
                                item {
                                    Text("No hay calendarios disponibles",
                                        modifier = Modifier.padding(16.dp), color = Terciario)
                                }
                            } else {
                                items(calendars) { cal ->
                                    ListItem(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.onCalendarChanged(cal)
                                                showCalendarDialog = false
                                            },
                                        headlineContent = { Text(cal.name, fontWeight = FontWeight.Medium) },
                                        leadingContent = {
                                            Icon(Icons.Default.Circle, null,
                                                tint = Primario, modifier = Modifier.size(12.dp))
                                        },
                                        trailingContent = {
                                            if (uiState.calendar?.id == cal.id) {
                                                Icon(Icons.Default.Check, null, tint = Primario)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCalendarDialog = false }) {
                        Text("CERRAR", color = Primario, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Diálogos de Hora
        // --- DIÁLOGOS DE INICIO ---
        if (showStartDatePicker) {
            selectionDate.DatePickerModal(
                initialDateMillis = uiState.initDate.time,
                onDismiss = { showStartDatePicker = false },
                onConfirm = { millis ->
                    millis?.let { viewModel.onDateChanged(it, true) }
                    showStartDatePicker = false
                    showStartTimePicker = true // Salta automáticamente al reloj
                }
            )
        }

        if (showStartTimePicker) {
            val cal = java.util.Calendar.getInstance().apply { time = uiState.initDate }
            selectionDate.TimePickerModal(
                initialHour = cal.get(java.util.Calendar.HOUR_OF_DAY),
                initialMinute = cal.get(java.util.Calendar.MINUTE),
                onDismiss = { showStartTimePicker = false },
                onConfirm = { h, m ->
                    viewModel.onTimeChanged(h, m, true)
                    showStartTimePicker = false
                }
            )
        }

// --- DIÁLOGOS DE FIN ---
        if (showEndDatePicker) {
            selectionDate.DatePickerModal(
                initialDateMillis = uiState.finishDate.time, // Corregido a finishDate
                onDismiss = { showEndDatePicker = false },
                onConfirm = { millis ->
                    millis?.let { viewModel.onDateChanged(it, false) } // Corregido a false (isStart)
                    showEndDatePicker = false
                    showEndTimePicker = true // Salta automáticamente al reloj
                }
            )
        }

        if (showEndTimePicker) {
            val cal = java.util.Calendar.getInstance().apply { time = uiState.finishDate }
            selectionDate.TimePickerModal(
                initialHour = cal.get(java.util.Calendar.HOUR_OF_DAY),
                initialMinute = cal.get(java.util.Calendar.MINUTE),
                onDismiss = { showEndTimePicker = false },
                onConfirm = { h, m ->
                    viewModel.onTimeChanged(h, m, false)
                    showEndTimePicker = false
                }
            )
        }
    }
    if (uiState.error != null) {
        Text(
            text = uiState.error!!,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

fun formatTime(date: java.util.Date): String {
    val cal = java.util.Calendar.getInstance().apply { time = date }
    val hours = cal.get(java.util.Calendar.HOUR_OF_DAY)
    val minutes = cal.get(java.util.Calendar.MINUTE)
    // Formatea a HH:mm (por ejemplo: 09:05)
    return String.format("%02d:%02d", hours, minutes)
}

fun formatDate(date: java.util.Date): String {
    val sdf = java.text.SimpleDateFormat("EEE, d MMM yyyy", java.util.Locale("es", "ES"))
    return sdf.format(date).replaceFirstChar { it.uppercase() }
}