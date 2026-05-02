package com.asistente.planificador.ui.screens

import com.asistente.core.domain.models.RecurrenceType
import com.asistente.core.domain.models.SlotType

fun SlotType.label(): String = when (this) {
    SlotType.BLOCKED      -> "Bloqueada Manualmente"
    SlotType.TASK_BLOCKED -> "Bloqueada por Tarea"
}

fun RecurrenceType.shortLabel(): String = when (this) {
    RecurrenceType.WEEKLY     -> "Semanal"
    RecurrenceType.EVEN_WEEKS -> "Semanas pares"
    RecurrenceType.ODD_WEEKS  -> "Semanas impares"
    RecurrenceType.DATE_RANGE -> "Rango"
    RecurrenceType.SINGLE_DAY -> "Día único"
    RecurrenceType.TASK_RANGE -> "Rango"
}