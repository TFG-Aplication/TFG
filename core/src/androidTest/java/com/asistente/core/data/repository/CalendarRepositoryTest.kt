package com.asistente.core.data.repository

import androidx.work.*
import com.asistente.core.data.local.daos.CalendarDao
import com.asistente.core.data.worker.CalendarWorker
import com.asistente.core.domain.models.Calendar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(MockitoJUnitRunner::class)
class CalendarRepositoryTest {

    @Mock
    private lateinit var calendarDao: CalendarDao

    @Mock
    private lateinit var workManager: WorkManager

    private lateinit var repository: CalendarRepository

    private val testUserId = "user123"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Inyección directa
        repository = CalendarRepository(calendarDao, workManager)
    }

    //  TEST 1: getCalendarById 

    @Test
    fun getCalendarByIdReturnsCalendarFromDao() = runTest {
        val calendarId = UUID.randomUUID().toString()
        val expectedCalendar = Calendar(
            id = UUID.randomUUID().toString(),
            name = "My Test Calendar",
            code = "TEST123",
            owners = listOf(testUserId),
            isShared = true
        )
        whenever(calendarDao.getCalendarById(calendarId)).thenReturn(expectedCalendar)

        val result = repository.getCalendarById(calendarId)

        assertNotNull(result)
        assertEquals("My Test Calendar", result.name)
        verify(calendarDao).getCalendarById(calendarId)
    }

    @Test
    fun getCalendarByIdWhenDoesNotExist() = runTest {
        val calendarId = UUID.randomUUID().toString()
        whenever(calendarDao.getCalendarById(calendarId)).thenReturn(null)

        val result = repository.getCalendarById(calendarId)

        assertNull(result)
        verify(calendarDao).getCalendarById(calendarId)
    }

    //  TEST 2: getAllCalendarByUserId 

    @Test
    fun getAllCalendarByUserId() = runTest {
        val calendars = listOf(
            Calendar(
                id = UUID.randomUUID().toString(),
                name = "My Test Calendar",
                code = "TEST123",
                owners = listOf(testUserId),
                isShared = true
            ),
            Calendar(
                id = UUID.randomUUID().toString(),
                name = "My Test Calendar 2",
                code = "TEST125",
                owners = listOf(testUserId),
                isShared = true
            )
        )
        whenever(calendarDao.getAllCalendarsByUserIdFlow(testUserId))
            .thenReturn(flowOf(calendars))

        val result = repository.getAllCalendarByUserId(testUserId).first()

        assertEquals(2, result.size)
        assertEquals("My Test Calendar", result[0].name)
        assertEquals("My Test Calendar 2", result[1].name)

        verify(calendarDao).getAllCalendarsByUserIdFlow(testUserId)

        verify(workManager).enqueueUniqueWork(
            eq("sync_calendar_$testUserId"),
            eq(ExistingWorkPolicy.REPLACE),
            any<OneTimeWorkRequest>()
        )
    }

    //  TEST 3: saveCalendar 

    @Test
    fun saveCalendarSetsSyncStatusTo0() = runTest {
        val calendar = Calendar(
            id = UUID.randomUUID().toString(),
            name = "My Test Calendar",
            code = "TEST123",
            owners = listOf(testUserId),
            isShared = true
        )

        repository.saveCalendar(calendar)

        verify(calendarDao).insertCalendar(calendar.copy(syncStatus = 0))
        verify(workManager).enqueueUniqueWork(
            eq("sync_calendar_$testUserId"),
            eq(ExistingWorkPolicy.REPLACE),
            any<OneTimeWorkRequest>()
        )
    }

    @Test
    fun saveCalendarWhenOwnersIsEmpty() = runTest {
        val calendar = Calendar(
            id = UUID.randomUUID().toString(),
            name = "My Test Calendar",
            code = "TEST123",
            owners = listOf(),
            isShared = true
        )

        repository.saveCalendar(calendar)

        verify(calendarDao).insertCalendar(calendar.copy(syncStatus = 0))

        val nameCaptor = argumentCaptor<String>()
        val policyCaptor = argumentCaptor<ExistingWorkPolicy>()
        val requestCaptor = argumentCaptor<OneTimeWorkRequest>()

        verify(workManager).enqueueUniqueWork(
            nameCaptor.capture(),
            policyCaptor.capture(),
            requestCaptor.capture()
        )

        assertEquals("sync_calendar_unknown", nameCaptor.firstValue)
        assertEquals(ExistingWorkPolicy.REPLACE, policyCaptor.firstValue)
    }

    //  TEST 4: updateCalendar 

    @Test
    fun updateCalendarSetsSyncStatusTo0() = runTest {
        val calendar = Calendar(
            id = UUID.randomUUID().toString(),
            name = "My Test Calendar",
            code = "TEST123",
            owners = listOf(testUserId),
            isShared = true
        )

        repository.updateCalendar(calendar)

        verify(calendarDao).updateCalendar(calendar.copy(syncStatus = 0))
        verify(workManager).enqueueUniqueWork(
            eq("sync_calendar_$testUserId"),
            eq(ExistingWorkPolicy.REPLACE),
            any<OneTimeWorkRequest>()
        )
    }

    //  TEST 5: deleteCalendar (shared = true) 

    @Test
    fun deleteCalendarSharedSyncStatus2() = runTest {
        val calendarId = UUID.randomUUID().toString()
        val calendar = Calendar(
            id = calendarId,
            name = "Shared",
            code = "SH",
            owners = listOf(testUserId),
            isShared = true,
            syncStatus = 1
        )
        whenever(calendarDao.getCalendarById(calendarId)).thenReturn(calendar)

        repository.deleteCalendar(calendarId, isShared = true)

        verify(calendarDao).getCalendarById(calendarId)
        verify(calendarDao).insertCalendar(calendar.copy(syncStatus = 2))
        verify(workManager).enqueueUniqueWork(
            eq("sync_calendar_$testUserId"),
            eq(ExistingWorkPolicy.REPLACE),
            any<OneTimeWorkRequest>()
        )
    }

    @Test
    fun deleteCalendarSharedWhenCalendarNotFound() = runTest {
        val calendarId = UUID.randomUUID().toString()
        whenever(calendarDao.getCalendarById(calendarId)).thenReturn(null)

        repository.deleteCalendar(calendarId, isShared = true)

        verify(calendarDao).getCalendarById(calendarId)
        verify(calendarDao, never()).insertCalendar(any())
        verify(workManager, never()).enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>())
    }

    //  TEST 6: deleteCalendar (shared = false) 

    @Test
    fun deleteCalendarWithSharedFalse() = runTest {
        val calendarId = UUID.randomUUID().toString()

        repository.deleteCalendar(calendarId, isShared = false)

        verify(calendarDao).deleteCalendarById(calendarId)
        verify(calendarDao, never()).insertCalendar(any())
        verify(workManager, never()).enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>())
    }

    //  TEST 7: Worker configuration 

    @Test
    fun enqueueSyncWorkerCreatesWorkerWithCorrectConstraints() = runTest {
        val calendar = Calendar(
            id = UUID.randomUUID().toString(),
            name = "Test",
            code = "TEST",
            owners = listOf(testUserId)
        )

        repository.saveCalendar(calendar)

        val nameCaptor = argumentCaptor<String>()
        val policyCaptor = argumentCaptor<ExistingWorkPolicy>()
        val requestCaptor = argumentCaptor<OneTimeWorkRequest>()

        verify(workManager).enqueueUniqueWork(
            nameCaptor.capture(),
            policyCaptor.capture(),
            requestCaptor.capture()
        )

        assertEquals("sync_calendar_$testUserId", nameCaptor.firstValue)
        assertEquals(ExistingWorkPolicy.REPLACE, policyCaptor.firstValue)

        val workRequest = requestCaptor.firstValue
        assertEquals(NetworkType.CONNECTED, workRequest.workSpec.constraints.requiredNetworkType)
        assertEquals(testUserId, workRequest.workSpec.input.getString(CalendarWorker.KEY_USER_ID))
        assertEquals(BackoffPolicy.EXPONENTIAL, workRequest.workSpec.backoffPolicy)
    }

    //  TEST 8: Multiple operations 

    @Test
    fun multipleOperationsWithSameUserId() = runTest {
        val cal1 = Calendar(
            id = UUID.randomUUID().toString(),
            name = "Cal1",
            code = "C1",
            owners = listOf(testUserId)
        )
        val cal2 = Calendar(
            id = UUID.randomUUID().toString(),
            name = "Cal2",
            code = "C2",
            owners = listOf(testUserId)
        )

        repository.saveCalendar(cal1)
        repository.saveCalendar(cal2)

        verify(workManager, times(2)).enqueueUniqueWork(
            eq("sync_calendar_$testUserId"),
            eq(ExistingWorkPolicy.REPLACE),
            any<OneTimeWorkRequest>()
        )
    }
}
