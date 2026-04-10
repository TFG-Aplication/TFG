package com.asistente.planificador.ui.viewmodels

import android.graphics.Color.parseColor
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asistente.core.domain.models.Calendar as CalendarModel
import com.asistente.core.domain.models.Category
import com.asistente.core.domain.models.Task
import com.asistente.core.domain.usecase.category.GetListCategory
import com.asistente.core.domain.usecase.calendar.GetListCalendars
import com.asistente.core.domain.usecase.category.GetSpecificCategory
import com.asistente.core.domain.usecase.task.CreateTask
import com.asistente.core.domain.usecase.task.DeleteTask
import com.asistente.core.domain.usecase.task.GetSpecificTask
import com.asistente.core.domain.usecase.task.UpdateTask
import com.asistente.planificador.ui.screens.tools.colorCuarto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.UUID
import javax.inject.Inject


data class TaskFormState(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val notes: String = "",
    val initDate: Date = Date(),
    val finishDate: Date = Date().apply { time += 3600000 },
    val calendar: CalendarModel? = null,
    val owners: List<String> = listOf("local_user"),
    val error: String? = null,
    val alerts: List<Long> = emptyList(),
    val category: Category? = null,
    val isAllDay: Boolean = false,
    val blockTimeSlot: Boolean = false,
    val isEditMode: Boolean = false,
    val previouslyBlockedTimeSlot: Boolean = false
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val createTaskUseCase: CreateTask,
    private val updateTaskUseCase: UpdateTask,
    private val getCalendarsUseCase: GetListCalendars,
    private val getCategoryUseCase: GetListCategory,
    private val deleteTaskUseCase: DeleteTask,
    private val getExpecificTaskUseCase: GetSpecificTask,
    private val getExpecificCategory: GetSpecificCategory
) : ViewModel() {

    private val taskId: String? = savedStateHandle["taskId"]
    private val _uiState = MutableStateFlow(TaskFormState())
    val uiState: StateFlow<TaskFormState> = _uiState.asStateFlow()
    private val userId = "local_user"

    val calendarsList: StateFlow<List<CalendarModel>> =
        (getCalendarsUseCase(userId) ?: flowOf(emptyList()))
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val categoryList: StateFlow<List<Category>> = _uiState
        .map { it.calendar?.id }
        .distinctUntilChanged()
        .flatMapLatest { calendarId ->
            if (calendarId == null) flowOf(emptyList())
            else getCategoryUseCase(calendarId) ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadTaskData()
        viewModelScope.launch {
            calendarsList.collect { list ->
                // Solo autoseleccionar si NO estamos en modo edición y no hay calendario elegido
                if (list.isNotEmpty() && _uiState.value.calendar == null && !_uiState.value.isEditMode) {
                    onCalendarChanged(list.first())
                }
            }
        }
        viewModelScope.launch {
            categoryList.collect { list ->
                val currentCategory = _uiState.value.category
                if (currentCategory != null && !list.contains(currentCategory)) {
                    _uiState.update { it.copy(category = null) }
                }
            }
        }
    }

    // ── Handlers ─────────────────────────────────────────────────────────────

    fun onCalendarChanged(newCalendar: CalendarModel) {
        _uiState.update { it.copy(calendar = newCalendar) }
    }

    fun onNoteChanged(newNote: String) {
        _uiState.update { it.copy(notes = newNote) }
    }

    fun onCategoryChanged(newCategory: Category?) {
        _uiState.update { it.copy(category = newCategory) }
    }

    fun onBlockTimeSlotChanged(value: Boolean) {
        _uiState.update { it.copy(blockTimeSlot = value) }
    }

    fun onNameChanged(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun onAlertsChanged(offsets: List<Long>) {
        _uiState.update { it.copy(alerts = offsets) }
    }

    // ACTUALIZACIÓN DE FECHA (Día/Mes/Año)
    fun onDateChanged(millis: Long, isStart: Boolean) {
        val calendarHelper = Calendar.getInstance()
        val currentSelectedDate = if (isStart) _uiState.value.initDate else _uiState.value.finishDate

        // Tomamos la hora actual
        calendarHelper.time = currentSelectedDate
        val hour = calendarHelper.get(Calendar.HOUR_OF_DAY)
        val minute = calendarHelper.get(Calendar.MINUTE)

        // Aplicamos el nuevo día (millis)
        calendarHelper.timeInMillis = millis
        calendarHelper.set(Calendar.HOUR_OF_DAY, hour)
        calendarHelper.set(Calendar.MINUTE, minute)

        updateAndValidateDates(calendarHelper.time, isStart)
    }

    fun deleteTask(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                deleteTaskUseCase(
                    taskId = uiState.value.id,
                    isShared = uiState.value.calendar?.isShared?: false
                )
                onSuccess()
            } catch (e: Exception) {
                // manejar error si quieres
            }
        }
    }

    // ACTUALIZACIÓN DE HORA (Hora/Minuto)
    fun onTimeChanged(hour: Int, minute: Int, isStart: Boolean) {
        val calendarHelper = Calendar.getInstance()
        calendarHelper.time = if (isStart) _uiState.value.initDate else _uiState.value.finishDate

        calendarHelper.set(Calendar.HOUR_OF_DAY, hour)
        calendarHelper.set(Calendar.MINUTE, minute)

        updateAndValidateDates(calendarHelper.time, isStart)
    }

    fun allDay(isSelected: Boolean) {
        _uiState.update { currentState ->
            if (isSelected) {
                val calStart = java.util.Calendar.getInstance().apply {
                    time = currentState.initDate
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }
                val calEnd = java.util.Calendar.getInstance().apply {
                    time = currentState.initDate
                    set(java.util.Calendar.HOUR_OF_DAY, 23)
                    set(java.util.Calendar.MINUTE, 59)
                    set(java.util.Calendar.SECOND, 59)
                    set(java.util.Calendar.MILLISECOND, 0)
                }
                currentState.copy(
                    isAllDay = true,
                    initDate = calStart.time,
                    finishDate = calEnd.time
                )
            } else {
                currentState.copy(isAllDay = false)
            }
        }
    }
    suspend fun getExpecificTask(id: String?): Task? {
        return getExpecificTaskUseCase(id ?: "")
    }

    suspend fun getTaskCategory(categoryId: String?): Category? {
        return getExpecificCategory(categoryId ?: "")
    }

    suspend fun  getCategoryColor(categoryId: String?): Color {
        val color = getExpecificCategory(categoryId?: "")?.color
        return if (color != null) {
            Color(parseColor(color))
        } else {
            colorCuarto
        }
    }

    // ── Privados ──────────────────────────────────────────────────────────────

    private fun loadTaskData() {
        taskId?.let { id ->
            viewModelScope.launch {
                val task = getExpecificTaskUseCase(id) ?: return@launch
                val category = task.categoryId?.let { getExpecificCategory(it) }

                // Esperar a que la lista no esté vacía
                val calendar = calendarsList
                    .first { it.isNotEmpty() }
                    .find { it.id == task.parentCalendarId }


                _uiState.update {
                    it.copy(
                        id = task.id,
                        name = task.name,
                        notes = task.notes ?: "",
                        initDate = task.init_date ?: Date(),
                        finishDate = task.finish_date ?: Date(),
                        calendar = calendar,
                        owners = task.owners,
                        category = category,
                        blockTimeSlot = task.blockTimeSlot,
                        previouslyBlockedTimeSlot = task.blockTimeSlot,
                        isEditMode = true,
                        alerts = task.alerts                                    // ← añadir esto
                            ?.map { timestamp ->
                                ((task.init_date?.time ?: 0L) - timestamp) / 60_000L
                            }
                            ?.filter { it > 0 }
                            ?: emptyList()
                    )
                }
            }
        }
    }

    private fun updateAndValidateDates(newDate: Date, isStart: Boolean) {
        _uiState.update { currentState ->
            val updatedInit = if (isStart) newDate else currentState.initDate
            val updatedFinish = if (!isStart) newDate else currentState.finishDate

            //  de lógica temporal
            val error = when {
                updatedFinish.before(updatedInit) -> {
                    "La fecha/hora de fin no puede ser anterior a la de inicio"
                }

                updatedFinish.time == updatedInit.time -> {
                    "La tarea debe durar al menos un minuto"
                }

                else -> null
            }

            currentState.copy(
                initDate = updatedInit,
                finishDate = updatedFinish,
                error = error
            )
        }
    }


    fun saveTask(onSuccess: () -> Unit) {
        val actual = _uiState.value
        if (actual.name.isBlank()) {
            _uiState.update { it.copy(error = "El nombre es obligatorio") }
            return
        }

        viewModelScope.launch {
            try {
                val actualCalenda = actual.calendar ?: throw Exception("Error al vincular con el calendario")

                createTaskUseCase(
                    name = actual.name,
                    notes = actual.notes,
                    place = null,
                    initDate = actual.initDate,
                    finishDate = actual.finishDate,
                    calendarId = actualCalenda.id,
                    owners = actual.owners,
                    categoryId = actual.category?.id,
                    isSharedCalendar = actualCalenda.isShared,
                    alerts = actual.alerts.map { offsetMinutes ->
                        actual.initDate.time - (offsetMinutes * 60_000L)
                    },
                    blockTimeSlot = actual.blockTimeSlot,
                )
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al guardar: ${e.message}") }
            }
        }
    }

    fun updateTask(onSuccess: () -> Unit) {
        val actual = _uiState.value
        if (actual.name.isBlank()) {
            _uiState.update { it.copy(error = "El nombre es obligatorio") }
            return
        }

        viewModelScope.launch {
            try {
                val actualCalendar = actual.calendar
                    ?: throw IllegalStateException("El calendario no puede ser nulo al actualizar")

                val mappedAlerts = actual.alerts.map { offsetMinutes ->
                    actual.initDate.time - (offsetMinutes * 60_000L)
                }

                updateTaskUseCase(
                    id = actual.id,
                    name = actual.name,
                    notes = actual.notes,
                    place = null,
                    initDate = actual.initDate,
                    finishDate = actual.finishDate,
                    calendarId = actualCalendar.id,
                    owners = actual.owners,
                    categoryId = actual.category?.id,
                    isSharedCalendar = actualCalendar.isShared,
                    alerts = mappedAlerts,
                    blockTimeSlot = actual.blockTimeSlot,
                )
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "No se pudo actualizar: ${e.message}") }
            }
        }
    }
}