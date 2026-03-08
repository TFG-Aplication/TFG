package com.asistente.core.domain.usecase.timeslot

import com.asistente.core.domain.models.RecurrenceType
import com.asistente.core.domain.models.TimeSlot
import com.asistente.core.domain.ropositories.interfaz.TimeSlotRepositoryInterface
import javax.inject.Inject

//Completar

class CreateTimeSlot @Inject constructor(
    private val repository: TimeSlotRepositoryInterface
) {
    suspend operator fun invoke(timeSlot: TimeSlot, isSharedCalendar: Boolean = false) {
        // Validaciones básicas
        require(timeSlot.name.isNotBlank()) { "El nombre de la franja no puede estar vacío" }
        require(timeSlot.startMinuteOfDay < timeSlot.endMinuteOfDay) {
            "La hora de inicio debe ser anterior a la de fin"
        }
        require(timeSlot.daysOfWeek.isNotEmpty() || timeSlot.recurrenceType == RecurrenceType.SINGLE_DAY) {
            "Debes seleccionar al menos un día"
        }
        if (timeSlot.recurrenceType == RecurrenceType.DATE_RANGE ||
            timeSlot.recurrenceType == RecurrenceType.SINGLE_DAY) {
            requireNotNull(timeSlot.rangeStart) { "Se requiere fecha de inicio para este tipo de recurrencia" }
        }
        if (timeSlot.recurrenceType == RecurrenceType.DATE_RANGE) {
            requireNotNull(timeSlot.rangeEnd) { "Se requiere fecha de fin para rango de fechas" }
            require(timeSlot.rangeEnd.after(timeSlot.rangeStart)) {
                "La fecha de fin debe ser posterior a la de inicio"
            }
        }

        repository.saveTimeSlot(timeSlot, isSharedCalendar)
    }
}