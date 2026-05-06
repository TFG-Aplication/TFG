package com.asistente.planificador.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asistente.core.domain.usecase.calendar.GetListCalendars
import com.asistente.core.domain.usecase.category.CreateCategory
import com.asistente.core.domain.usecase.category.DeleteCategory
import com.asistente.core.domain.usecase.category.GetSpecificCategory
import com.asistente.core.domain.usecase.category.UpdateCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.asistente.core.domain.models.Calendar as CalendarModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import javax.inject.Inject


data class CategoryFormState  (
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val color: String? = null,
    val calendar: CalendarModel? = null,
    val syncStatus: Int = 0,
    val isEditMode: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val savedStateHandle        : SavedStateHandle,
    private val createCategory          : CreateCategory,
    private val getCalendarsUseCase     : GetListCalendars,
    private val getExpecificCategoryUseCase: GetSpecificCategory,
    private val deleteCategoryUseCase   : DeleteCategory,
    private val updateCategoryUseCase   : UpdateCategory
) : ViewModel() {

    // Ambos vienen automáticamente del SavedStateHandle según la ruta
    private val categoryId        : String? = savedStateHandle["categoryId"]
    private val defaultCalendarId : String? = savedStateHandle["defaultCalendarId"]

    private val _uiState = MutableStateFlow(CategoryFormState())
    val uiState: StateFlow<CategoryFormState> = _uiState.asStateFlow()
    private val userId = "local_user"

    val calendarsList: StateFlow<List<CalendarModel>> =
        (getCalendarsUseCase(userId) ?: flowOf(emptyList()))
            .stateIn(
                scope          = viewModelScope,
                started        = SharingStarted.WhileSubscribed(5000),
                initialValue   = emptyList()
            )

    init {
        viewModelScope.launch {
            // Espera a que la lista tenga datos antes de resolver calendarios
            val list = calendarsList.first { it.isNotEmpty() }
            if (categoryId != null) {
                loadCategory(list)
            } else {
                val default = list.find { it.id == defaultCalendarId } ?: list.first()
                _uiState.update { it.copy(calendar = default) }
            }
        }
    }

    private suspend fun loadCategory(list: List<CalendarModel>) {
        val category = getExpecificCategoryUseCase(categoryId!!) ?: return
        val calendar = list.find { it.id == category.parentCalendarId }
        _uiState.update {
            it.copy(
                id         = category.id,
                name       = category.name,
                color      = category.color,
                calendar   = calendar,
                isEditMode = true
            )
        }
    }

    fun onNameChanged(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun onColorChanged(newColor: String) {
        _uiState.update {
            it.copy(color = if (it.color == newColor) null else newColor)
        }
    }

    fun onCalendarChanged(newCalendar: CalendarModel) {
        _uiState.update { it.copy(calendar = newCalendar) }
    }

    fun deleteCategory(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                deleteCategoryUseCase(_uiState.value.id, _uiState.value.calendar?.isShared ?: false)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "No se pudo eliminar: ${e.message}") }
            }
        }
    }

    fun updateCategory(onSuccess: () -> Unit) {
        val s = _uiState.value
        if (s.name.isBlank()) { _uiState.update { it.copy(error = "El nombre es obligatorio") }; return }
        viewModelScope.launch {
            try {
                val cal = s.calendar ?: throw IllegalStateException("Calendario nulo al actualizar")
                updateCategoryUseCase(s.id, s.name, s.color ?: "#F3E5E2")
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "No se pudo actualizar: ${e.message}") }
            }
        }
    }

    fun saveCategory(onSuccess: () -> Unit) {
        val s = _uiState.value
        if (s.name.isBlank()) { _uiState.update { it.copy(error = "El nombre es obligatorio") }; return }
        viewModelScope.launch {
            try {
                val cal = s.calendar ?: throw Exception("Error al vincular calendario")
                createCategory(
                    name             = s.name,
                    color            = s.color ?: "#F3E5E2",
                    calendarId       = cal.id,
                    isSharedCalendar = cal.isShared
                )
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al guardar: ${e.message}") }
            }
        }
    }
}