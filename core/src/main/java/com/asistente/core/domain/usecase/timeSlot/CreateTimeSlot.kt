package com.asistente.core.domain.usecase.timeslot

import com.asistente.core.domain.models.RecurrenceType
import com.asistente.core.domain.models.SlotType
import com.asistente.core.domain.models.TimeSlot
import com.asistente.core.domain.ropositories.interfaz.TimeSlotRepositoryInterface
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CreateTimeSlot @Inject constructor(
    private val repository: TimeSlotRepositoryInterface
) {
    suspend operator fun invoke(timeSlot: TimeSlot, isSharedCalendar: Boolean = false) {

        // ── Nombre ───────────────────────────────────────────────────────────
        require(timeSlot.name.isNotBlank()) {
            "El nombre de la franja no puede estar vacío"
        }
        require(timeSlot.name.trim().length >= 3) {
            "El nombre debe tener al menos 3 caracteres"
        }

        // ── Horas ────────────────────────────────────────────────────────────
        require(timeSlot.startMinuteOfDay < timeSlot.endMinuteOfDay) {
            "La hora de inicio debe ser anterior a la de fin"
        }

        // ── Días seleccionados ───────────────────────────────────────────────
        require(
            timeSlot.daysOfWeek.isNotEmpty() ||
                    timeSlot.recurrenceType == RecurrenceType.SINGLE_DAY
        ) {
            "Debes seleccionar al menos un día"
        }

        // ── Fechas de rango ──────────────────────────────────────────────────
        if (timeSlot.recurrenceType == RecurrenceType.DATE_RANGE ||
            timeSlot.recurrenceType == RecurrenceType.SINGLE_DAY
        ) {
            requireNotNull(timeSlot.rangeStart) {
                "Se requiere fecha de inicio para este tipo de recurrencia"
            }
        }
        if (timeSlot.recurrenceType == RecurrenceType.DATE_RANGE) {
            requireNotNull(timeSlot.rangeEnd) {
                "Se requiere fecha de fin para rango de fechas"
            }
            require(timeSlot.rangeEnd!!.after(timeSlot.rangeStart)) {
                "La fecha de fin debe ser posterior a la de inicio"
            }
        }

        // ── Obtener franjas existentes del mismo calendario ──────────────────
        val existing: List<TimeSlot> = repository
            .getAllTimeSlotsByCalendarId(timeSlot.parentCalendarId)
            .first()

        // ── Activa por defecto si no existe ninguna ──────────────────────────
        val slotToSave = if (!timeSlot.isActive && existing.none { it.isActive }) {
            timeSlot.copy(isActive = true)
        } else {
            timeSlot
        }

        // ── Validación de solapamiento según tipo ────────────────────────────
        val overlapping = existing.filter { overlaps(slotToSave, it) }

        if (overlapping.isNotEmpty()) {
            when (slotToSave.slotType) {
                SlotType.BLOCKED -> {
                    // Manual no puede solapar con otra manual, sí con TASK_BLOCKED
                    val conflicting = overlapping.filter { it.slotType == SlotType.BLOCKED }
                    require(conflicting.isEmpty()) {
                        val names = conflicting.joinToString { "\"${it.name}\"" }
                        "La franja se solapa con otra franja manual: $names"
                    }
                }
                SlotType.TASK_BLOCKED -> {
                    // Task no puede solapar con otra task, sí con BLOCKED
                    val conflicting = overlapping.filter { it.slotType == SlotType.TASK_BLOCKED }
                    require(conflicting.isEmpty()) {
                        val names = conflicting.joinToString { "\"${it.name}\"" }
                        "La tarea se solapa con otra tarea bloqueante: $names"
                    }
                }
            }
        }

        repository.saveTimeSlot(slotToSave, isSharedCalendar)
    }

    // ── Lógica de solapamiento ────────────────────────────────────────────────
    private fun overlaps(a: TimeSlot, b: TimeSlot): Boolean {
        val timesOverlap =
            a.startMinuteOfDay < b.endMinuteOfDay &&
                    a.endMinuteOfDay > b.startMinuteOfDay

        if (!timesOverlap) return false

        return when {
            a.recurrenceType == RecurrenceType.WEEKLY &&
                    b.recurrenceType == RecurrenceType.WEEKLY ->
                a.daysOfWeek.any { it in b.daysOfWeek }

            a.recurrenceType == RecurrenceType.DATE_RANGE &&
                    b.recurrenceType == RecurrenceType.DATE_RANGE -> {
                val rangesOverlap =
                    a.rangeStart != null && b.rangeStart != null &&
                            a.rangeEnd != null && b.rangeEnd != null &&
                            a.rangeStart.before(b.rangeEnd) &&
                            a.rangeEnd.after(b.rangeStart)
                rangesOverlap && a.daysOfWeek.any { it in b.daysOfWeek }
            }

            a.recurrenceType == RecurrenceType.SINGLE_DAY &&
                    b.recurrenceType == RecurrenceType.SINGLE_DAY ->
                a.rangeStart != null && a.rangeStart == b.rangeStart

            else -> true
        }
    }
}