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
import com.asistente.core.data.local.daos.CategoryDao
import com.asistente.core.data.remote.CategoryRemoteServices
import com.asistente.core.data.repository.CategoryRepository
import com.asistente.core.data.worker.CategoryWorker
import com.asistente.core.domain.models.Category
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
class CategoryIntegration {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var firestore: FirebaseFirestore
    private lateinit var remoteServices: CategoryRemoteServices
    private lateinit var repository: CategoryRepository
    private lateinit var workManager: WorkManager

    private val testCalendarId = "calendar123"

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        // 1. Firebase con proyecto real
        if (FirebaseApp.getApps(context).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setProjectId("trabajo-fin-de-grado-40a05")
                .setApplicationId("1:756327291572:android:8a6ccc0e61bd8bdc7915e1")
                .setApiKey("AIzaSyAdtteXH9fPRuUkef6nnoegunZ7KN9T3Fw")
                .build()
            FirebaseApp.initializeApp(context, options)
        }

        firestore = FirebaseFirestore.getInstance()

        // Configurar emulador
        try {
            firestore.useEmulator("10.0.2.2", 8080)
        } catch (e: IllegalStateException) {
            android.util.Log.d("Test", "Emulator ya configurado")
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

        categoryDao = database.categoryDao()
        remoteServices = CategoryRemoteServices(firestore)

        // 3. WorkManager
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        workManager = WorkManager.getInstance(context)

        repository = CategoryRepository(categoryDao, workManager)
    }

    @After
    fun teardown() = runTest {
        // Limpiar Firestore
        if (::firestore.isInitialized) {
            val categories = firestore.collection("categories")
                .get()
                .await()

            categories.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        }

        // Cerrar Room
        if (::database.isInitialized) {
            database.close()
        }
    }

    // ========== HELPER ==========

    private suspend fun executeWorker(): ListenableWorker.Result {
        val worker = TestListenableWorkerBuilder<CategoryWorker>(context)
            .setInputData(workDataOf(CategoryWorker.KEY_CALENDAR_ID to testCalendarId))
            .setWorkerFactory(TestWorkerFactory(categoryDao, remoteServices))
            .build()

        return worker.doWork()
    }

    // ========== TESTS ==========

    @Test
    fun saveCategory_syncedToFirebase() = runTest {
        // 1. Crear categoría localmente
        val category = Category(
            id = UUID.randomUUID().toString(),
            name = "Work",
            color = "#FF5733",
            parentCalendarId = testCalendarId,
            syncStatus = 0
        )

        repository.saveCategory(category)

        // 2. Verificar en Room (syncStatus = 0, pendiente)
        val localCategory = categoryDao.getCategoryById(category.id)
        assertNotNull(localCategory)
        assertEquals(0, localCategory.syncStatus)
        assertEquals("Work", localCategory.name)

        // 3. Ejecutar Worker (sincronizar)
        val workerResult = executeWorker()
        assertEquals(ListenableWorker.Result.success(), workerResult)

        // 4. Verificar en Firebase
        val firestoreDoc = firestore.collection("categories")
            .document(category.id)
            .get()
            .await()

        assertTrue(firestoreDoc.exists())
        assertEquals("Work", firestoreDoc.getString("name"))
        assertEquals("#FF5733", firestoreDoc.getString("color"))

        // 5. Verificar en Room (syncStatus = 1, sincronizado)
        val updatedLocal = categoryDao.getCategoryById(category.id)
        assertEquals(1, updatedLocal?.syncStatus)
    }

