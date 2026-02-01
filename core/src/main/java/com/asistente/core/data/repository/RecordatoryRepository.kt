package com.asistente.core.data.repository

import com.asistente.core.data.local.daos.RecordatoryDao
import com.asistente.core.data.remote.RecordatoryRemoteServices
import com.asistente.core.domain.models.Recordatory
import com.asistente.core.domain.ropositories.interfaz.RecodatoryRepositoryInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class RecordatoryRepository @Inject constructor(
    private val localRecordatory: RecordatoryDao,

    private val remoteRecordatory: RecordatoryRemoteServices,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

) : RecodatoryRepositoryInterface {

    override suspend fun getRecordatoryById(id: String): Recordatory? {
        var Recordatory = localRecordatory.getRecordatoryById(id)
        if (Recordatory == null)
            Recordatory = remoteRecordatory.getRecordatoryByIdRemote(id)
        if (Recordatory != null)
            localRecordatory.insertRecordatory(Recordatory)

        return Recordatory
    }

    override fun getAllRecordatoryByUserId(id: String): Flow<List<Recordatory>> {
        externalScope.launch {
            try {
                val remoteRecordatorys = remoteRecordatory.getAllRecordatorysByUserIdRemote(id)
                val localRecordatorys = localRecordatory.getAllRecordatoryListByUserId(id)
                val remoteIds = remoteRecordatorys.map { it.id }
                localRecordatorys.forEach { local ->
                    if (!remoteIds.contains(local.id)) {
                        localRecordatory.deleteRecordatoryById(local.id)
                    }
                }
                remoteRecordatorys.forEach { remote ->
                    localRecordatory.insertRecordatory(remote)
                }

            } catch (e: Exception) {
            }
        }
        return localRecordatory.getAllRecordatorysByUserId(id)
    }

    override fun getAllRecordatoryByCalendarId(calendarId: String): Flow<List<Recordatory>> {
        externalScope.launch {
            try {
                val remoteRecordatorys = remoteRecordatory.getAllRecordatorysByCalendarIdRemote(calendarId)

                val localRecordatorys = localRecordatory.getAllRecordatoryList(calendarId)

                val remoteIds = remoteRecordatorys.map { it.id }
                localRecordatorys.forEach { local ->
                    if (!remoteIds.contains(local.id)) {
                        localRecordatory.deleteRecordatoryById(local.id)
                    }
                }
                remoteRecordatorys.forEach { localRecordatory.insertRecordatory(it) }
            } catch (e: Exception) {
            }
        }
        return localRecordatory.getAllRecordatorysByCalendarId(calendarId)
    }

    override suspend fun saveRecordatory(Recordatory: Recordatory, isSharedCalendar: Boolean) {
        if (isSharedCalendar) {
            val success = remoteRecordatory.saveRecordatoryRemote(Recordatory)
            if (success) {
                localRecordatory.insertRecordatory(Recordatory)
            } else {
                throw Exception("Conexión necesaria para modificar calendarios compartidos")
            }
        } else {
            // si calendario es normal
            localRecordatory.insertRecordatory(Recordatory)
            externalScope.launch {
                remoteRecordatory.saveRecordatoryRemote(Recordatory)
            }
        }
    }

    override suspend fun deleteRecordatory(RecordatoryId: String, isShared: Boolean) {
        if (isShared) {
            val success = remoteRecordatory.deleteRecordatoryRemote(RecordatoryId)
            if (success) localRecordatory.deleteRecordatoryById(RecordatoryId)
            else throw Exception("No se pudo borrar: verifica tu conexión")
        } else {
            localRecordatory.deleteRecordatoryById(RecordatoryId)
            externalScope.launch { remoteRecordatory.deleteRecordatoryRemote(RecordatoryId) }
        }
    }
}