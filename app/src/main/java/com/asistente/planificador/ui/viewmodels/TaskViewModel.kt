package com.asistente.planificador.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.usecase.calendar.GetListCalendars
import com.asistente.core.domain.usecase.task.CreateTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    val calendar: Calendar? = null,
    val owners: List<String> = listOf("local_user"),
    //val syncStatus: Int = 0,
    val error: String? = null,
    //val alerts: List<Long> = listOf(15),
    //val category: Category = Category(),
    //val isAllDay: Boolean = false
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val createTaskUseCase: CreateTask,
    private val getCalendarsUseCase: GetListCalendars // Cambié el nombre a uno más claro
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskFormState())
    val uiState: StateFlow<TaskFormState> = _uiState.asStateFlow()
    private val userId = "local_user"

    // lista calendarios
    val calendarsList: StateFlow<List<Calendar>> = (getCalendarsUseCase(userId) ?: flowOf(emptyList()))
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            calendarsList.collect { list ->
                if (list.isNotEmpty() && _uiState.value.calendar == null) {
                    onCalendarChanged(list.first())
                }
            }
        }
    }



    fun onCalendarChanged(newCalendar: Calendar) {
        _uiState.update { it.copy(calendar = newCalendar) }
    }

    // cambio nombre tarea
    fun onNameChanged(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }


    // cambio hora
    fun onTimeChanged(hour: Int, minute: Int, isInit: Boolean) {
        _uiState.update { state ->
            val calendar = java.util.Calendar.getInstance()
            calendar.time = if (isInit) state.initDate else state.finishDate
            calendar.set(java.util.Calendar.HOUR_OF_DAY, hour)
            calendar.set(java.util.Calendar.MINUTE, minute)

            if (isInit) state.copy(initDate = calendar.time)
            else state.copy(finishDate = calendar.time)
        }
    }

    //guardar
    fun saveTask() {
        val actual = _uiState.value
        android.util.Log.d("TaskViewModel", "Guardando tarea: $actual")


        if (actual.name.isBlank()) {
            _uiState.update { it.copy(error = "El nombre es obligatorio") }
            return
        }

        //lanzamos corrutina para el proceso suspendido
        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }

            try {

                val actualCalenda = actual.calendar
                    ?: throw Exception("Error al vincular con el calendario")


                // LLAMADA A TU USE CASE (Invoke)
                createTaskUseCase(
                    id = actual.id,
                    name = actual.name,
                    notes = null,
                    place = null,
                    init_date = actual.initDate,
                    finich_date = actual.finishDate,
                    calendar = actualCalenda,
                    owners = actual.owners,
                    category = null,
                    alerts = null
                )

            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al guardar: ${e.message}") }
            }
        }
    }
}