    @Test
    fun createInFirebase_downloadedToRoom() = runTest {
        // 1. Crear categoría en Firebase
        val categoryId = UUID.randomUUID().toString()
        val categoryData = hashMapOf(
            "id" to categoryId,
            "name" to "Personal",
            "color" to "#33FF57",
            "parentCalendarId" to testCalendarId,
            "syncStatus" to 1
        )

        firestore.collection("categories")
            .document(categoryId)
            .set(categoryData)
            .await()

        // 2. Verificar que NO está en Room
        val beforeSync = categoryDao.getCategoryById(categoryId)
        assertNull(beforeSync)

        // 3. Ejecutar Worker (descargar)
        val workerResult = executeWorker()
        assertEquals(ListenableWorker.Result.success(), workerResult)

        // 4. Verificar que SE descargó a Room
        val afterSync = categoryDao.getCategoryById(categoryId)
        assertNotNull(afterSync)
        assertEquals("Personal", afterSync.name)
        assertEquals("#33FF57", afterSync.color)
        assertEquals(1, afterSync.syncStatus)

        // 5. Verificar en Flow del Repository
        val categories = repository.getAllCategoryByCalendarId(testCalendarId).first()
        assertTrue(categories.any { it.id == categoryId })
    }

    @Test
    fun updateCategory_syncedToFirebase() = runTest {
        // 1. Crear y sincronizar categoría inicial
        val category = Category(
            id = UUID.randomUUID().toString(),
            name = "Original Name",
            color = "#FF5733",
            parentCalendarId = testCalendarId,
            syncStatus = 0
        )

        repository.saveCategory(category)
        executeWorker() // Primera sincronización

        // Verificar que está en Firebase
        val initialDoc = firestore.collection("categories")
            .document(category.id)
            .get()
            .await()
        assertEquals("Original Name", initialDoc.getString("name"))

        // 2. Actualizar localmente
        val updated = category.copy(name = "Updated Name")
        repository.updateCategory(updated)

        // Verificar que está pendiente
        val localAfterUpdate = categoryDao.getCategoryById(category.id)
        assertEquals(0, localAfterUpdate?.syncStatus)

        // 3. Ejecutar Worker (sincronizar actualización)
        val workerResult = executeWorker()
        assertEquals(ListenableWorker.Result.success(), workerResult)

        // 4. Verificar en Firebase
        val updatedDoc = firestore.collection("categories")
            .document(category.id)
            .get()
            .await()

        assertEquals("Updated Name", updatedDoc.getString("name"))

        // 5. Verificar en Room
        val localAfterSync = categoryDao.getCategoryById(category.id)
        assertEquals(1, localAfterSync?.syncStatus)
    }

    @Test
    fun deleteCategory_removedFromFirebase() = runTest {
        // 1. Crear y sincronizar categoría
        val category = Category(
            id = UUID.randomUUID().toString(),
            name = "To Delete",
            color = "#FF5733",
            parentCalendarId = testCalendarId,
            syncStatus = 0
        )

        repository.saveCategory(category)
        executeWorker() // Sincronizar

        // Verificar que existe en Firebase
        val beforeDelete = firestore.collection("categories")
            .document(category.id)
            .get()
            .await()
        assertTrue(beforeDelete.exists())

        // 2. Eliminar (soft delete)
        repository.deleteCategory(category.id, isShared = true)

        // Verificar syncStatus = 2
        val localAfterDelete = categoryDao.getCategoryById(category.id)
        assertEquals(2, localAfterDelete?.syncStatus)

        // 3. Ejecutar Worker (sincronizar eliminación)
        val workerResult = executeWorker()
        assertEquals(ListenableWorker.Result.success(), workerResult)

        // 4. Verificar que se eliminó de Firebase
        val afterDelete = firestore.collection("categories")
            .document(category.id)
            .get()
            .await()
        assertTrue(!afterDelete.exists())

        // 5. Verificar que se eliminó de Room (hard delete)
        val localFinal = categoryDao.getCategoryById(category.id)
        assertNull(localFinal)
    }

