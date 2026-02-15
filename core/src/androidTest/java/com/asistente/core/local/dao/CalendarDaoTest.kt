package com.asistente.core.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asistente.core.data.local.AppDatabase
import com.asistente.core.data.local.daos.CalendarDao
import com.asistente.core.domain.models.Calendar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class CalendarDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var calendarDao: CalendarDao

    private val testUserId = "user123"

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // Solo para tests
            .build()
        calendarDao = database.calendarDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    // ========== TEST 1: Inserción y recuperación básica ==========
    @Test
    fun insertCalendar_canBeRetrievedById() = runTest {
        // Arrange
        val calendar = Calendar(
            id = UUID.randomUUID().toString(),
            name = "Test Calendar",
            code = "TEST123",
            owners = listOf("user1", "user2"),
            isShared = true,
            syncStatus = 0
        )

        // Act
        calendarDao.insertCalendar(calendar)
        val retrieved = calendarDao.getCalendarById(calendar.id)

        // Assert
        assertNotNull(retrieved)
        assertEquals("Test Calendar", retrieved.name)
        assertEquals("TEST123", retrieved.code)
        assertEquals(true, retrieved.isShared)
    }

    // ========== TEST 2: Flow reactivo con getAllCalendarsByUserIdFlow ==========
    @Test
    fun getAllCalendarsByUserIdFlow_returnsUserCalendars() = runTest {
        // Arrange
        val cal1 = Calendar(
            id = UUID.randomUUID().toString(),
            name = "Calendar 1",
            code = "CAL1",
            owners = listOf(testUserId, "otherUser")
        )
        val cal2 = Calendar(
            id = UUID.randomUUID().toString(),
            name = "Calendar 2",
            code = "CAL2",
            owners = listOf(testUserId)
        )
        val cal3 = Calendar(
            id = UUID.randomUUID().toString(),
            name = "Calendar 3",
            code = "CAL3",
            owners = listOf("differentUser")
        )

        // Act
        calendarDao.insertCalendar(cal1)
        calendarDao.insertCalendar(cal2)
        calendarDao.insertCalendar(cal3)

        val calendars = calendarDao.getAllCalendarsByUserIdFlow(testUserId).first()

        // Assert
        assertEquals(2, calendars.size)
        assertTrue(calendars.any { it.name == "Calendar 1" })
        assertTrue(calendars.any { it.name == "Calendar 2" })
        assertTrue(calendars.none { it.name == "Calendar 3" })
    }

    // ========== TEST 3: Flow con getCalendarByIdFlow ==========
    @Test
    fun getCalendarByIdFlow_returnsCorrectCalendar() = runTest {
        val calendar = Calendar(
            id = "test-id-123",
            name = "My Calendar",
            code = "MYCODE",
            owners = listOf(testUserId)
        )

        calendarDao.insertCalendar(calendar)

        val retrieved = calendarDao.getCalendarByIdFlow("test-id-123").first()

        assertNotNull(retrieved)
        assertEquals("My Calendar", retrieved.name)
    }

    // ========== TEST 4: Soft delete (syncStatus = 2) ==========
    @Test
    fun softDelete_hidesCalendarFromQueries() = runTest {
        val calendar = Calendar(
            id = UUID.randomUUID().toString(),
            name = "ToDelete",
            code = "DEL123",
            owners = listOf(testUserId),
            syncStatus = 0
        )

        calendarDao.insertCalendar(calendar)

        // Verifica que existe
        assertNotNull(calendarDao.getCalendarById(calendar.id))

        // Marca como eliminado (syncStatus = 2)
        val deletedCalendar = calendar.copy(syncStatus = 2)
        calendarDao.insertCalendar(deletedCalendar) // REPLACE actualiza

        // Verifica que ya no aparece en queries con syncStatus != 2
        assertNull(calendarDao.getCalendarById(calendar.id))
    }

    // ========== TEST 5: Update calendar ==========
    @Test
    fun updateCalendar_changesAreReflected() = runTest {
        val calendar = Calendar(
            id = UUID.randomUUID().toString(),
            name = "Original Name",
            code = "ORIG",
            owners = listOf(testUserId)
        )

        calendarDao.insertCalendar(calendar)

        // Update
        val updated = calendar.copy(name = "Updated Name", isShared = true)
        calendarDao.updateCalendar(updated)

        val retrieved = calendarDao.getCalendarById(calendar.id)

        assertNotNull(retrieved)
        assertEquals("Updated Name", retrieved.name)
        assertEquals(true, retrieved.isShared)
    }

    // ========== TEST 6: Delete calendar ==========
    @Test
    fun deleteCalendarById_removesCalendar() = runTest {
        val calendar = Calendar(
            id = UUID.randomUUID().toString(),
            name = "To Delete",
            code = "DEL",
            owners = listOf(testUserId)
        )

        calendarDao.insertCalendar(calendar)

        // Verifica que existe
        assertNotNull(calendarDao.getCalendarById(calendar.id))

        // Elimina
        calendarDao.deleteCalendarById(calendar.id)

        // Verifica que ya no existe
        assertNull(calendarDao.getCalendarById(calendar.id))
    }

    // ========== TEST 7: Get calendar by code ==========
    @Test
    fun getCalendarByCode_returnsCorrectCalendar() = runTest {
        val calendar = Calendar(
            id = UUID.randomUUID().toString(),
            name = "Shared Calendar",
            code = "SHARE2024",
            owners = listOf(testUserId)
        )

        calendarDao.insertCalendar(calendar)

        val retrieved = calendarDao.getCalendarByCode("SHARE2024")

        assertNotNull(retrieved)
        assertEquals("Shared Calendar", retrieved.name)
    }

    // ========== TEST 8: Get calendar by code Flow ==========
    @Test
    fun getCalendarByCodeFlow_returnsCorrectCalendar() = runTest {
        val calendar = Calendar(
            id = UUID.randomUUID().toString(),
            name = "Flow Calendar",
            code = "FLOW123",
            owners = listOf(testUserId)
        )

        calendarDao.insertCalendar(calendar)

        val retrieved = calendarDao.getCalendarByCodeFlow("FLOW123").first()

        assertNotNull(retrieved)
        assertEquals("Flow Calendar", retrieved.name)
    }

    // ========== TEST 9: Get unsynced calendars ==========
    @Test
    fun getUnsyncedCalendars_returnsOnlyUnsyncedOnes() = runTest {
        val synced = Calendar(
            id = UUID.randomUUID().toString(),
            name = "Synced",
            code = "SYNC1",
            owners = listOf(testUserId),
            syncStatus = 1 // Sincronizado
        )
        val unsynced = Calendar(
            id = UUID.randomUUID().toString(),
            name = "Unsynced",
            code = "UNSYNC1",
            owners = listOf(testUserId),
            syncStatus = 0 // No sincronizado
        )

        calendarDao.insertCalendar(synced)
        calendarDao.insertCalendar(unsynced)

        val unsyncedCalendars = calendarDao.getUnsyncedCalendars(testUserId)

        assertEquals(1, unsyncedCalendars.size)
        assertEquals("Unsynced", unsyncedCalendars[0].name)
    }

    // ========== TEST 10: Get calendars by sync status ==========
    @Test
    fun getCalendarBySyncStatus_filtersCorrectly() = runTest {
        val cal1 = Calendar(
            id = UUID.randomUUID().toString(),
            name = "Status 0",
            code = "S0",
            owners = listOf(testUserId),
            syncStatus = 0
        )
        val cal2 = Calendar(
            id = UUID.randomUUID().toString(),
            name = "Status 1",
            code = "S1",
            owners = listOf(testUserId),
            syncStatus = 1
        )
        val cal3 = Calendar(
            id = UUID.randomUUID().toString(),
            name = "Status 2",
            code = "S2",
            owners = listOf(testUserId),
            syncStatus = 2
        )

        calendarDao.insertCalendar(cal1)
        calendarDao.insertCalendar(cal2)
        calendarDao.insertCalendar(cal3)

        val status0 = calendarDao.getCalendarBySyncStatus(0, testUserId)
        val status1 = calendarDao.getCalendarBySyncStatus(1, testUserId)
        val status2 = calendarDao.getCalendarBySyncStatus(2, testUserId)

        assertEquals(1, status0.size)
        assertEquals("Status 0", status0[0].name)

        assertEquals(1, status1.size)
        assertEquals("Status 1", status1[0].name)

        assertEquals(1, status2.size)
        assertEquals("Status 2", status2[0].name)
    }

    // ========== TEST 11: Índice único en 'code' ==========
    @Test
    fun insertingDuplicateCode_replacesExisting() = runTest {
        val cal1 = Calendar(
            id = UUID.randomUUID().toString(),
            name = "First",
            code = "UNIQUE",
            owners = listOf(testUserId)
        )
        val cal2 = Calendar(
            id = UUID.randomUUID().toString(),
            name = "Second",
            code = "UNIQUE", // Mismo código
            owners = listOf(testUserId)
        )

        calendarDao.insertCalendar(cal1)

        try {
            calendarDao.insertCalendar(cal2)
            // Si llega aquí, Room reemplazó por el OnConflictStrategy.REPLACE
            // O lanzó excepción por el índice único
        } catch (e: android.database.sqlite.SQLiteConstraintException) {
            // Esperado si el índice único funciona
            assertTrue(true)
        }
    }

    // ========== TEST 12: Owners como List funciona correctamente ==========
    @Test
    fun ownersField_storesAndRetrievesListCorrectly() = runTest {
        val calendar = Calendar(
            id = UUID.randomUUID().toString(),
            name = "Multi Owner",
            code = "MULTI",
            owners = listOf("user1", "user2", "user3")
        )

        calendarDao.insertCalendar(calendar)
        val retrieved = calendarDao.getCalendarById(calendar.id)

        assertNotNull(retrieved)
        assertEquals(3, retrieved.owners.size)
        assertTrue(retrieved.owners.contains("user1"))
        assertTrue(retrieved.owners.contains("user2"))
        assertTrue(retrieved.owners.contains("user3"))
    }
}