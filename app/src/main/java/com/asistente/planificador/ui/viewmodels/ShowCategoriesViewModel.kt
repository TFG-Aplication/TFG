package com.asistente.planificador.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.usecase.category.GetListCategory
import com.asistente.core.domain.usecase.calendar.GetListCalendars
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import androidx.lifecycle.viewModelScope
import com.asistente.core.domain.models.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class ShowCategoriesViewModel @Inject constructor (
    private val category: GetListCategory,
    private val calendars: GetListCalendars
): ViewModel() {
    private val userId = "local_user"


    // Estado para almacenar el ID del calendario seleccionado
    private val _selectedCalendarId = MutableStateFlow<String?>(null)
    val selectedCalendarId = _selectedCalendarId.asStateFlow()



    val calendarsList: StateFlow<List<Calendar>> = (calendars(userId) ?: flowOf(emptyList()))
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categories: StateFlow<List<Category>> = _selectedCalendarId
        .filterNotNull() // Solo buscamos si hay un ID seleccionado
        .flatMapLatest { calendarId ->
            category(calendarId) ?: flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectCalendar(calendarId: String) {
        _selectedCalendarId.value = calendarId
    }

}