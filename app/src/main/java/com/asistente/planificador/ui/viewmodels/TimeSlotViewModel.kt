package com.asistente.planificador.ui.viewmodels

import android.graphics.Color.parseColor
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import androidx.lifecycle.viewModelScope
import com.asistente.core.domain.models.RecurrenceType
import com.asistente.core.domain.models.SlotType
import com.asistente.core.domain.models.Task
import com.asistente.core.domain.models.TimeSlot
import com.asistente.core.domain.usecase.category.GetSpecificCategory
import com.asistente.core.domain.usecase.task.GetSpecificTask
import com.asistente.core.domain.usecase.timeslot.CreateTimeSlot
import com.asistente.core.domain.usecase.timeslot.DeleteTimeSlot
import com.asistente.core.domain.usecase.timeslot.GetListTimeSlot
import com.asistente.core.domain.usecase.timeslot.UpdateTimeSlot
import com.asistente.core.domain.ropositories.interfaz.TimeSlotRepositoryInterface
import com.asistente.core.domain.usecase.timeslot.TimeSlotOverlapChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

// ── Formulario ────────────────────────────────────────────────────────────────

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
    val enable: Boolean = true,
    val error: String? = null,
    val isEditing: Boolean = false
)

// ── Detail sheet ──────────────────────────────────────────────────────────────

data class TimeSlotDetailState(
    val slot: TimeSlot,
    val overlappingSlots: List<TimeSlot> = emptyList(),
    val associatedTask: Task? = null
)

// ── Categoría resuelta para una card TASK_BLOCKED ─────────────────────────────

data class SlotCategoryInfo(
    val name: String,
    val color: Color
)

// ── Eventos one-shot ──────────────────────────────────────────────────────────

