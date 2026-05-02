package com.asistente.core.domain.usecase.timeslot

import com.asistente.core.domain.models.RecurrenceType
import com.asistente.core.domain.models.TimeSlot
import java.util.Calendar
import java.util.Date

/**
 * Calcula qué franjas existentes se solapan con una nueva franja candidata.
 *
 * Lógica general:
 *  1. Descartar franjas inactivas.
 *  2. Para cada franja existente, obtener todas las "ventanas de tiempo concretas"
 *     que genera dentro del periodo de la nueva franja.
 *  3. Comprobar si alguna de esas ventanas se solapa con la ventana candidata.
 */
object TimeSlotOverlapChecker {

    /**
     * @param candidate     La nueva franja que se quiere crear.
     * @param existingSlots Todas las franjas ya existentes en BD para el mismo calendario.
     * @return Lista de franjas que se solapan con [candidate].
     */
    fun findOverlaps(
        candidate: TimeSlot,
        existingSlots: List<TimeSlot>
    ): List<TimeSlot> {
        val candidateWindows = resolveWindows(candidate)
        if (candidateWindows.isEmpty()) return emptyList()

        return existingSlots.filter { existing ->
            if (!existing.enable) return@filter false
            val existingWindows = resolveWindows(existing)
            existingWindows.any { eWin ->
                candidateWindows.any { cWin -> windowsOverlap(cWin, eWin) }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Ventana de tiempo concreta (instante absoluto inicio–fin)
    // ─────────────────────────────────────────────────────────────

    private data class TimeWindow(val startMs: Long, val endMs: Long)

    private fun windowsOverlap(a: TimeWindow, b: TimeWindow): Boolean =
        a.startMs < b.endMs && b.startMs < a.endMs   // solapamiento estricto

    // ─────────────────────────────────────────────────────────────
    // Conversión DAY_OF_WEEK del Calendar (1=Dom…7=Sáb)
    // al sistema de la app                (1=Lun…7=Dom)
    // ─────────────────────────────────────────────────────────────

    private fun calDowToIso(calDow: Int): Int =
        if (calDow == Calendar.SUNDAY) 7 else calDow - 1

    // ─────────────────────────────────────────────────────────────
    // Resolución de ventanas según RecurrenceType
    // ─────────────────────────────────────────────────────────────

    private fun resolveWindows(slot: TimeSlot): List<TimeWindow> {
        return when (slot.recurrenceType) {
            RecurrenceType.SINGLE_DAY -> resolveSingleDay(slot)
            RecurrenceType.DATE_RANGE -> resolveDateRange(slot)
            RecurrenceType.TASK_RANGE -> resolveTaskRange(slot)
            RecurrenceType.WEEKLY     -> resolveRepeating(slot, weekFilter = null)
            RecurrenceType.EVEN_WEEKS -> resolveRepeating(slot, weekFilter = WeekParity.EVEN)
            RecurrenceType.ODD_WEEKS  -> resolveRepeating(slot, weekFilter = WeekParity.ODD)
        }
    }

    // ── SINGLE_DAY ──────────────────────────────────────────────

    private fun resolveSingleDay(slot: TimeSlot): List<TimeWindow> {
        val day = slot.rangeStart ?: return emptyList()
        return listOf(buildWindow(day, slot.startMinuteOfDay, slot.endMinuteOfDay))
    }

    // ── DATE_RANGE ───────────────────────────────────────────────

    /**
     * Itera día a día dentro del rango e incluye los días cuyo
     * dayOfWeek (1=Lun…7=Dom) esté en slot.daysOfWeek (si la lista
     * está vacía se incluyen todos los días).
     */
    private fun resolveDateRange(slot: TimeSlot): List<TimeWindow> {
        val start = slot.rangeStart ?: return emptyList()
        val end   = slot.rangeEnd   ?: return emptyList()

        val windows = mutableListOf<TimeWindow>()
        val cal = Calendar.getInstance().apply { time = startOfDay(start) }
        val endDay = startOfDay(end).time

        while (cal.timeInMillis <= endDay) {
            val dow = calDowToIso(cal.get(Calendar.DAY_OF_WEEK))
            if (slot.daysOfWeek.isEmpty() || dow in slot.daysOfWeek) {
                windows.add(buildWindow(cal.time, slot.startMinuteOfDay, slot.endMinuteOfDay))
            }
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return windows
    }

    // ── TASK_RATE ───────────────────────────────────────────────

    private fun resolveTaskRange(slot: TimeSlot): List<TimeWindow> {
        val start = slot.rangeStart ?: return emptyList()
        val end   = slot.rangeEnd   ?: return emptyList()

        val calStart = Calendar.getInstance().apply { time = start }
        val calEnd   = Calendar.getInstance().apply { time = end }

        val startDayMs = startOfDay(start).time
        val endDayMs   = startOfDay(end).time

        return if (startDayMs == endDayMs) {
            // Mismo día — ventana simple
            listOf(TimeWindow(start.time, end.time))
        } else {
            val windows = mutableListOf<TimeWindow>()

            // Primer día: desde la hora de inicio hasta medianoche
            windows.add(TimeWindow(
                start.time,
                startDayMs + ONE_DAY_MS  // medianoche del día siguiente
            ))

            // Días intermedios completos
            var currentDayMs = startDayMs + ONE_DAY_MS
            while (currentDayMs < endDayMs) {
                windows.add(TimeWindow(currentDayMs, currentDayMs + ONE_DAY_MS))
                currentDayMs += ONE_DAY_MS
            }

            // Último día: desde medianoche hasta la hora de fin
            windows.add(TimeWindow(endDayMs, end.time))

            windows
        }
    }

    // ── WEEKLY / EVEN_WEEKS / ODD_WEEKS ─────────────────────────

    private enum class WeekParity { EVEN, ODD }

    /**
     * Para recurrencias semanales (con o sin filtro de paridad) se usa
     * un horizonte de ±365 días desde hoy si el slot no tiene rangeStart/End.
     */
    private fun resolveRepeating(slot: TimeSlot, weekFilter: WeekParity?): List<TimeWindow> {
        val horizonStart = slot.rangeStart ?: run {
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -365) }.time
        }
        val horizonEnd = slot.rangeEnd ?: run {
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 365) }.time
        }

        val windows = mutableListOf<TimeWindow>()
        val cal = Calendar.getInstance().apply { time = startOfDay(horizonStart) }
        val endMs = startOfDay(horizonEnd).time

        while (cal.timeInMillis <= endMs) {
            val dow = calDowToIso(cal.get(Calendar.DAY_OF_WEEK))
            if (slot.daysOfWeek.isEmpty() || dow in slot.daysOfWeek) {
                if (weekFilter == null || matchesParity(cal, weekFilter)) {
                    windows.add(buildWindow(cal.time, slot.startMinuteOfDay, slot.endMinuteOfDay))
                }
            }
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return windows
    }

    // ─────────────────────────────────────────────────────────────
    // Utilidades
    // ─────────────────────────────────────────────────────────────

    /**
     * Construye una ventana absoluta dado un día base y minutos de inicio/fin.
     * Si endMinute <= startMinute se asume que la franja cruza la medianoche
     * y el fin pertenece al día siguiente.
     */
    private fun buildWindow(baseDay: Date, startMinute: Int, endMinute: Int): TimeWindow {
        val startMs = startOfDay(baseDay).time + startMinute * 60_000L
        val endMs = if (endMinute > startMinute) {
            startOfDay(baseDay).time + endMinute * 60_000L
        } else {
            // Cruza medianoche → fin en el día siguiente
            startOfDay(baseDay).time + ONE_DAY_MS + endMinute * 60_000L
        }
        return TimeWindow(startMs, endMs)
    }

    /**
     * Paridad de semana ISO: semana par → EVEN, impar → ODD.
     */
    private fun matchesParity(cal: Calendar, filter: WeekParity): Boolean {
        val weekNumber = cal.get(Calendar.WEEK_OF_YEAR)
        return when (filter) {
            WeekParity.EVEN -> weekNumber % 2 == 0
            WeekParity.ODD  -> weekNumber % 2 != 0
        }
    }

    private fun startOfDay(date: Date): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    private const val ONE_DAY_MS = 24 * 60 * 60 * 1_000L
}