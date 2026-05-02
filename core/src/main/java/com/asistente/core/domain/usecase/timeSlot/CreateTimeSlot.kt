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
    suspend operator fun invoke(
        timeSlot: TimeSlot,
        isSharedCalendar: Boolean = false
    ): Result<List<String>> {
        return try {
            // ── TASK_BLOCKED solo se crea desde CreateTask ───────────────────
            require(timeSlot.slotType != SlotType.TASK_BLOCKED || timeSlot.taskId != null) {
                "Una franja TASK_BLOCKED debe tener una tarea asociada"
            }

            // ── Nombre ───────────────────────────────────────────────────────
            require(timeSlot.name.isNotBlank()) { "El nombre no puede estar vacío" }
            require(timeSlot.name.trim().length >= 3) { "El nombre debe tener al menos 3 caracteres" }
            require(timeSlot.name.trim().length <= 20) { "El nombre debe tener menos de 20 caracteres" }

            // ── Horas ────────────────────────────────────────────────────────
            require(timeSlot.startMinuteOfDay < timeSlot.endMinuteOfDay) {
                "La hora de inicio debe ser anterior a la de fin"
            }

            // ── Días seleccionados ───────────────────────────────────────────
            require(
                timeSlot.daysOfWeek.isNotEmpty() ||
                        timeSlot.recurrenceType == RecurrenceType.SINGLE_DAY ||
                        timeSlot.recurrenceType == RecurrenceType.DATE_RANGE
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
                require(timeSlot.rangeEnd!!.after(timeSlot.rangeStart)) {
                    "La fecha de fin debe ser posterior a la de inicio"
                }
            }

            // ── Franjas existentes ───────────────────────────────────────────
            val existing = repository
                .getAllTimeSlotsByCalendarId(timeSlot.parentCalendarId)
                .first()

            // ── Activa por defecto si no hay ninguna activa ──────────────────
            val slotToSave = if (!timeSlot.enable && existing.none { it.enable }) {
                timeSlot.copy(enable = true)
            } else {
                timeSlot
            }

            val overlapping = TimeSlotOverlapChecker.findOverlaps(
                candidate = timeSlot,
                existingSlots = existing
            )

            val warnings = mutableListOf<String>()

            when (slotToSave.slotType) {

                SlotType.TASK_BLOCKED -> {
                    // Error duro: no puede solaparse con otra TASK_BLOCKED
                    val conflicting = overlapping.filter { it.slotType == SlotType.TASK_BLOCKED }
                    require(conflicting.isEmpty()) {
                        "La tarea se solapa con otra tarea bloqueante: " +
                                conflicting.joinToString { "\"${it.name}\"" }
                    }
                    // Warning: solapa con BLOCKED (TASK_BLOCKED tiene prioridad visual)
                    val withManual = overlapping.filter { it.slotType == SlotType.BLOCKED }
                    if (withManual.isNotEmpty()) {
                        warnings += "La tarea bloqueante se solapa con franjas manuales: " +
                                withManual.joinToString { "\"${it.name}\"" }
                    }
                }

                SlotType.BLOCKED -> {
                    // Solo warnings, puede solaparse con todo
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
                }
            }

            repository.saveTimeSlot(slotToSave, isSharedCalendar)
            Result.success(warnings)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}