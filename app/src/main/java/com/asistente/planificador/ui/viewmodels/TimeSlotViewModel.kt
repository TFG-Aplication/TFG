package com.asistente.planificador.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asistente.core.domain.models.RecurrenceType
import com.asistente.core.domain.models.SlotType
import com.asistente.core.domain.models.TimeSlot
import com.asistente.core.domain.usecase.timeslot.CreateTimeSlot
import com.asistente.core.domain.usecase.timeslot.GetListTimeSlot
import com.asistente.core.domain.ropositories.interfaz.TimeSlotRepositoryInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

data class TimeSlotFormState(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val startMinuteOfDay: Int = 480,
    val endMinuteOfDay: Int = 840,
    val daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5),
    val recurrenceType: RecurrenceType = RecurrenceType.WEEKLY,
    val rangeStart: Date? = null,
    val rangeEnd: Date? = null,
    val slotType: SlotType = SlotType.BLOCKED,
    val isActive: Boolean = true,
    val error: String? = null,
    val isEditing: Boolean = false
)

@HiltViewModel
class TimeSlotViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val createTimeSlotUseCase: CreateTimeSlot,
    private val getListTimeSlotsUseCase: GetListTimeSlot,
    private val timeSlotRepository: TimeSlotRepositoryInterface
) : ViewModel() {

    private val calendarId: String = savedStateHandle["calendarId"] ?: ""

    // ── Toggle global del asistente para este calendario ─────────────────────
    private val _planningEnabled = MutableStateFlow<Boolean>(
        savedStateHandle["planningEnabled"] ?: true
    )
    val planningEnabled: StateFlow<Boolean> = _planningEnabled.asStateFlow()

    fun togglePlanning() {
        val new = !_planningEnabled.value
        _planningEnabled.value = new
        savedStateHandle["planningEnabled"] = new
    }

    // ── Formulario ────────────────────────────────────────────────────────────
    private val _formState = MutableStateFlow(TimeSlotFormState())
    val formState: StateFlow<TimeSlotFormState> = _formState.asStateFlow()

    val timeSlotList: StateFlow<List<TimeSlot>> =
        getListTimeSlotsUseCase(calendarId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onNameChanged(name: String) {
        _formState.update { it.copy(name = name, error = null) }
    }

    fun onStartTimeChanged(hour: Int, minute: Int) {
        _formState.update { it.copy(startMinuteOfDay = hour * 60 + minute) }
        validateTimes()
    }

    fun onEndTimeChanged(hour: Int, minute: Int) {
        _formState.update { it.copy(endMinuteOfDay = hour * 60 + minute) }
        validateTimes()
    }

    fun onDayToggled(day: Int) {
        val current = _formState.value.daysOfWeek.toMutableList()
        if (current.contains(day)) current.remove(day) else current.add(day)
        _formState.update { it.copy(daysOfWeek = current.sorted()) }
    }

    fun onRecurrenceTypeChanged(type: RecurrenceType) {
        _formState.update {
            it.copy(
                recurrenceType = type,
                rangeStart = if (type == RecurrenceType.WEEKLY ||
                    type == RecurrenceType.EVEN_WEEKS ||
                    type == RecurrenceType.ODD_WEEKS) null else it.rangeStart,
                rangeEnd = if (type != RecurrenceType.DATE_RANGE) null else it.rangeEnd
            )
        }
    }

    fun onRangeStartChanged(millis: Long) {
        _formState.update { it.copy(rangeStart = Date(millis)) }
    }

    fun onRangeEndChanged(millis: Long) {
        _formState.update { it.copy(rangeEnd = Date(millis)) }
    }

    fun loadForEdit(timeSlot: TimeSlot) {
        _formState.update {
            TimeSlotFormState(
                id               = timeSlot.id,
                name             = timeSlot.name,
                startMinuteOfDay = timeSlot.startMinuteOfDay,
                endMinuteOfDay   = timeSlot.endMinuteOfDay,
                daysOfWeek       = timeSlot.daysOfWeek,
                recurrenceType   = timeSlot.recurrenceType,
                rangeStart       = timeSlot.rangeStart,
                rangeEnd         = timeSlot.rangeEnd,
                slotType         = timeSlot.slotType,
                isActive         = timeSlot.isActive,
                isEditing        = true
            )
        }
    }

    fun resetForm() { _formState.value = TimeSlotFormState() }

    fun saveTimeSlot(onSuccess: () -> Unit) {
        val state = _formState.value
        if (state.name.isBlank()) {
            _formState.update { it.copy(error = "El nombre es obligatorio") }; return
        }
        if (state.startMinuteOfDay >= state.endMinuteOfDay) {
            _formState.update { it.copy(error = "La hora de inicio debe ser anterior a la de fin") }; return
        }
        if (state.daysOfWeek.isEmpty() && state.recurrenceType != RecurrenceType.SINGLE_DAY) {
            _formState.update { it.copy(error = "Selecciona al menos un día") }; return
        }
        if ((state.recurrenceType == RecurrenceType.DATE_RANGE ||
                    state.recurrenceType == RecurrenceType.SINGLE_DAY) && state.rangeStart == null) {
            _formState.update { it.copy(error = "Selecciona la fecha") }; return
        }
        viewModelScope.launch {
            try {
                createTimeSlotUseCase(TimeSlot(
                    id               = state.id,
                    name             = state.name,
                    parentCalendarId = calendarId,
                    owners           = listOf("local_user"),
                    startMinuteOfDay = state.startMinuteOfDay,
                    endMinuteOfDay   = state.endMinuteOfDay,
                    daysOfWeek       = state.daysOfWeek,
                    recurrenceType   = state.recurrenceType,
                    rangeStart       = state.rangeStart,
                    rangeEnd         = state.rangeEnd,
                    slotType         = state.slotType,
                    isActive         = state.isActive
                ))
                resetForm()
                onSuccess()
            } catch (e: Exception) {
                _formState.update { it.copy(error = "Error al guardar: ${e.message}") }
            }
        }
    }

    fun toggleTimeSlotActive(timeSlotId: String) {
        viewModelScope.launch {
            try {
                val slot = timeSlotRepository.getTimeSlotById(timeSlotId) ?: return@launch
                val updated = slot.copy(isActive = !slot.isActive)
                timeSlotRepository.updateTimeSlot(updated)
            } catch (e: Exception) {
                _formState.update { it.copy(error = "Error al actualizar: ${e.message}") }
            }
        }
    }

    fun deleteTimeSlot(timeSlotId: String) {
        viewModelScope.launch {
            try {
                timeSlotRepository.deleteTimeSlot(timeSlotId, isShared = false)
            } catch (e: Exception) {
                _formState.update { it.copy(error = "Error al eliminar: ${e.message}") }
            }
        }
    }

    private fun validateTimes() {
        val state = _formState.value
        val error = if (state.startMinuteOfDay >= state.endMinuteOfDay)
            "La hora de inicio debe ser anterior a la de fin" else null
        _formState.update { it.copy(error = error) }
    }
}

fun Int.toHourMinute(): Pair<Int, Int> = Pair(this / 60, this % 60)

fun Int.toTimeString(): String {
    val (h, m) = toHourMinute()
    return String.format("%02d:%02d", h, m)
}