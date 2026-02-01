package com.asistente.core.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.ropositories.interfaz.CalendarRepositoryInterface
import com.asistente.core.domain.usecase.calendar.CreateCalendar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val createCalendarUseCase: CreateCalendar,
    private val repository: CalendarRepositoryInterface,
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
            val currentList = repository.getAllCalendarByUserId(userId)?.firstOrNull() ?: emptyList()


            if (currentList.isEmpty()) {
                createCalendarUseCase(name = "Mi primer calendario")
            }
        }
    }


}