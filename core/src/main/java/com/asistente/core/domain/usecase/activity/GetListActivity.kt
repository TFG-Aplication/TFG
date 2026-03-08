package com.asistente.core.domain.usecases

import com.asistente.core.domain.models.Activity
import com.asistente.core.domain.repositories.interfaz.ActivityRepositoryInterface
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetListActivity @Inject constructor(
    private val repository: ActivityRepositoryInterface
) {
    operator fun invoke(calendarId: String): Flow<List<Activity>> =
        repository.getAllActivitiesByCalendarId(calendarId)
}