sealed class TimeSlotEvent {
    object SaveSuccess : TimeSlotEvent()
    data class SaveWithWarnings(val warnings: List<String>) : TimeSlotEvent()
    data class Error(val message: String) : TimeSlotEvent()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class TimeSlotViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val createTimeSlot: CreateTimeSlot,
    private val updateTimeSlot: UpdateTimeSlot,
    private val deleteTimeSlot: DeleteTimeSlot,
    private val getListTimeSlot: GetListTimeSlot,
    private val getSpecificTask: GetSpecificTask,
    private val getSpecificCategory: GetSpecificCategory,   // ← NUEVO
    private val timeSlotRepository: TimeSlotRepositoryInterface,
) : ViewModel() {

    private val calendarId: String = savedStateHandle["calendarId"] ?: ""

    // ── Toggle global del asistente ───────────────────────────────────────────
    private val _planningEnabled = MutableStateFlow(
        savedStateHandle["planningEnabled"] ?: true
    )
    val planningEnabled: StateFlow<Boolean> = _planningEnabled.asStateFlow()

    fun togglePlanning() {
        val new = !_planningEnabled.value
        _planningEnabled.value = new
        savedStateHandle["planningEnabled"] = new
    }

    // ── Lista de franjas ──────────────────────────────────────────────────────
    val timeSlotList: StateFlow<List<TimeSlot>> =
        getListTimeSlot(calendarId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Mapa taskId → SlotCategoryInfo (se puebla automáticamente) ───────────
    // Solo se resuelve una vez por taskId; null significa "sin categoría"
    private val _categoryByTaskId = MutableStateFlow<Map<String, SlotCategoryInfo?>>(emptyMap())
    val categoryByTaskId: StateFlow<Map<String, SlotCategoryInfo?>> = _categoryByTaskId.asStateFlow()

    init {
        // Cada vez que cambia la lista de slots, resolvemos las categorías pendientes
        viewModelScope.launch {
            timeSlotList.collect { slots ->
                val taskIds = slots
                    .filter { it.slotType == SlotType.TASK_BLOCKED && it.taskId != null }
                    .map { it.taskId!! }
                    .distinct()

                val current = _categoryByTaskId.value
                val pending = taskIds.filter { !current.containsKey(it) }

                pending.forEach { taskId ->
                    launch { resolveCategory(taskId) }
                }
            }
        }
    }

    private suspend fun resolveCategory(taskId: String) {
        val info = runCatching {
            val task = getSpecificTask(taskId) ?: return@runCatching null
            val cat  = task.categoryId?.let { getSpecificCategory(it) } ?: return@runCatching null
            val color = runCatching { Color(parseColor(cat.color)) }.getOrNull()
                ?: return@runCatching null
            SlotCategoryInfo(name = cat.name, color = color)
        }.getOrNull()

        _categoryByTaskId.update { it + (taskId to info) }
    }

    // ── Formulario ────────────────────────────────────────────────────────────
    private val _formState = MutableStateFlow(TimeSlotFormState())
    val formState: StateFlow<TimeSlotFormState> = _formState.asStateFlow()

    // ── Detail sheet ──────────────────────────────────────────────────────────
    private val _selectedSlotId = MutableStateFlow<String?>(null)

    val detailState: StateFlow<TimeSlotDetailState?> = combine(
        _selectedSlotId,
        timeSlotList
    ) { selectedId, slots ->
        selectedId to slots
    }.flatMapLatest { (selectedId, slots) ->
        if (selectedId == null) flowOf(null)
        else {
            val slot = slots.find { it.id == selectedId } ?: return@flatMapLatest flowOf(null)
            val overlapping = slots.filter { other ->
                other.id != slot.id && TimeSlotOverlapChecker.findOverlaps(slot, listOf(other)).isNotEmpty()
            }
            // Cargar tarea asociada si es TASK_BLOCKED
            flow<TimeSlotDetailState?> {
                val task = if (slot.slotType == SlotType.TASK_BLOCKED && slot.taskId != null) {
                    runCatching { getSpecificTask(slot.taskId!!) }.getOrNull()
                } else null
                emit(TimeSlotDetailState(slot = slot, overlappingSlots = overlapping, associatedTask = task))
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // ── Eventos one-shot ──────────────────────────────────────────────────────
    private val _events = MutableSharedFlow<TimeSlotEvent>()
    val events = _events.asSharedFlow()

    // ── Formulario: callbacks ─────────────────────────────────────────────────

    fun onNameChanged(name: String) =
        _formState.update { it.copy(name = name, error = null) }

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
        val date = Date(millis)
        val derivedDay: List<Int> = if (_formState.value.recurrenceType == RecurrenceType.SINGLE_DAY) {
            val cal = java.util.Calendar.getInstance().apply { time = date }
            val javaDow = cal.get(java.util.Calendar.DAY_OF_WEEK)
            val mapped = if (javaDow == java.util.Calendar.SUNDAY) 7 else javaDow - 1
            listOf(mapped)
        } else {
            _formState.value.daysOfWeek
        }
        _formState.update { it.copy(rangeStart = date, daysOfWeek = derivedDay) }

    }
    fun onRangeEndChanged(millis: Long) =
        _formState.update { it.copy(rangeEnd = Date(millis)) }

    fun loadForEdit(slotId: String) {
        viewModelScope.launch {
            val slot = timeSlotRepository.getTimeSlotById(slotId) ?: return@launch
            loadForEdit(slot)
        }
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
                enable         = timeSlot.enable,
                isEditing        = true
            )
        }
    }

    fun resetForm() { _formState.value = TimeSlotFormState() }

    // ── Guardar (crear o editar) ───────────────────────────────────────────────

    fun saveTimeSlot() {
        val state = _formState.value

        val slot = TimeSlot(
            id               = state.id,
            name             = state.name.trim(),
            parentCalendarId = calendarId,
            owners           = listOf("local_user"),
            startMinuteOfDay = state.startMinuteOfDay,
            endMinuteOfDay   = state.endMinuteOfDay,
            daysOfWeek       = state.daysOfWeek,
            recurrenceType   = state.recurrenceType,
            rangeStart       = state.rangeStart,
            rangeEnd         = state.rangeEnd,
            slotType         = state.slotType,
            enable         = state.enable
        )

        viewModelScope.launch {
            val result = if (state.isEditing) updateTimeSlot(slot) else createTimeSlot(slot)
            result.fold(
                onSuccess = { warnings ->
                    resetForm()
                    if (warnings.isEmpty()) _events.emit(TimeSlotEvent.SaveSuccess)
                    else _events.emit(TimeSlotEvent.SaveWithWarnings(warnings))
                },
                onFailure = { e ->
                    _formState.update { it.copy(error = e.message) }
                }
            )
        }
    }

    // ── Toggle activo/inactivo ────────────────────────────────────────────────

    fun toggleTimeSlotActive(timeSlotId: String) {
        viewModelScope.launch {
            runCatching {
                val slot    = timeSlotRepository.getTimeSlotById(timeSlotId) ?: return@launch
                val newSlot = slot.copy(enable = !slot.enable)
                timeSlotRepository.updateTimeSlot(newSlot)

                if (newSlot.enable) {
                    val others = timeSlotList.value.filter { it.id != slot.id && it.enable }
                    val overlaps = others.filter { other ->
                        TimeSlotOverlapChecker.findOverlaps(newSlot, listOf(other)).isNotEmpty()
                    }
                    if (overlaps.isNotEmpty()) {
                        val names = overlaps.joinToString(", ") { "\"${it.name}\"" }
                        _events.emit(
                            TimeSlotEvent.SaveWithWarnings(
                                listOf("Esta franja se solapa con: $names")
                            )
                        )
                        return@runCatching
                    }
                }
            }.onFailure { e ->
                _events.emit(TimeSlotEvent.Error("Error al actualizar: ${e.message}"))
            }
        }
    }

    // ── Activar/desactivar todas (solo BLOCKED, no TASK_BLOCKED) ─────────────────

    fun disableAllActiveSlots() {
        viewModelScope.launch {
            runCatching {
                timeSlotList.value
                    .filter { it.enable && it.slotType == SlotType.BLOCKED }
                    .forEach { timeSlotRepository.updateTimeSlot(it.copy(enable = false)) }
            }.onFailure { e ->
                _events.emit(TimeSlotEvent.Error("Error al desactivar: ${e.message}"))
            }
        }
    }

    fun enableAllInactiveSlots() {
        viewModelScope.launch {
            runCatching {
                timeSlotList.value
                    .filter { !it.enable && it.slotType == SlotType.BLOCKED }
                    .forEach { timeSlotRepository.updateTimeSlot(it.copy(enable = true)) }
            }.onFailure { e ->
                _events.emit(TimeSlotEvent.Error("Error al activar: ${e.message}"))
            }
        }
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────

    fun deleteTimeSlot(timeSlotId: String) {
        viewModelScope.launch {
            runCatching {
                deleteTimeSlot(timeSlotId, isShared = false)
            }.onFailure { e ->
                _events.emit(TimeSlotEvent.Error("Error al eliminar: ${e.message}"))
            }
        }
    }

    // ── Detail sheet ──────────────────────────────────────────────────────────

    fun openDetail(slot: TimeSlot) {
        _selectedSlotId.value = slot.id
    }

    fun closeDetail() {
        _selectedSlotId.value = null
    }
    // ── Helpers privados ──────────────────────────────────────────────────────

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