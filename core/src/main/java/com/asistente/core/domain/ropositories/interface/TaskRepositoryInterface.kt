package com.asistente.core.domain.ropositories.`interface`

import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.models.Task
import kotlinx.coroutines.flow.Flow


interface TaskRepositoryInterface {

     suspend fun getTaskById(id: String): Task?
     fun getAllTaskByUserId(id: String): Flow<List<Task>>?
     fun getAllTaskByCalendarId(email: String): Flow<List<Task>>?

    suspend fun saveTask(Task: Task, isSharedCalendar: Boolean)

    suspend fun deleteTask(taskId: String, isShared: Boolean)

}