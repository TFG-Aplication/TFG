package com.asistente.core.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asistente.core.data.local.AppDatabase
import com.asistente.core.data.local.daos.CalendarDao
import com.asistente.core.data.local.daos.CategoryDao
import com.asistente.core.data.local.daos.TaskDao
import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.models.Category
import com.asistente.core.domain.models.Task
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.runner.RunWith
import java.util.Date
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TaskDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var taskDao: TaskDao
    private lateinit var calendarDao: CalendarDao
    private lateinit var categoryDao: CategoryDao

    private lateinit var testCalendarId: String
    private lateinit var testCategoryId: String
    private val testUserId = "user123"

    @Before
    fun setup() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        taskDao = database.taskDao()
        calendarDao = database.calendarDao()
        categoryDao = database.categoryDao()

        // Crear calendario de prueba
        val calendar = Calendar(
            id = UUID.randomUUID().toString(),
            name = "Test Calendar",
            code = "TEST",
            owners = listOf(testUserId)
        )
        calendarDao.insertCalendar(calendar)
        testCalendarId = calendar.id

        // Crear categoría de prueba
        val category = Category(
            id = UUID.randomUUID().toString(),
            name = "Test Category",
            parentCalendarId = testCalendarId,
            color = "#FF5733"
        )
        categoryDao.insertCategory(category)
        testCategoryId = category.id
    }

    @After
    fun teardown() {
        database.close()
    }

    // ========== TEST 1: Inserción y recuperación básica ==========
    @Test
    fun insertTask_canBeRetrievedById() = runTest {
        val task = Task(
            id = UUID.randomUUID().toString(),
            name = "Complete project",
            parentCalendarId = testCalendarId,
            categoryId = testCategoryId,
            owners = listOf(testUserId),
            place = "Office",
            notes = "Important task",
            alerts = listOf(1000L, 2000L),
            init_date = Date(),
            finish_date = Date(System.currentTimeMillis() + 86400000)
        )

        taskDao.insertTask(task)
        val retrieved = taskDao.getTaskById(task.id)

        assertNotNull(retrieved)
        assertEquals("Complete project", retrieved.name)
        assertEquals(testCalendarId, retrieved.parentCalendarId)
        assertEquals("Office", retrieved.place)
        assertEquals("Important task", retrieved.notes)
    }

    // ========== TEST 2: Flow por usuario ==========
    @Test
    fun getAllTasksByUserIdFlow_returnsUserTasks() = runTest {
        val userTask = Task(
            id = UUID.randomUUID().toString(),
            name = "User Task",
            parentCalendarId = testCalendarId,
            owners = listOf(testUserId, "otherUser")
        )
        val otherTask = Task(
            id = UUID.randomUUID().toString(),
            name = "Someone Else Task",
            parentCalendarId = testCalendarId,
            owners = listOf("differentUser")
        )

        taskDao.insertTask(userTask)
        taskDao.insertTask(otherTask)

        val tasks = taskDao.getAllTasksByUserIdFlow(testUserId).first()

        assertEquals(1, tasks.size)
        assertEquals("User Task", tasks[0].name)
    }

    // ========== TEST 3: Flow por calendario ==========
    @Test
    fun getAllTasksByCalendarIdFlow_returnsCalendarTasks() = runTest {
        val task1 = Task(
            id = UUID.randomUUID().toString(),
            name = "Task 1",
            parentCalendarId = testCalendarId,
            owners = listOf(testUserId)
        )
        val task2 = Task(
            id = UUID.randomUUID().toString(),
            name = "Task 2",
            parentCalendarId = testCalendarId,
            owners = listOf(testUserId)
        )
        val task3 = Task(
            id = UUID.randomUUID().toString(),
            name = "Other Calendar",
            parentCalendarId = "other-calendar-id",
            owners = listOf(testUserId)
        )

        taskDao.insertTask(task1)
        taskDao.insertTask(task2)
        taskDao.insertTask(task3)

        val tasks = taskDao.getAllTasksByCalendarIdFlow(testCalendarId).first()

        assertEquals(2, tasks.size)
        assertTrue(tasks.any { it.name == "Task 1" })
        assertTrue(tasks.any { it.name == "Task 2" })
        assertTrue(tasks.none { it.name == "Other Calendar" })
    }

    // ========== TEST 4: Flow por ID ==========
    @Test
    fun getTaskByIdFlow_returnsCorrectTask() = runTest {
        val task = Task(
            id = "test-task-123",
            name = "My Task",
            parentCalendarId = testCalendarId,
            owners = listOf(testUserId)
        )

        taskDao.insertTask(task)

        val retrieved = taskDao.getTaskByIdFlow("test-task-123").first()

        assertNotNull(retrieved)
        assertEquals("My Task", retrieved.name)
    }

    // ========== TEST 5: Suspendida por usuario ==========
    @Test
    fun getAllTasksByUserId_returnsUserTasks() = runTest {
        val task = Task(
            id = UUID.randomUUID().toString(),
            name = "Test Task",
            parentCalendarId = testCalendarId,
            owners = listOf(testUserId)
        )

        taskDao.insertTask(task)

        val tasks = taskDao.getAllTasksByUserId(testUserId)

        assertEquals(1, tasks.size)
        assertEquals("Test Task", tasks[0].name)
    }

    // ========== TEST 6: Suspendida por calendario ==========
    @Test
    fun getAllTasksByCalendarId_returnsCalendarTasks() = runTest {
        val task = Task(
            id = UUID.randomUUID().toString(),
            name = "Calendar Task",
            parentCalendarId = testCalendarId,
            owners = listOf(testUserId)
        )

        taskDao.insertTask(task)

        val tasks = taskDao.getAllTasksByCalendarId(testCalendarId)

        assertEquals(1, tasks.size)
        assertEquals("Calendar Task", tasks[0].name)
    }

    // ========== TEST 7: Update task ==========
    @Test
    fun updateTask_changesAreReflected() = runTest {
        val task = Task(
            id = UUID.randomUUID().toString(),
            name = "Original",
            parentCalendarId = testCalendarId,
            owners = listOf(testUserId),
            place = "Home"
        )

        taskDao.insertTask(task)

        val updated = task.copy(name = "Updated", place = "Office")
        taskDao.updateTask(updated)

        val retrieved = taskDao.getTaskById(task.id)

        assertNotNull(retrieved)
        assertEquals("Updated", retrieved.name)
        assertEquals("Office", retrieved.place)
    }

    // ========== TEST 8: Delete task ==========
    @Test
    fun deleteTaskById_removesTask() = runTest {
        val task = Task(
            id = UUID.randomUUID().toString(),
            name = "To Delete",
            parentCalendarId = testCalendarId,
            owners = listOf(testUserId)
        )

        taskDao.insertTask(task)
        assertNotNull(taskDao.getTaskById(task.id))

        taskDao.deleteTaskById(task.id)

        assertNull(taskDao.getTaskById(task.id))
    }

    // ========== TEST 9: Delete (syncStatus = 2) ==========
    @Test
    fun syncStatusDeleted_hidesTaskFromQueries() = runTest {
        val task = Task(
            id = UUID.randomUUID().toString(),
            name = "ToDelete",
            parentCalendarId = testCalendarId,
            owners = listOf(testUserId),
            syncStatus = 0
        )

        taskDao.insertTask(task)

        val before = taskDao.getAllTasksByCalendarIdFlow(testCalendarId).first()
        assertEquals(1, before.size)

        val deleted = task.copy(syncStatus = 2)
        taskDao.insertTask(deleted)

        val after = taskDao.getAllTasksByCalendarIdFlow(testCalendarId).first()
        assertEquals(0, after.size)

        val stillExists = taskDao.getTaskById(task.id)
        assertNotNull(stillExists)
        assertEquals(2, stillExists.syncStatus)
    }

    // ========== TEST 10: Get unsynced tasks ==========
    @Test
    fun getUnsyncedTask_returnsOnlyUnsynced() = runTest {
        val synced = Task(
            id = UUID.randomUUID().toString(),
            name = "Synced",
            parentCalendarId = testCalendarId,
            owners = listOf(testUserId),
            syncStatus = 1
        )
        val unsynced = Task(
            id = UUID.randomUUID().toString(),
            name = "Unsynced",
            parentCalendarId = testCalendarId,
            owners = listOf(testUserId),
            syncStatus = 0
        )

        taskDao.insertTask(synced)
        taskDao.insertTask(unsynced)

        val unsyncedTasks = taskDao.getUnsyncedTask(testCalendarId)

        assertEquals(1, unsyncedTasks.size)
        assertEquals("Unsynced", unsyncedTasks[0].name)
    }

    // ========== TEST 11: Get by sync status ==========
    @Test
    fun getTaskBySyncStatus_filtersCorrectly() = runTest {
        val task0 = Task(
            id = UUID.randomUUID().toString(),
            name = "Status 0",
            parentCalendarId = testCalendarId,
            owners = listOf(testUserId),
            syncStatus = 0
        )
        val task1 = Task(
            id = UUID.randomUUID().toString(),
            name = "Status 1",
            parentCalendarId = testCalendarId,
            owners = listOf(testUserId),
            syncStatus = 1
        )
        val task2 = Task(
            id = UUID.randomUUID().toString(),
            name = "Status 2",
            parentCalendarId = testCalendarId,
            owners = listOf(testUserId),
            syncStatus = 2
        )

        taskDao.insertTask(task0)
        taskDao.insertTask(task1)
        taskDao.insertTask(task2)

        val status0 = taskDao.getTaskBySyncStatus(0, testCalendarId)
        val status1 = taskDao.getTaskBySyncStatus(1, testCalendarId)
        val status2 = taskDao.getTaskBySyncStatus(2, testCalendarId)

        assertEquals(1, status0.size)
        assertEquals("Status 0", status0[0].name)

        assertEquals(1, status1.size)
        assertEquals("Status 1", status1[0].name)

        assertEquals(1, status2.size)
        assertEquals("Status 2", status2[0].name)
    }

    // ========== TEST 12: Date fields  ==========
    @Test
    fun dateFields_storeAndRetrieveCorrectly() = runTest {
        val initDate = Date(System.currentTimeMillis())
        val finishDate = Date(System.currentTimeMillis() + 3600000) // +1 hora

        val task = Task(
            id = UUID.randomUUID().toString(),
            name = "Task with dates",
            parentCalendarId = testCalendarId,
            owners = listOf(testUserId),
            init_date = initDate,
            finish_date = finishDate
        )

        taskDao.insertTask(task)
        val retrieved = taskDao.getTaskById(task.id)

        assertNotNull(retrieved)
        assertNotNull(retrieved.init_date)
        assertNotNull(retrieved.finish_date)

        // Comparar timestamps (precisión de 1 segundo)
        assertEquals(initDate.time / 1000, retrieved.init_date!!.time / 1000)
        assertEquals(finishDate.time / 1000, retrieved.finish_date!!.time / 1000)
    }

    // ========== TEST 13: List<String> owners  ==========
    @Test
    fun ownersField_storesAndRetrievesCorrectly() = runTest {
        val task = Task(
            id = UUID.randomUUID().toString(),
            name = "Multi owner",
            parentCalendarId = testCalendarId,
            owners = listOf("user1", "user2", "user3")
        )

        taskDao.insertTask(task)
        val retrieved = taskDao.getTaskById(task.id)

        assertNotNull(retrieved)
        assertEquals(3, retrieved.owners.size)
        assertTrue(retrieved.owners.contains("user1"))
        assertTrue(retrieved.owners.contains("user2"))
        assertTrue(retrieved.owners.contains("user3"))
    }

    // ========== TEST 14: List<Long> alerts  ==========
    @Test
    fun alertsField_storesAndRetrievesCorrectly() = runTest {
        val task = Task(
            id = UUID.randomUUID().toString(),
            name = "Task with alerts",
            parentCalendarId = testCalendarId,
            owners = listOf(testUserId),
            alerts = listOf(1000L, 2000L, 3000L)
        )

        taskDao.insertTask(task)
        val retrieved = taskDao.getTaskById(task.id)

        assertNotNull(retrieved)
        assertEquals(3, retrieved.alerts?.size)
        assertTrue(retrieved.alerts!!.contains(1000L))
        assertTrue(retrieved.alerts!!.contains(2000L))
        assertTrue(retrieved.alerts!!.contains(3000L))
    }

    // ========== TEST 15: OnConflictStrategy.REPLACE ==========
    @Test
    fun insertTask_withSameId_replacesExisting() = runTest {
        val taskId = UUID.randomUUID().toString()

        val original = Task(
            id = taskId,
            name = "Original",
            parentCalendarId = testCalendarId,
            owners = listOf(testUserId)
        )

        taskDao.insertTask(original)

        val replaced = Task(
            id = taskId,
            name = "Replaced",
            parentCalendarId = testCalendarId,
            owners = listOf(testUserId)
        )

        taskDao.insertTask(replaced)

        val retrieved = taskDao.getTaskById(taskId)

        assertNotNull(retrieved)
        assertEquals("Replaced", retrieved.name)
    }
}
