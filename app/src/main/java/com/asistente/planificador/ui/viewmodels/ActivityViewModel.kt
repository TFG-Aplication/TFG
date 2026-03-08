package com.asistente.planificador.ui.viewmodels

import android.graphics.Color.parseColor
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asistente.core.domain.models.Calendar as CalendarModel
import com.asistente.core.domain.models.Category
import com.asistente.core.domain.models.Activity
import com.asistente.core.domain.usecase.activity.CreateActivity
import com.asistente.core.domain.usecase.category.GetListCategory
import com.asistente.core.domain.usecase.calendar.GetListCalendars
import com.asistente.core.domain.usecase.category.GetSpecificCategory
import com.asistente.core.domain.usecases.GetListActivity
import com.asistente.planificador.ui.screens.colorCuarto
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
import java.util.Date
import java.util.UUID
import javax.inject.Inject

data class ActivityUiState(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val notes: String = "",
    val calendar: CalendarModel? = null,
    val category: Category? = null,
    val owners: List<String> = listOf("local_user"),
    val durationMinutes: Long = 60L,
    val earliestStart: Date = Date(),
    val deadline: Date = Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L),
    val priority: Int = 1,
    val error: String? = null
)

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val createActivityUseCase: CreateActivity,
    private val getActivitiesUseCase: GetListActivity,
    private val getCalendarsUseCase: GetListCalendars,
    private val getCategoryUseCase: GetListCategory,
    private val getSpecificCategory: GetSpecificCategory
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    private val userId = "local_user"

    val calendarsList: StateFlow<List<CalendarModel>> = (getCalendarsUseCase(userId) ?: flowOf(emptyList()))
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

    // Lista de actividades del calendario seleccionado
    val activityList: StateFlow<List<Activity>> = _uiState
        .map { it.calendar?.id }
        .distinctUntilChanged()
        .flatMapLatest { calendarId ->
            if (calendarId == null) flowOf(emptyList())
            else getActivitiesUseCase(calendarId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            calendarsList.collect { list ->
                if (list.isNotEmpty() && _uiState.value.calendar == null) {
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

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, error = null) }
    }

    fun onCalendarChanged(calendar: CalendarModel) {
        _uiState.update { it.copy(calendar = calendar) }
    }

    fun onCategoryChanged(category: Category?) {
        _uiState.update { it.copy(category = category) }
    }

    fun onNotesChanged(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun onDurationChanged(minutes: Long) {
        _uiState.update { it.copy(durationMinutes = minutes) }
    }

    fun onPriorityChanged(priority: Int) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun onEarliestStartDateChanged(millis: Long) {
        val cal = java.util.Calendar.getInstance().apply {
            val current = _uiState.value.earliestStart
            timeInMillis = millis
            set(java.util.Calendar.HOUR_OF_DAY, current.hours)
            set(java.util.Calendar.MINUTE, current.minutes)
            set(java.util.Calendar.SECOND, 0)
        }
        _uiState.update { it.copy(earliestStart = cal.time) }
        validateDates()
    }

    fun onEarliestStartTimeChanged(hour: Int, minute: Int) {
        val cal = java.util.Calendar.getInstance().apply {
            time = _uiState.value.earliestStart
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
        }
        _uiState.update { it.copy(earliestStart = cal.time) }
        validateDates()
    }

    fun onDeadlineDateChanged(millis: Long) {
        val cal = java.util.Calendar.getInstance().apply {
            val current = _uiState.value.deadline
            timeInMillis = millis
            set(java.util.Calendar.HOUR_OF_DAY, current.hours)
            set(java.util.Calendar.MINUTE, current.minutes)
            set(java.util.Calendar.SECOND, 0)
        }
        _uiState.update { it.copy(deadline = cal.time) }
        validateDates()
    }

    fun onDeadlineTimeChanged(hour: Int, minute: Int) {
        val cal = java.util.Calendar.getInstance().apply {
            time = _uiState.value.deadline
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
        }
        _uiState.update { it.copy(deadline = cal.time) }
        validateDates()
    }

    // ── Helpers de categoría/color (igual que TaskViewModel) ─────────────────

    suspend fun getCategoryColor(categoryId: String?): Color {
        val color = getSpecificCategory(categoryId ?: "")?.color
        return if (color != null) Color(parseColor(color)) else colorCuarto
    }

    // ── Validación ───────────────────────────────────────────────────────────

    private fun validateDates() {
        val state = _uiState.value
        val error = when {
            state.deadline.before(state.earliestStart) ->
                "La fecha límite no puede ser anterior al inicio"
            (state.deadline.time - state.earliestStart.time) / 60000 < state.durationMinutes ->
                "La ventana de tiempo es menor que la duración"
            else -> null
        }
        _uiState.update { it.copy(error = error) }
    }

    // ── Guardar ──────────────────────────────────────────────────────────────

    fun saveActivity() {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "El nombre es obligatorio") }
            return
        }

        val calendar = state.calendar
        if (calendar == null) {
            _uiState.update { it.copy(error = "Selecciona un calendario") }
            return
        }

        validateDates()
        if (_uiState.value.error != null) return

        viewModelScope.launch {
            try {
                createActivityUseCase(
                    Activity(
                        id = state.id,
                        name = state.name,
                        notes = state.notes.ifBlank { null },
                        parentCalendarId = calendar.id,
                        categoryId = state.category?.id,
                        owners = state.owners,
                        durationMinutes = state.durationMinutes,
                        earliest_start = state.earliestStart,
                        deadline = state.deadline,
                        priority = state.priority
                    ),
                    isSharedCalendar = calendar.isShared
                )
                // Reset manteniendo el calendario
                _uiState.value = ActivityUiState(calendar = calendar)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al guardar: ${e.message}") }
            }
        }
    }
}