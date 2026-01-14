package com.asistente.core.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.ropositories.`interface`.CalendarRepositoryInterface
import com.asistente.core.domain.usecase.calendar.CreateCalendar
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CalendarViewModel(
    private val createCalendarUseCase: CreateCalendar,
    private val repository: CalendarRepositoryInterface
) : ViewModel() {

    // El ID que usaremos mientras no haya login
    private val userId = "local_user"

    val calendars: StateFlow<List<Calendar>> = repository.getAllCalendarByUserId(userId)
        ?.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        ) ?: MutableStateFlow(emptyList())

    init {
        // Al inicializar el ViewModel, disparamos la lógica automática
        ensureFirstCalendarExists()
    }

    private fun ensureFirstCalendarExists() {
        viewModelScope.launch {
            // Obtenemos la lista actual (esperamos al primer valor de Room)
            val currentList = calendars.value.ifEmpty {
                repository.getAllCalendarByUserId(userId)?.firstOrNull() ?: emptyList()
            }

            if (currentList.isEmpty()) {
                createCalendarUseCase(name = "Mi primer calendario")
            }
        }
    }

    // crear nuevos calendarios manualmente
    fun createNewCalendar(name: String) {
        viewModelScope.launch {
            createCalendarUseCase(name = name)
        }
    }
}