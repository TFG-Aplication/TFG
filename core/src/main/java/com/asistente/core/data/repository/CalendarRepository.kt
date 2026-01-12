package com.asistente.core.data.repository

import com.asistente.core.data.local.daos.CalendarDao
import com.asistente.core.data.remote.CalendarRemoteServices
import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.ropositories.`interface`.CalendarRepositoryInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CalendarRepository(
    private val localCalendar: CalendarDao,
    private val remoteCalendar: CalendarRemoteServices,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : CalendarRepositoryInterface {

    override suspend fun getCalendarById(id: String): Calendar? {
        var calendar = localCalendar.getCalendarById(id)
        if (calendar == null)
            calendar = remoteCalendar.getCalendarByIdRemote(id)
            localCalendar.insertCalendar(calendar)

        return calendar
    }

    override fun getAllCalendarByUserId(id: String): Flow<List<Calendar>> {
        // si los calendarios compartidos han sido borrados x otro usuario se eliminan tambien del room antes de enseñarlos todos
        externalScope.launch {
            try {
                val remoteCalendars = remoteCalendar.getAllCalendarByUserIdRemote(id)
                val localCalendars = localCalendar.getAllCalendarsList(id)
                val remoteIds = remoteCalendars.map { it.id }
                localCalendars.forEach { local ->
                    if (!remoteIds.contains(local.id)) {
                        localCalendar.deleteCalendarById(local.id)
                    }
                }
                // si otrapersona ha creado nuevos calendarios compartidos actualiza el room
                remoteCalendars.forEach { localCalendar.insertCalendar(it) }

            } catch (e: Exception) { }
        }
        return localCalendar.getAllCalendarsByUserId(id)
    }

    override suspend fun saveCalendar(calendar: Calendar) {
        // Guarda en Room -> Sube a FireBase
        localCalendar.insertCalendar(calendar)
        externalScope.launch {
            remoteCalendar.saveCalendarRemote(calendar)
        }
    }

    override suspend fun deleteCalendar(id: String, isShared: Boolean) {
        if (isShared) {

            val success = remoteCalendar.deleteCalendarRemote(id)
            if (success) localCalendar.deleteCalendarById(id)
            else throw Exception("No se pudo borrar: verifica tu conexión")
        } else {
            localCalendar.deleteCalendarById(id)
            externalScope.launch { remoteCalendar.deleteCalendarRemote(id) }
        }
    }
}