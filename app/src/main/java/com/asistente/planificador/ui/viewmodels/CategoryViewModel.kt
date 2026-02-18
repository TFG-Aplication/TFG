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
    private val savedStateHandle: SavedStateHandle,
    private val createCategory: CreateCategory,
    private val getCalendarsUseCase: GetListCalendars,
    private val getExpecificCategoryUseCase: GetSpecificCategory,
    private val deleteCategoryUseCase: DeleteCategory,
    private val updateCategoryUseCase: UpdateCategory
    ): ViewModel() {

    private val categoryId: String? = savedStateHandle["categoryId"]

    private val _uiState = MutableStateFlow(CategoryFormState())
    val uiState: StateFlow<CategoryFormState> = _uiState.asStateFlow()
    private val userId = "local_user"


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
        _uiState.update {
            val selectedColor = if (it.color == newColor) null else newColor
            it.copy(color = selectedColor)
        }
    }
    
    fun onCalendarChanged(newCalendar: CalendarModel) {
        _uiState.update { it.copy(calendar = newCalendar) }
    }

    init {
        loadCategory()
        viewModelScope.launch {
            calendarsList.collect { list ->
                if (list.isNotEmpty() && _uiState.value.calendar == null) {
                    onCalendarChanged(list.first())
                }
            }
        }
    }

    private fun loadCategory() {
        categoryId?.let { id ->
            viewModelScope.launch {
                val category = getExpecificCategoryUseCase(id)
                val calendar = calendarsList.value.find { it.id == category?.parentCalendarId }
                category?.let { category ->
                    _uiState.update { it.copy(
                        id = category.id,
                        name = category.name,
                        color = category.color,
                        calendar = calendar,
                        isEditMode = true

                    )}
                }
            }
        }
    }

    fun deleteCategory(onSuccess: () -> Unit) {
        val categoryId = _uiState.value.id
        val isShared = _uiState.value.calendar?.isShared ?: false
        viewModelScope.launch {
            try {
                deleteCategoryUseCase(categoryId, isShared)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "No se pudo eliminar: ${e.message}") }
            }
        }
    }

    fun updateCategory(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(error = "El nombre es obligatorio") }
            return
        }

        viewModelScope.launch {
            try {
                val calendar = currentState.calendar ?: throw IllegalStateException("El calendario no puede ser nulo al actualizar")
                val categoryToUpdate = com.asistente.core.domain.models.Category(
                    id = currentState.id,
                    name = currentState.name,
                    color = currentState.color ?: "#F3E5E2",
                    parentCalendarId = calendar.id
                )
                updateCategoryUseCase(categoryToUpdate.id, categoryToUpdate.name, categoryToUpdate.color)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "No se pudo actualizar: ${e.message}") }
            }
        }
    }

    fun saveCategory(onSuccess: () -> Unit) {
        val actual = _uiState.value
        if (actual.name.isBlank()) {
            _uiState.update { it.copy(error = "El nombre es obligatorio") }
            return
        }

        viewModelScope.launch {
            try {
                val actualCalendar = actual.calendar ?: throw Exception("Error al vincular calendario")
                val finalColor = _uiState.value.color ?: "#F3E5E2"
                createCategory(
                    name = actual.name,
                    color = finalColor,
                    calendarId = actualCalendar.id,
                    isSharedCalendar = actualCalendar.isShared
                )
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al guardar: ${e.message}") }
            }
        }
    }
}
