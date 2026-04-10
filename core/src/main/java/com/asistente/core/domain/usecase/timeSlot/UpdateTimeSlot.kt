package com.asistente.core.domain.usecase.timeslot

import com.asistente.core.domain.models.RecurrenceType
import com.asistente.core.domain.models.SlotType
import com.asistente.core.domain.models.TimeSlot
import com.asistente.core.domain.ropositories.interfaz.TimeSlotRepositoryInterface
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateTimeSlot @Inject constructor(
    private val repository: TimeSlotRepositoryInterface,
) {
    suspend operator fun invoke(
        timeSlot: TimeSlot,
        isSharedCalendar: Boolean = false
    ): Result<List<String>> {
        return try {
            // ── TASK_BLOCKED se actualiza solo desde UpdateTask ──────────────
            require(timeSlot.slotType != SlotType.TASK_BLOCKED) {
                "Las franjas TASK_BLOCKED se actualizan editando la tarea asociada"
            }

            // ── Nombre ───────────────────────────────────────────────────────
            require(timeSlot.name.isNotBlank()) { "El nombre no puede estar vacío" }
            require(timeSlot.name.trim().length >= 3) { "El nombre debe tener al menos 3 caracteres" }

            // ── Horas ────────────────────────────────────────────────────────
            require(timeSlot.startMinuteOfDay < timeSlot.endMinuteOfDay) {
                "La hora de inicio debe ser anterior a la de fin"
            }

            // ── Días seleccionados ───────────────────────────────────────────
            require(
                timeSlot.daysOfWeek.isNotEmpty() ||
                        timeSlot.recurrenceType == RecurrenceType.SINGLE_DAY
            ) { "Debes seleccionar al menos un día" }

            // ── Fechas de rango ──────────────────────────────────────────────
            if (timeSlot.recurrenceType == RecurrenceType.DATE_RANGE ||
                timeSlot.recurrenceType == RecurrenceType.SINGLE_DAY
            ) {
                requireNotNull(timeSlot.rangeStart) {
                    "Se requiere fecha de inicio para este tipo de recurrencia"
                }
            }
            if (timeSlot.recurrenceType == RecurrenceType.DATE_RANGE) {
                requireNotNull(timeSlot.rangeEnd) { "Se requiere fecha de fin para rango de fechas" }
                require(timeSlot.rangeEnd!!.after(timeSlot.rangeStart) || timeSlot.rangeEnd == timeSlot.rangeStart ) {
                    "La fecha de fin debe ser posterior a la de inicio"
                }
            }

            // ── Franjas existentes excluyendo la actual ──────────────────────
            val existing = repository
                .getAllTimeSlotsByCalendarId(timeSlot.parentCalendarId)
                .first()
                .filter { it.id != timeSlot.id }

            val overlapping = TimeSlotOverlapChecker.findOverlaps(
                candidate = timeSlot,
                existingSlots = existing
            )
            val warnings = mutableListOf<String>()

            // BLOCKED puede solaparse con todo, solo warnings
            val withManual = overlapping.filter { it.slotType == SlotType.BLOCKED }
            if (withManual.isNotEmpty()) {
                warnings += "La franja se solapa con otras franjas manuales: " +
                        withManual.joinToString { "\"${it.name}\"" }
            }
            val withTaskBlocked = overlapping.filter { it.slotType == SlotType.TASK_BLOCKED }
            if (withTaskBlocked.isNotEmpty()) {
                warnings += "La franja se solapa con tareas bloqueantes: " +
                        withTaskBlocked.joinToString { "\"${it.name}\"" }
            }

            repository.updateTimeSlot(timeSlot)
            Result.success(warnings)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}