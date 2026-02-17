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
import com.asistente.core.data.local.daos.CategoryDao
import com.asistente.core.data.local.daos.TaskDao
import com.asistente.core.data.remote.TaskRemoteServices
import com.asistente.core.data.repository.TaskRepository
import com.asistente.core.data.worker.TaskWorker
import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.models.Category
import com.asistente.core.domain.models.Task
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
class TaskIntegration {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var taskDao: TaskDao
    private lateinit var calendarDao: CalendarDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var firestore: FirebaseFirestore
    private lateinit var remoteServices: TaskRemoteServices
    private lateinit var repository: TaskRepository
    private lateinit var workManager: WorkManager

    // Datos de prueba base
    private val testUserId = "user123"
    private lateinit var testCalendar: Calendar
    private lateinit var testCategory: Category

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        // 1. Firebase
        if (FirebaseApp.getApps(context).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setProjectId("trabajo-fin-de-grado-40a05")
                .setApplicationId("1:756327291572:android:8a6ccc0e61bd8bdc7915e1")
                .setApiKey("AIzaSyAdtteXH9fPRuUkef6nnoegunZ7KN9T3Fw")
                .build()
            FirebaseApp.initializeApp(context, options)
        }

        firestore = FirebaseFirestore.getInstance()

        try {
            firestore.useEmulator("10.0.2.2", 8080)
        } catch (e: IllegalStateException) {
            android.util.Log.d("TaskIntegrationTest", "Emulator ya configurado")
        }

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()
        firestore.firestoreSettings = settings

        // 2. Room en memoria
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        taskDao = database.taskDao()
        calendarDao = database.calendarDao()
        categoryDao = database.categoryDao()

        // 3. Crear datos base en Room (Calendar y Category)
        testCalendar = Calendar(
            id = "calendar123",
            name = "Test Calendar",
            code = "TEST",
            owners = listOf(testUserId),
            isShared = true,
            syncStatus = 1
        )

        testCategory = Category(
            id = "category123",
            name = "Work",
            color = "#FF5733",
            parentCalendarId = testCalendar.id,
            syncStatus = 1
        )

        // Insertar en Room para respetar Foreign Keys
        // (si tu DB tiene FK constraints)
        // calendarDao.insertCalendar(testCalendar)
        // categoryDao.insertCategory(testCategory)

        // 4. Servicios
        remoteServices = TaskRemoteServices(firestore)

        // 5. WorkManager
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        workManager = WorkManager.getInstance(context)

