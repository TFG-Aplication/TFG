package com.asistente.core.data.repository

import com.asistente.core.data.local.daos.CalendarDao
import com.asistente.core.data.remote.CalendarRemoteServices
import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.ropositories.interfaz.CalendarRepositoryInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class CalendarRepository @Inject constructor(
    private val localCalendar: CalendarDao,
    private val remoteCalendar: CalendarRemoteServices,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : CalendarRepositoryInterface {

    // a lo mejor hay q cambiarlo :)
    override suspend fun getCalendarById(id: String): Calendar? {
        var calendar = localCalendar.getCalendarById(id)
        if (calendar == null)
            calendar = remoteCalendar.getCalendarByIdRemote(id)
            if (calendar != null) {
                localCalendar.insertCalendar(calendar)
            }
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
                    if (local.syncStatus == 1 && !remoteIds.contains(local.id)) {
                        localCalendar.deleteCalendarById(local.id)
                    }
                    if(local.syncStatus == 0 && !remoteIds.contains(local.id)) {
                        remoteCalendar.saveCalendarRemote(local)
                        local.syncStatus = 1
                    }

                }
                // si otra persona ha creado nuevos calendarios compartidos actualiza el room
                remoteCalendars.forEach { remote ->
                    // Al insertar con REPLACE, se actualiza lo que ya existía
                    localCalendar.insertCalendar(remote.copy(syncStatus = 1))
                }
            } catch (e: Exception) { }
        }
        return localCalendar.getAllCalendarsByUserId(id)
    }

    override suspend fun saveCalendar(calendar: Calendar) {
        // Guarda en Room -> Sube a FireBase y marca como sincronizado
        localCalendar.insertCalendar(calendar)
        externalScope.launch {
            val success = remoteCalendar.saveCalendarRemote(calendar)
            if (success) {
                // SOLO si Firebase confirma, actualizamos local a status 1
                localCalendar.insertCalendar(calendar.copy(syncStatus = 1))
                android.util.Log.d("REPO", "Calendario sincronizado con éxito")
            }
        }
        println("seguardo todo todito todo")
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