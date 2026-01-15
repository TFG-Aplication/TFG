package com.asistente.core.domain.ropositories.`interface`

import com.asistente.core.domain.models.Recordatory
import kotlinx.coroutines.flow.Flow

interface RecodatoryRepositoryInterface {

    suspend fun getRecordatoryById(id: String): Recordatory?
    fun getAllRecordatoryByUserId(id: String): Flow<List<Recordatory>>?
    fun getAllRecordatoryByCalendarId(email: String): Flow<List<Recordatory>>?

    suspend fun saveRecordatory(Recordatory: Recordatory, isSharedCalendar: Boolean)

    suspend fun deleteRecordatory(RecordatoryId: String, isShared: Boolean)
}