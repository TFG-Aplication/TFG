package com.asistente.core.domain.models

/**
 * Tipo de franja:
 * BLOCKED   → el algoritmo no colocará actividades aquí
 * PREFERRED → el algoritmo priorizará colocar actividades aquí
 * AVAILABLE → el algoritmo puede usar esta franja si no hay hueco mejor
 */
enum class SlotType {
    BLOCKED,
    PREFERRED,
    AVAILABLE
}