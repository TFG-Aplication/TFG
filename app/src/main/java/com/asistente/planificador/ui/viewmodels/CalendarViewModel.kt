package com.asistente.core.ui.viewmodels

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.models.Task
import com.asistente.core.domain.ropositories.interfaz.CalendarRepositoryInterface
import com.asistente.core.domain.usecase.calendar.CreateCalendar
import com.asistente.core.domain.usecase.category.GetExpecificCategory
import com.asistente.core.domain.usecase.task.GetListTask
import com.asistente.planificador.ui.screens.Primario
import com.asistente.planificador.ui.screens.colorCuarto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val createCalendarUseCase: CreateCalendar,
    private val repository: CalendarRepositoryInterface,
    private val getTaskUseCase: GetListTask,
    private val getExpecificCategory: GetExpecificCategory
) : ViewModel() {

    private val userId = "local_user"

    // estado calendario seleccionado
    private val _selectedCalendar = MutableStateFlow<Calendar?>(null)
    val selectedCalendar: StateFlow<Calendar?> = _selectedCalendar.asStateFlow()

    // Observamos el calendario seleccionado y traemos sus tareas automáticamente
    val taskList: StateFlow<List<Task>> = selectedCalendar
        .flatMapLatest { c ->
            if (c == null) flowOf(emptyList())
            else getTaskUseCase(c.id) ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(3000), emptyList())

    val calendarsList: StateFlow<List<Calendar>> = repository.getAllCalendarByUserId(userId)
        ?.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        ) ?: MutableStateFlow(emptyList())

    init {
        ensureFirstCalendarExists()
        // Seleccionar automáticamente el primer calendario cuando la lista cargue
        observeCalendarsAndSelectFirst()
    }

    // cambiar el calendario desde el Dropdown/Selector
    fun onCalendarChanged(calendar: Calendar) {
        _selectedCalendar.value = calendar
    }


    //obtener color de una categoria de una task
    suspend fun  getCategoryColor(categoryId: String?): Color {
        val color = getExpecificCategory(categoryId?: "")?.color
        return if (color != null) {
            Color(android.graphics.Color.parseColor(color))
        } else {
            colorCuarto
        }
    }


    private fun observeCalendarsAndSelectFirst() {
        viewModelScope.launch {
            calendarsList.collect { list ->
                if (_selectedCalendar.value == null && list.isNotEmpty()) {
                    _selectedCalendar.value = list.first()
                }
            }
        }
    }

    // si no existe ningun calendario crear el primero
    private fun ensureFirstCalendarExists() {
        viewModelScope.launch {
            val currentList = repository.getAllCalendarByUserId(userId)?.firstOrNull() ?: emptyList()
            if (currentList.isEmpty()) {
                createCalendarUseCase(name = "Mi primer calendario")
            }
        }
    }


}