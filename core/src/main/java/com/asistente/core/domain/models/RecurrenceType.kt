package com.asistente.core.domain.models

/**
 * Tipos de recurrencia de una franja horaria
 * WEEKLY      → todos los lunes/martes/... indefinidamente
 * EVEN_WEEKS  → solo semanas pares
 * ODD_WEEKS   → solo semanas impares
 * DATE_RANGE  → activa entre rangeStart y rangeEnd (+ días de la semana dentro del rango)
 * SINGLE_DAY  → solo el día exacto indicado en rangeStart
 */

enum class RecurrenceType {
    WEEKLY,
    EVEN_WEEKS,
    ODD_WEEKS,
    DATE_RANGE,
    SINGLE_DAY
}