package com.asistente.core.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.*
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.asistente.core.data.local.AppDatabase
import com.asistente.core.data.local.daos.CalendarDao
import com.asistente.core.data.remote.CalendarRemoteServices
import com.asistente.core.data.repository.CalendarRepository
import com.asistente.core.data.worker.CalendarWorker
import com.asistente.core.domain.models.Calendar
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
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
class CalendarIntegration {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var calendarDao: CalendarDao
    private lateinit var firestore: FirebaseFirestore
    private lateinit var remoteServices: CalendarRemoteServices
    private lateinit var repository: CalendarRepository
    private lateinit var workManager: WorkManager

    private val testUserId = "user123"

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        if (FirebaseApp.getApps(context).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setProjectId("demo-test-project")
                .setApplicationId("1:123456789:android:abcdef")
                .build()
            FirebaseApp.initializeApp(context, options)
        }

        firestore = FirebaseFirestore.getInstance()

        try {
            firestore.useEmulator("10.0.2.2", 8080)
        } catch (e: IllegalStateException) {
            // Ya está configurado, continuar
        }

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()
        firestore.firestoreSettings = settings

        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        calendarDao = database.calendarDao()
        remoteServices = CalendarRemoteServices(firestore)

        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG).setExecutor(SynchronousExecutor()).build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        workManager = WorkManager.getInstance(context)

        repository = CalendarRepository(calendarDao, workManager)
    }

    @After
    fun teardown() = runTest {
        if (::firestore.isInitialized) {
            val calendars = firestore.collection("calendars").get().await()

            calendars.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        }
        if (::database.isInitialized) {
            database.close()
        }
    }

    private suspend fun executeWorker(): ListenableWorker.Result {
        val worker = TestListenableWorkerBuilder<CalendarWorker>(context)
            .setInputData(workDataOf(CalendarWorker.KEY_USER_ID to testUserId))
            .setWorkerFactory(TestWorkerFactory(calendarDao, remoteServices))
            .build()

        return worker.doWork()
    }

    //  TESTS

    @Test
    fun saveCalendar() = runTest {
        val calendar = Calendar(
            id = UUID.randomUUID().toString(),
            name = "My Test Calendar",
            code = "TEST123",
            owners = listOf(testUserId),
            isShared = true
        )

        repository.saveCalendar(calendar)

        val localCalendar = calendarDao.getCalendarById(calendar.id)
        assertNotNull(localCalendar)
        assertEquals(0, localCalendar.syncStatus)

        val workerResult = executeWorker()
        assertEquals(ListenableWorker.Result.success(), workerResult)

        val firestoreDoc = firestore.collection("calendars")
            .document(calendar.id).get().await()

        assertTrue(firestoreDoc.exists())
        assertEquals("My Test Calendar", firestoreDoc.getString("name"))

        val updatedLocal = calendarDao.getCalendarById(calendar.id)
        assertEquals(1, updatedLocal?.syncStatus)
    }

    @Test
    fun downloadCalendar() = runTest {
        val calendarId = UUID.randomUUID().toString()
        val calendarData = hashMapOf(
            "id" to calendarId,
            "name" to "Remote Calendar",
            "code" to "REMOTE",
            "owners" to listOf(testUserId),
            "isShared" to true
        )

        firestore.collection("calendars")
            .document(calendarId).set(calendarData).await()

        val beforeSync = calendarDao.getCalendarById(calendarId)
        assertNull(beforeSync)

        val workerResult = executeWorker()
        assertEquals(ListenableWorker.Result.success(), workerResult)

        val afterSync = calendarDao.getCalendarById(calendarId)
        assertNotNull(afterSync)
        assertEquals("Remote Calendar", afterSync.name)
        assertEquals(1, afterSync.syncStatus)
    }

    @Test
    fun updateCalendar() = runTest {
        val calendar = Calendar(
            id = UUID.randomUUID().toString(),
            name = "My Test Calendar",
            code = "TEST123",
            owners = listOf(testUserId),
            isShared = true
        )

        repository.saveCalendar(calendar)
        executeWorker()

        val updated = calendar.copy(name = "Updated Name")
        repository.updateCalendar(updated)

        val workerResult = executeWorker()
        assertEquals(ListenableWorker.Result.success(), workerResult)

        val updatedDoc = firestore.collection("calendars")
            .document(calendar.id).get().await()

        assertEquals("Updated Name", updatedDoc.getString("name"))
    }

    @Test
    fun deleteCalendar() = runTest {
        val calendar = Calendar(
            id = UUID.randomUUID().toString(),
            name = "My Test Calendar",
            code = "TEST123",
            owners = listOf(testUserId),
            isShared = true
        )

        repository.saveCalendar(calendar)
        executeWorker()

        repository.deleteCalendar(calendar.id, isShared = true)

        val workerResult = executeWorker()
        assertEquals(ListenableWorker.Result.success(), workerResult)

        val afterDelete = firestore.collection("calendars")
            .document(calendar.id).get().await()
        assertTrue(!afterDelete.exists())

        val localFinal = calendarDao.getCalendarById(calendar.id)
        assertNull(localFinal)
    }

    @Test
    fun multipleCalendars() = runTest {
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

        calendars.forEach { repository.saveCalendar(it) }

        val workerResult = executeWorker()
        assertEquals(ListenableWorker.Result.success(), workerResult)

        val remoteCalendars = remoteServices.getAllCalendarByUserIdRemote(testUserId)
        assertEquals(2, remoteCalendars.size)

        val flowCalendars = repository.getAllCalendarByUserId(testUserId).first()
        assertEquals(2, flowCalendars.size)
    }


    private class TestWorkerFactory(
        private val calendarDao: CalendarDao,
        private val remoteServices: CalendarRemoteServices
    ) : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker? {
            return when (workerClassName) {
                CalendarWorker::class.java.name -> {
                    CalendarWorker(appContext, workerParameters, calendarDao, remoteServices)
                }
                else -> null
            }
        }
    }
}