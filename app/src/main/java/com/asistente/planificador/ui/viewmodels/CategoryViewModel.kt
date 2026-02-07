package com.asistente.planificador.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asistente.core.domain.usecase.calendar.GetListCalendars
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.asistente.core.domain.models.Calendar as CalendarModel
import com.asistente.core.domain.usecase.task.CreateCategory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import javax.inject.Inject


data class CategoryFormState  (
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val color: String = "",
    val calendar: CalendarModel? = null,
    //val syncStatus: Int = 0,
    val error: String? = null
)
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val createCategory: CreateCategory,
    private val getCalendarsUseCase: GetListCalendars

    ): ViewModel() {

    private val _uiState = MutableStateFlow(CategoryFormState())
    val uiState: StateFlow<CategoryFormState> = _uiState.asStateFlow()
    private val userId = "local_user"


    // Lista de Calendarios del usuario
    val calendarsList: StateFlow<List<CalendarModel>> = (getCalendarsUseCase(userId) ?: flowOf(emptyList()))
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    fun onNameChanged(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }
    fun onColorChanged(newColor: String) {
        _uiState.update { it.copy(color = newColor) }
    }
    fun onCalendarChanged(newCalendar: CalendarModel) {
        _uiState.update { it.copy(calendar = newCalendar) }
    }

    init {
        // Autoselección inicial
        viewModelScope.launch {
            calendarsList.collect { list ->
                if (list.isNotEmpty() && _uiState.value.calendar == null) {
                    onCalendarChanged(list.first())
                }
            }
        }
    }
    fun saveCategory() {
        val actual = _uiState.value
        if (actual.name.isBlank()) {
            _uiState.update { it.copy(error = "El nombre es obligatorio") }
            return
        }

        viewModelScope.launch {
            try {
                val actualCategori = actual.calendar ?: throw Exception("Error al vincular con el calendario")

                createCategory(
                    id = actual.id,
                    name = actual.name,
                    color = actual.color,
                    calendar = actualCategori,
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al guardar: ${e.message}") }
            }
        }
    }
}