    @Test
    fun multipleCategories_allSynced() = runTest {
        // 1. Crear múltiples categorías
        val categories = listOf(
            Category(
                id = UUID.randomUUID().toString(),
                name = "Work",
                color = "#FF5733",
                parentCalendarId = testCalendarId,
                syncStatus = 0
            ),
            Category(
                id = UUID.randomUUID().toString(),
                name = "Personal",
                color = "#33FF57",
                parentCalendarId = testCalendarId,
                syncStatus = 0
            ),
            Category(
                id = UUID.randomUUID().toString(),
                name = "Health",
                color = "#3357FF",
                parentCalendarId = testCalendarId,
                syncStatus = 0
            )
        )

        // 2. Guardar todas
        categories.forEach { category ->
            repository.saveCategory(category)
        }

        // Verificar que todas están pendientes
        categories.forEach { category ->
            val local = categoryDao.getCategoryById(category.id)
            assertEquals(0, local?.syncStatus)
        }

        // 3. Ejecutar Worker (sincronizar todas)
        val workerResult = executeWorker()
        assertEquals(ListenableWorker.Result.success(), workerResult)

        // 4. Verificar en Firebase
        val remoteCategories = remoteServices.getAllCategorysByCalendarIdRemote(testCalendarId)
        assertEquals(3, remoteCategories.size)

        // 5. Verificar en Room
        categories.forEach { category ->
            val local = categoryDao.getCategoryById(category.id)
            assertEquals(1, local?.syncStatus)
        }

        // 6. Verificar en Flow
        val flowCategories = repository.getAllCategoryByCalendarId(testCalendarId).first()
        assertEquals(3, flowCategories.size)
    }

    @Test
    fun conflictResolution_localWins() = runTest {
        // 1. Crear categoría en Firebase (usuario remoto)
        val remoteCategory = Category(
            id = "conflict-id",
            name = "Remote Version",
            color = "#FF5733",
            parentCalendarId = testCalendarId,
            syncStatus = 1
        )

        firestore.collection("categories")
            .document(remoteCategory.id)
            .set(remoteCategory)
            .await()

        // 2. Crear misma categoría localmente (usuario local, offline)
        val localCategory = Category(
            id = "conflict-id",
            name = "Local Version",
            color = "#33FF57",
            parentCalendarId = testCalendarId,
            syncStatus = 0 // Pendiente
        )

        categoryDao.insertCategory(localCategory)

        // 3. Ejecutar Worker
        val workerResult = executeWorker()
        assertEquals(ListenableWorker.Result.success(), workerResult)

        // 4. Verificar: Local debe GANAR (syncStatus=0 tiene prioridad)
        val localFinal = categoryDao.getCategoryById("conflict-id")
        assertEquals("Local Version", localFinal?.name)

        // 5. Verificar en Firebase: Local se subió
        val firestoreDoc = firestore.collection("categories")
            .document("conflict-id")
            .get()
            .await()

        assertEquals("Local Version", firestoreDoc.getString("name"))
    }

    @Test
    fun cleanupDeletedInFirebase() = runTest {
        // 1. Crear categoría y sincronizar
        val category = Category(
            id = UUID.randomUUID().toString(),
            name = "Shared Category",
            color = "#FF5733",
            parentCalendarId = testCalendarId,
            syncStatus = 0
        )

        repository.saveCategory(category)
        executeWorker() // Sincronizar

        // Verificar que está sincronizada
        val syncedLocal = categoryDao.getCategoryById(category.id)
        assertEquals(1, syncedLocal?.syncStatus)

        // 2. Otro usuario la elimina de Firebase
        firestore.collection("categories")
            .document(category.id)
            .delete()
            .await()

        // 3. Ejecutar Worker (detecta eliminación)
        val workerResult = executeWorker()
        assertEquals(ListenableWorker.Result.success(), workerResult)

        // 4. Verificar que se eliminó de Room
        val localAfterCleanup = categoryDao.getCategoryById(category.id)
        assertNull(localAfterCleanup)
    }

    // ========== WorkerFactory ==========

    private class TestWorkerFactory(
        private val categoryDao: CategoryDao,
        private val remoteServices: CategoryRemoteServices
    ) : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker? {
            return when (workerClassName) {
                CategoryWorker::class.java.name -> {
                    CategoryWorker(appContext, workerParameters, categoryDao, remoteServices)
                }
                else -> null
            }
        }
    }
}