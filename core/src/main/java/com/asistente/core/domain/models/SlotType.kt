package com.asistente.core.domain.models

/**
 * Tipo de franja:
 * BLOCKED       → el algoritmo no colocará actividades aquí (creada manualmente por el usuario)
 * TASK_BLOCKED  → igual que BLOCKED pero generada automáticamente por una tarea
 */
enum class SlotType {
    BLOCKED,
    TASK_BLOCKED
}