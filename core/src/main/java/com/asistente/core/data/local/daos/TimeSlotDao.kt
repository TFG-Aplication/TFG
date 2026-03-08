package com.asistente.core.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.asistente.core.domain.models.TimeSlot
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeSlotDao {

    // ── Reactivas para UI ─────────────────────────────────────────────────

    @Query("SELECT * FROM time_slots WHERE parentCalendarId = :calendarId AND syncStatus != 2 AND isActive = 1")
    fun getAllTimeSlotsByCalendarIdFlow(calendarId: String): Flow<List<TimeSlot>>

    @Query("SELECT * FROM time_slots WHERE (owners LIKE '%\"' || :userId || '\"%' OR owners LIKE '%' || :userId || '%') AND syncStatus != 2 AND isActive = 1")
    fun getAllTimeSlotsByUserIdFlow(userId: String): Flow<List<TimeSlot>>

    @Query("SELECT * FROM time_slots WHERE id = :slotId")
    fun getTimeSlotByIdFlow(slotId: String): Flow<TimeSlot?>

    // ── Suspendidas para lógica interna / sync ────────────────────────────

    @Query("SELECT * FROM time_slots WHERE parentCalendarId = :calendarId AND syncStatus != 2 AND isActive = 1")
    suspend fun getAllTimeSlotsByCalendarId(calendarId: String): List<TimeSlot>

    @Query("SELECT * FROM time_slots WHERE (owners LIKE '%\"' || :userId || '\"%' OR owners LIKE '%' || :userId || '%') AND syncStatus != 2 AND isActive = 1")
    suspend fun getAllTimeSlotsByUserId(userId: String): List<TimeSlot>

    @Query("SELECT * FROM time_slots WHERE id = :slotId")
    suspend fun getTimeSlotById(slotId: String): TimeSlot?

    // ── INSERT / UPDATE / DELETE ──────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeSlot(timeSlot: TimeSlot)

    @Update
    suspend fun updateTimeSlot(timeSlot: TimeSlot)

    @Query("DELETE FROM time_slots WHERE id = :id")
    suspend fun deleteTimeSlotById(id: String)

    // ── SINCRONIZACIÓN ────────────────────────────────────────────────────

    @Query("SELECT * FROM time_slots WHERE syncStatus = 0 AND parentCalendarId = :calendarId")
    suspend fun getUnsyncedTimeSlots(calendarId: String): List<TimeSlot>

    @Query("SELECT * FROM time_slots WHERE syncStatus = :status AND parentCalendarId = :calendarId")
    suspend fun getTimeSlotBySyncStatus(status: Int, calendarId: String): List<TimeSlot>

    // ── Consultas específicas del planificador ────────────────────────────

    @Query("SELECT * FROM time_slots WHERE parentCalendarId = :calendarId AND slotType = 'BLOCKED' AND isActive = 1 AND syncStatus != 2")
    suspend fun getBlockedSlots(calendarId: String): List<TimeSlot>

    @Query("SELECT * FROM time_slots WHERE parentCalendarId = :calendarId AND slotType = 'PREFERRED' AND isActive = 1 AND syncStatus != 2")
    suspend fun getPreferredSlots(calendarId: String): List<TimeSlot>
}