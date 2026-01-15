package com.asistente.planificador.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.usecase.task.CreateTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID


data class TaskFormState(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val notes: String = "",
    val place: String = "",
    val initDate: Date = Date(),
    val finishDate: Date = Date().apply { time += 3600000 },
    val calendar: Calendar? = null,
    val owners: List<String> = listOf("local_user"),
    val syncStatus: Int = 0,
    // Variables de control
    val error: String? = null
)


class TaskViewModel (
    private val createTaskUseCase: CreateTask
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskFormState())
    val uiState: StateFlow<TaskFormState> = _uiState.asStateFlow()

    fun onNameChanged(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun onNotesChanged(newNotes: String) {
        _uiState.update { it.copy(notes = newNotes) }
    }

    //guardar
    fun saveTask() {
        val actual = _uiState.value

        if (actual.name.isBlank()) {
            _uiState.update { it.copy(error = "El nombre es obligatorio") }
            return
        }

        //lanzamos corrutina para el proceso suspendido
        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }

            try {

                val actualCalenda = actual.calendar
                    ?: throw Exception("Error al vincular con el calendario")


                // LLAMADA A TU USE CASE (Invoke)
                createTaskUseCase(
                    id = actual.id,
                    name = actual.name,
                    notes = actual.notes,
                    place = actual.place,
                    init_date = actual.initDate,
                    finich_date = actual.finishDate,
                    calendar = actualCalenda,
                    owners = actual.owners
                )

            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al guardar: ${e.message}") }
            }
        }
    }
}