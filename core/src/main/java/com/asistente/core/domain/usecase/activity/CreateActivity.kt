// CreateActivity.kt
package com.asistente.core.domain.usecase.activity

import com.asistente.core.domain.models.Activity
import com.asistente.core.domain.repositories.interfaz.ActivityRepositoryInterface
import javax.inject.Inject

class CreateActivity @Inject constructor(
    private val repository: ActivityRepositoryInterface
) {
    suspend operator fun invoke(activity: Activity, isSharedCalendar: Boolean = false) {
        val start = activity.earliest_start ?: java.util.Date()
        val end = java.util.Date(start.time + activity.durationMinutes * 60 * 1000)

        val adjustedStart = if (activity.deadline != null && end.after(activity.deadline)) {
            java.util.Date(activity.deadline.time - activity.durationMinutes * 60 * 1000)
        } else start

        repository.saveActivity(
            activity.copy(
                scheduled_start = adjustedStart,
                scheduled_end = java.util.Date(adjustedStart.time + activity.durationMinutes * 60 * 1000),
                is_scheduled = true
            ),
            isSharedCalendar
        )
    }
}