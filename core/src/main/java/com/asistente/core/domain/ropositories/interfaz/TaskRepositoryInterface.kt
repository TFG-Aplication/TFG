package com.asistente.core.domain.ropositories.interfaz

import com.asistente.core.domain.models.Task
import kotlinx.coroutines.flow.Flow


interface TaskRepositoryInterface {

     suspend fun getTaskById(id: String): Task?
     fun getAllTaskByUserId(id: String): Flow<List<Task>>?
     fun getAllTaskByCalendarId(calendarId: String): Flow<List<Task>>?

    suspend fun saveTask(Task: Task, isSharedCalendar: Boolean)

    suspend fun updateTask(Task: Task)

    suspend fun deleteTask(taskId: String, isShared: Boolean)

}