package com.asistente.planificador.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asistente.core.domain.models.Calendar as CalendarModel
import com.asistente.core.domain.models.Category
import com.asistente.core.domain.usecase.category.GetListCategory
import com.asistente.core.domain.usecase.calendar.GetListCalendars
import com.asistente.core.domain.usecase.task.CreateTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
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


data class TaskFormState  (
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    //val notes: String = "",
    //val place: String = "",
    val initDate: Date = Date(),
    val finishDate: Date = Date().apply { time += 3600000 },
    val calendar: CalendarModel? = null,
    val owners: List<String> = listOf("local_user"),
    //val syncStatus: Int = 0,
    val error: String? = null,
    //val alerts: List<Long> = listOf(15),
    val category: Category? = null,
    //val isAllDay: Boolean = false
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val createTaskUseCase: CreateTask,
    private val getCalendarsUseCase: GetListCalendars,
    private val getCategoryUseCase: GetListCategory
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskFormState())
    val uiState: StateFlow<TaskFormState> = _uiState.asStateFlow()
    private val userId = "local_user"

    // Lista de Calendarios del usuario
    val calendarsList: StateFlow<List<CalendarModel>> = (getCalendarsUseCase(userId) ?: flowOf(emptyList()))
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Lista de Categorías reactiva al calendario seleccionado
    val categoryList: StateFlow<List<Category>> = _uiState
        .map { it.calendar?.id } // Observamos solo el ID del calendario actual
        .distinctUntilChanged()
        .flatMapLatest { calendarId ->
            if (calendarId == null) flowOf(emptyList())
            else getCategoryUseCase(calendarId) ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Autoselección inicial
        viewModelScope.launch {
            calendarsList.collect { list ->
                if (list.isNotEmpty() && _uiState.value.calendar == null) {
                    onCalendarChanged(list.first())
                }
            }
        }
        viewModelScope.launch {
            categoryList.collect { list ->
                //pa cuando cambia de calendario, si la categoria seleccionada no existe en sicho calendario
                val currentCategory = _uiState.value.category
                if (currentCategory != null && !list.contains(currentCategory)) {
                    _uiState.update { it.copy(category = null) }
                }
            }
        }
    }

    fun onCalendarChanged(newCalendar: CalendarModel) {
        _uiState.update { it.copy(calendar = newCalendar) }
    }

    fun onCategoryChanged(newCategory: Category?) {
        _uiState.update { it.copy(category = newCategory) }
    }

    fun onNameChanged(newName: String) {
        _uiState.update { it.copy(name = newName) }
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

    // ACTUALIZACIÓN DE HORA (Hora/Minuto)
    fun onTimeChanged(hour: Int, minute: Int, isStart: Boolean) {
        val calendarHelper = Calendar.getInstance()
        calendarHelper.time = if (isStart) _uiState.value.initDate else _uiState.value.finishDate

        calendarHelper.set(Calendar.HOUR_OF_DAY, hour)
        calendarHelper.set(Calendar.MINUTE, minute)

        updateAndValidateDates(calendarHelper.time, isStart)
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


    fun saveTask() {
        val actual = _uiState.value
        if (actual.name.isBlank()) {
            _uiState.update { it.copy(error = "El nombre es obligatorio") }
            return
        }

        viewModelScope.launch {
            try {
                val actualCalenda = actual.calendar ?: throw Exception("Error al vincular con el calendario")

                createTaskUseCase(
                    id = actual.id,
                    name = actual.name,
                    notes = null,
                    place = null,
                    init_date = actual.initDate,
                    finich_date = actual.finishDate,
                    calendar = actualCalenda,
                    owners = actual.owners,
                    category = actual.category,
                    alerts = null
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al guardar: ${e.message}") }
            }
        }
    }
}