        repository = TaskRepository(taskDao, workManager)
    }

    @After
    fun teardown() = runTest {
        // Limpiar Firestore
        if (::firestore.isInitialized) {
            val tasks = firestore.collection("tasks").get().await()
            tasks.documents.forEach { doc -> doc.reference.delete().await() }
        }

        // Cerrar Room
        if (::database.isInitialized) {
            database.close()
        }
    }

    // ========== HELPER ==========

    private suspend fun executeWorker(calendarId: String = testCalendar.id): ListenableWorker.Result {
        val worker = TestListenableWorkerBuilder<TaskWorker>(context)
            .setInputData(workDataOf(TaskWorker.KEY_CALENDAR_ID to calendarId))
            .setWorkerFactory(TestWorkerFactory(taskDao, remoteServices))
            .build()

        return worker.doWork()
    }

    // ========== HELPER: Crear tasks ==========

    private fun createTask(
        name: String = "Test Task",
        categoryId: String? = null,   // ⬅️ Nullable: Task puede no tener categoría
        calendarId: String = testCalendar.id,
        syncStatus: Int = 0
    ) = Task(
        id = UUID.randomUUID().toString(),
        name = name,
        parentCalendarId = calendarId,
        categoryId = categoryId,
        owners = listOf(testUserId),
        syncStatus = syncStatus
    )

    // ========== TEST 1: Task SIN categoría - Save & Sync ==========

    @Test
    fun saveTask_withoutCategory_syncedToFirebase() = runTest {
        // 1. Crear tarea SIN categoría
        val task = createTask(
            name = "Simple Task",
            categoryId = null  // ⬅️ Sin categoría
        )

        repository.saveTask(task, isSharedCalendar = true)

        // 2. Verificar en Room (syncStatus = 0)
        val localTask = taskDao.getTaskById(task.id)
        assertNotNull(localTask)
        assertEquals(0, localTask.syncStatus)
        assertNull(localTask.categoryId)  // Sin categoría

        // 3. Ejecutar Worker
        val result = executeWorker()
        assertEquals(ListenableWorker.Result.success(), result)

        // 4. Verificar en Firebase
        val firestoreDoc = firestore.collection("tasks")
            .document(task.id)
            .get()
            .await()

        assertTrue(firestoreDoc.exists())
        assertEquals("Simple Task", firestoreDoc.getString("name"))
        assertNull(firestoreDoc.getString("categoryId"))

        // 5. Verificar syncStatus = 1 en Room
        val syncedTask = taskDao.getTaskById(task.id)
        assertEquals(1, syncedTask?.syncStatus)
    }

    // ========== TEST 2: Task CON categoría - Save & Sync ==========

    @Test
    fun saveTask_withCategory_syncedToFirebase() = runTest {
        // 1. Crear tarea CON categoría
        val task = createTask(
            name = "Work Task",
            categoryId = testCategory.id  // ⬅️ Con categoría
        )

        repository.saveTask(task, isSharedCalendar = true)

        // 2. Verificar en Room
        val localTask = taskDao.getTaskById(task.id)
        assertNotNull(localTask)
        assertEquals(0, localTask.syncStatus)
        assertEquals(testCategory.id, localTask.categoryId)

        // 3. Ejecutar Worker
        val result = executeWorker()
        assertEquals(ListenableWorker.Result.success(), result)

        // 4. Verificar en Firebase
        val firestoreDoc = firestore.collection("tasks")
            .document(task.id)
            .get()
            .await()

        assertTrue(firestoreDoc.exists())
        assertEquals("Work Task", firestoreDoc.getString("name"))
        assertEquals(testCategory.id, firestoreDoc.getString("categoryId"))

        // 5. Verificar sincronizado en Room
        val syncedTask = taskDao.getTaskById(task.id)
        assertEquals(1, syncedTask?.syncStatus)
        assertEquals(testCategory.id, syncedTask?.categoryId)
    }

    // ========== TEST 3: Task descargada de Firebase SIN categoría ==========

    @Test
    fun createInFirebase_taskWithoutCategory_downloadedToRoom() = runTest {
        // 1. Crear tarea en Firebase SIN categoría
        val taskId = UUID.randomUUID().toString()
        val taskData = hashMapOf(
            "id" to taskId,
            "name" to "Remote Task No Category",
            "parentCalendarId" to testCalendar.id,
            "categoryId" to null,
            "owners" to listOf(testUserId),
            "syncStatus" to 1
        )

        firestore.collection("tasks")
            .document(taskId)
            .set(taskData)
            .await()

        // 2. Verificar que NO está en Room
        assertNull(taskDao.getTaskById(taskId))

        // 3. Ejecutar Worker
        val result = executeWorker()
        assertEquals(ListenableWorker.Result.success(), result)

        // 4. Verificar descargada en Room
        val localTask = taskDao.getTaskById(taskId)
        assertNotNull(localTask)
        assertEquals("Remote Task No Category", localTask.name)
        assertNull(localTask.categoryId)
        assertEquals(1, localTask.syncStatus)
    }

    // ========== TEST 4: Task descargada de Firebase CON categoría ==========

    @Test
    fun createInFirebase_taskWithCategory_downloadedToRoom() = runTest {
        // 1. Crear tarea en Firebase CON categoría
        val taskId = UUID.randomUUID().toString()
        val taskData = hashMapOf(
            "id" to taskId,
            "name" to "Remote Task With Category",
            "parentCalendarId" to testCalendar.id,
            "categoryId" to testCategory.id,
            "owners" to listOf(testUserId),
            "syncStatus" to 1
        )

        firestore.collection("tasks")
            .document(taskId)
            .set(taskData)
            .await()

        // 2. Verificar que NO está en Room
        assertNull(taskDao.getTaskById(taskId))

        // 3. Ejecutar Worker
        val result = executeWorker()
        assertEquals(ListenableWorker.Result.success(), result)

        // 4. Verificar descargada en Room CON categoría
        val localTask = taskDao.getTaskById(taskId)
        assertNotNull(localTask)
        assertEquals("Remote Task With Category", localTask.name)
        assertEquals(testCategory.id, localTask.categoryId)
        assertEquals(1, localTask.syncStatus)
    }

    // ========== TEST 5: Update task (cambia de sin categoría a con categoría) ==========

    @Test
    fun updateTask_addCategory_syncedToFirebase() = runTest {
        // 1. Crear tarea SIN categoría y sincronizar
        val task = createTask(
            name = "Task Without Category",
            categoryId = null
        )

        repository.saveTask(task, isSharedCalendar = true)
        executeWorker()

        // Verificar en Firebase sin categoría
        val initialDoc = firestore.collection("tasks")
            .document(task.id)
            .get()
            .await()
        assertNull(initialDoc.getString("categoryId"))

        // 2. Actualizar tarea AÑADIENDO categoría
        val updatedTask = task.copy(
            name = "Task With Category Now",
            categoryId = testCategory.id  // ⬅️ Añadir categoría
        )

        repository.updateTask(updatedTask)

        // Verificar pendiente en Room
        assertEquals(0, taskDao.getTaskById(task.id)?.syncStatus)

        // 3. Ejecutar Worker
        val result = executeWorker()
        assertEquals(ListenableWorker.Result.success(), result)

        // 4. Verificar en Firebase con categoría
        val updatedDoc = firestore.collection("tasks")
            .document(task.id)
            .get()
            .await()

        assertEquals("Task With Category Now", updatedDoc.getString("name"))
        assertEquals(testCategory.id, updatedDoc.getString("categoryId"))

        // 5. Verificar en Room
        val localTask = taskDao.getTaskById(task.id)
        assertEquals(1, localTask?.syncStatus)
        assertEquals(testCategory.id, localTask?.categoryId)
    }

    // ========== TEST 6: Update task (cambia de con categoría a sin categoría) ==========

    @Test
    fun updateTask_removeCategory_syncedToFirebase() = runTest {
        // 1. Crear tarea CON categoría y sincronizar
        val task = createTask(
            name = "Task With Category",
            categoryId = testCategory.id
        )

        repository.saveTask(task, isSharedCalendar = true)
        executeWorker()

        val initialDoc = firestore.collection("tasks")
            .document(task.id)
            .get()
            .await()
        assertEquals(testCategory.id, initialDoc.getString("categoryId"))


        // 2. Actualizar QUITANDO categoría
        val updatedTask = task.copy(
            name = "Task Without Category Now",
            categoryId = null  // ⬅️ Quitar categoría
        )

        repository.updateTask(updatedTask)

        // 3. Ejecutar Worker
        val result = executeWorker()
        assertEquals(ListenableWorker.Result.success(), result)

        // 4. Verificar en Firebase sin categoría
        val updatedDoc = firestore.collection("tasks")
            .document(task.id)
            .get()
            .await()

        assertEquals("Task Without Category Now", updatedDoc.getString("name"))
        assertTrue(!updatedDoc.contains("categoryId"))

        val localTask = taskDao.getTaskById(task.id)
        assertEquals(1, localTask?.syncStatus)
        assertNull(localTask?.categoryId)
    }

    // ========== TEST 7: Delete task (shared = true) ==========

    @Test
    fun deleteTask_shared_removedFromFirebase() = runTest {
        // 1. Crear y sincronizar tarea
        val task = createTask(
            name = "Task To Delete",
            categoryId = testCategory.id
        )

        repository.saveTask(task, isSharedCalendar = true)
        executeWorker()

        // Verificar que existe en Firebase
        assertTrue(
            firestore.collection("tasks")
                .document(task.id)
                .get()
                .await()
                .exists()
        )

        // 2. Soft delete
        repository.deleteTask(task.id, isShared = true)
        assertEquals(2, taskDao.getTaskById(task.id)?.syncStatus)

        // 3. Ejecutar Worker
        val result = executeWorker()
        assertEquals(ListenableWorker.Result.success(), result)

        // 4. Verificar eliminada de Firebase
        assertTrue(
            !firestore.collection("tasks")
                .document(task.id)
                .get()
                .await()
                .exists()
        )

        // 5. Verificar eliminada de Room
        assertNull(taskDao.getTaskById(task.id))
    }

    // ========== TEST 8: Delete task (shared = false) ==========

    @Test
    fun deleteTask_notShared_onlyDeletedFromRoom() = runTest {
        // 1. Crear tarea local (no compartida, sin sync)
        val task = createTask(name = "Private Task")
        repository.saveTask(task, isSharedCalendar = false)

        // Verificar en Room
        assertNotNull(taskDao.getTaskById(task.id))

        // 2. Hard delete directo
        repository.deleteTask(task.id, isShared = false)

        // 3. Verificar eliminada de Room inmediatamente
        assertNull(taskDao.getTaskById(task.id))

        // 4. Verificar que NUNCA estuvo en Firebase
        assertTrue(
            !firestore.collection("tasks")
                .document(task.id)
                .get()
                .await()
                .exists()
        )
    }

    // ========== TEST 9: Múltiples tasks con y sin categoría ==========

    @Test
    fun multipleTasks_withAndWithoutCategory_allSynced() = runTest {
        // 1. Crear mix de tareas
        val tasksWithCategory = listOf(
            createTask(name = "Work Task 1", categoryId = testCategory.id),
            createTask(name = "Work Task 2", categoryId = testCategory.id)
        )
        val tasksWithoutCategory = listOf(
            createTask(name = "Simple Task 1", categoryId = null),
            createTask(name = "Simple Task 2", categoryId = null)
        )

        // 2. Guardar todas
        tasksWithCategory.forEach { repository.saveTask(it, isSharedCalendar = true) }
        tasksWithoutCategory.forEach { repository.saveTask(it, isSharedCalendar = true) }

        // Verificar todas pendientes
        (tasksWithCategory + tasksWithoutCategory).forEach { task ->
            assertEquals(0, taskDao.getTaskById(task.id)?.syncStatus)
        }

        // 3. Ejecutar Worker
        val result = executeWorker()
        assertEquals(ListenableWorker.Result.success(), result)

        // 4. Verificar todas en Firebase
        val remoteTasks = remoteServices.getAllTasksByCalendarIdRemote(testCalendar.id)
        assertEquals(4, remoteTasks.size)

        val remoteWithCategory = remoteTasks.filter { it.categoryId != null }
        val remoteWithoutCategory = remoteTasks.filter { it.categoryId == null }
        assertEquals(2, remoteWithCategory.size)
        assertEquals(2, remoteWithoutCategory.size)

        // 5. Verificar todas sincronizadas en Room
        (tasksWithCategory + tasksWithoutCategory).forEach { task ->
            assertEquals(1, taskDao.getTaskById(task.id)?.syncStatus)
        }

        // 6. Verificar Flow del Repository
        val flowTasks = repository.getAllTaskByCalendarId(testCalendar.id).first()
        assertEquals(4, flowTasks.size)
    }

    // ========== TEST 10: Cleanup - Task eliminada en Firebase ==========

    @Test
    fun cleanup_taskDeletedInFirebase_removedFromRoom() = runTest {
        // 1. Crear y sincronizar tarea
        val task = createTask(
            name = "Task To Be Deleted Remotely",
            categoryId = testCategory.id
        )

        repository.saveTask(task, isSharedCalendar = true)
        executeWorker()

        // Verificar sincronizada
        assertEquals(1, taskDao.getTaskById(task.id)?.syncStatus)

        // 2. Otro usuario la elimina en Firebase
        firestore.collection("tasks")
            .document(task.id)
            .delete()
            .await()

        // 3. Ejecutar Worker
        val result = executeWorker()
        assertEquals(ListenableWorker.Result.success(), result)

        // 4. Verificar eliminada de Room
        assertNull(taskDao.getTaskById(task.id))
    }

    // ========== TEST 11: Conflicto - Local gana sobre Firebase ==========

    @Test
    fun conflict_localVersionWins() = runTest {
        val taskId = "conflict-task-id"

        // 1. Crear tarea en Firebase (versión remota)
        val remoteTaskData = hashMapOf(
            "id" to taskId,
            "name" to "Remote Version",
            "calendarId" to testCalendar.id,
            "categoryId" to null,
            "userId" to testUserId,
            "syncStatus" to 1
        )

        firestore.collection("tasks")
            .document(taskId)
            .set(remoteTaskData)
            .await()

        // 2. Crear misma task localmente (pendiente, syncStatus = 0)
        val localTask = Task(
            id = taskId,
            name = "Local Version",
            parentCalendarId = testCalendar.id,
            categoryId = testCategory.id, // ⬅️ Local tiene categoría, remota no
            owners = listOf(testUserId),
            syncStatus = 0
        )

        taskDao.insertTask(localTask)

        // 3. Ejecutar Worker
        val result = executeWorker()
        assertEquals(ListenableWorker.Result.success(), result)

        // 4. Verificar: Local GANA (syncStatus = 0 tiene prioridad)
        val localFinal = taskDao.getTaskById(taskId)
        assertEquals("Local Version", localFinal?.name)
        assertEquals(testCategory.id, localFinal?.categoryId)

        // 5. Verificar que la versión local se subió a Firebase
        val firestoreDoc = firestore.collection("tasks")
            .document(taskId)
            .get()
            .await()

        assertEquals("Local Version", firestoreDoc.getString("name"))
        assertEquals(testCategory.id, firestoreDoc.getString("categoryId"))
    }

    // ========== TEST 12: Flow reactivo ==========

    @Test
    fun flow_getAllTaskByCalendarId_reactsToChanges() = runTest {
        // 1. Crear tareas en Firebase
        val task1 = createTask(name = "Task 1", categoryId = testCategory.id)
        val task2 = createTask(name = "Task 2", categoryId = null)

        remoteServices.saveTaskRemote(task1)
        remoteServices.saveTaskRemote(task2)

        // 2. Descargar con Worker
        val result = executeWorker()
        assertEquals(ListenableWorker.Result.success(), result)

        // 3. Verificar Flow
        val tasks = repository.getAllTaskByCalendarId(testCalendar.id).first()
        assertEquals(2, tasks.size)
        assertTrue(tasks.any { it.name == "Task 1" && it.categoryId == testCategory.id })
        assertTrue(tasks.any { it.name == "Task 2" && it.categoryId == null })
    }

    // ========== WorkerFactory ==========

    private class TestWorkerFactory(
        private val taskDao: TaskDao,
        private val remoteServices: TaskRemoteServices
    ) : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker? {
            return when (workerClassName) {
                TaskWorker::class.java.name -> {
                    TaskWorker(appContext, workerParameters, taskDao, remoteServices)
                }
                else -> null
            }
        }
    }
}