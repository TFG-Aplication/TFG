package com.asistente.core.data.repository

import androidx.work.*
import com.asistente.core.data.local.daos.CategoryDao
import com.asistente.core.data.worker.CategoryWorker
import com.asistente.core.domain.models.Category
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
class CategoryRepositoryTest {

    @Mock
    private lateinit var categoryDao: CategoryDao

    @Mock
    private lateinit var workManager: WorkManager

    private lateinit var repository: CategoryRepository

    private val testCalendarId = "calendar123"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = CategoryRepository(categoryDao, workManager)
    }

    // ========== TEST 1: getCategoryById ==========

    @Test
    fun getCategoryById() = runTest {
        val categoryId = UUID.randomUUID().toString()
        val expectedCategory = Category(
            id = categoryId,
            name = "Work",
            color = "#FF5733",
            parentCalendarId = testCalendarId,
            syncStatus = 1
        )

        whenever(categoryDao.getCategoryById(categoryId)).thenReturn(expectedCategory)

        val result = repository.getCategoryById(categoryId)

        assertNotNull(result)
        assertEquals("Work", result.name)
        assertEquals("#FF5733", result.color)
        verify(categoryDao).getCategoryById(categoryId)
    }

    @Test
    fun getCategoryByIdReturnsNullWhenDoesNotExist() = runTest {
        val categoryId = UUID.randomUUID().toString()
        whenever(categoryDao.getCategoryById(categoryId)).thenReturn(null)

        val result = repository.getCategoryById(categoryId)

        assertNull(result)
        verify(categoryDao).getCategoryById(categoryId)
    }

    // ========== TEST 2: getAllCategoryByCalendarId ==========

    @Test
    fun getAllCategoryByCalendarId() = runTest {
        val categories = listOf(
            Category(
                id = UUID.randomUUID().toString(),
                name = "Work",
                color = "#FF5733",
                parentCalendarId = testCalendarId,
                syncStatus = 1
            ),
            Category(
                id = UUID.randomUUID().toString(),
                name = "Personal",
                color = "#33FF57",
                parentCalendarId = testCalendarId,
                syncStatus = 1
            )
        )

        whenever(categoryDao.getAllCategorysByCalendarIdFlow(testCalendarId))
            .thenReturn(flowOf(categories))

        val result = repository.getAllCategoryByCalendarId(testCalendarId).first()

        assertEquals(2, result.size)
        assertEquals("Work", result[0].name)
        assertEquals("Personal", result[1].name)
        verify(categoryDao).getAllCategorysByCalendarIdFlow(testCalendarId)
    }

    // ========== TEST 3: saveCategory ==========

    @Test
    fun saveCategory() = runTest {
        val category = Category(
            id = UUID.randomUUID().toString(),
            name = "Work",
            color = "#FF5733",
            parentCalendarId = testCalendarId,
            syncStatus = 1
        )

        repository.saveCategory(category)

        // Verificar que se guardó con syncStatus = 0
        verify(categoryDao).insertCategory(category.copy(syncStatus = 0))

        // Verificar que se encoló el worker
        verify(workManager).enqueueUniqueWork(
            eq("sync_category_$testCalendarId"),
            eq(ExistingWorkPolicy.REPLACE),
            any<OneTimeWorkRequest>()
        )
    }

    // ========== TEST 4: updateCategory ==========

    @Test
    fun updateCategory() = runTest {
        val category = Category(
            id = UUID.randomUUID().toString(),
            name = "Updated Work",
            color = "#FF5733",
            parentCalendarId = testCalendarId,
        )

        repository.updateCategory(category)

        verify(categoryDao).updateCategory(category.copy(syncStatus = 0))
        verify(workManager).enqueueUniqueWork(
            eq("sync_category_$testCalendarId"),
            eq(ExistingWorkPolicy.REPLACE),
            any<OneTimeWorkRequest>()
        )
    }

    // ========== TEST 5: deleteCategory (shared = true) ==========

    @Test
    fun deleteCategory() = runTest {
        val categoryId = UUID.randomUUID().toString()
        val category = Category(
            id = categoryId,
            name = "To Delete",
            color = "#FF5733",
            parentCalendarId = testCalendarId,
            syncStatus = 1
        )

        whenever(categoryDao.getCategoryById(categoryId)).thenReturn(category)

        repository.deleteCategory(categoryId, isShared = true)

        verify(categoryDao).getCategoryById(categoryId)
        verify(categoryDao).insertCategory(category.copy(syncStatus = 2))
        verify(workManager).enqueueUniqueWork(
            eq("sync_category_$testCalendarId"),
            eq(ExistingWorkPolicy.REPLACE),
            any<OneTimeWorkRequest>()
        )
    }

    @Test
    fun deleteCategoryWhenCategoryNotFoundDoesNothing() = runTest {
        val categoryId = UUID.randomUUID().toString()
        whenever(categoryDao.getCategoryById(categoryId)).thenReturn(null)

        repository.deleteCategory(categoryId, isShared = true)

        verify(categoryDao).getCategoryById(categoryId)
        verify(categoryDao, never()).insertCategory(any())
        verify(workManager, never()).enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>())
    }

    // ========== TEST 6: deleteCategory (shared = false) ==========

    @Test
    fun deleteCategoryWithSharedFalse() = runTest {
        val categoryId = UUID.randomUUID().toString()

        repository.deleteCategory(categoryId, isShared = false)

        verify(categoryDao).deleteCategoryById(categoryId)
        verify(categoryDao, never()).getCategoryById(any())
        verify(categoryDao, never()).insertCategory(any())
        verify(workManager, never()).enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>())
    }

    // ========== TEST 7: Worker configuration ==========

    @Test
    fun workerRequestHasCorrectConstraintsAndInputData() = runTest {
        val category = Category(
            id = UUID.randomUUID().toString(),
            name = "Work",
            color = "#FF5733",
            parentCalendarId = testCalendarId,
            syncStatus = 0
        )

        repository.saveCategory(category)

        val nameCaptor = argumentCaptor<String>()
        val policyCaptor = argumentCaptor<ExistingWorkPolicy>()
        val requestCaptor = argumentCaptor<OneTimeWorkRequest>()

        verify(workManager).enqueueUniqueWork(
            nameCaptor.capture(),
            policyCaptor.capture(),
            requestCaptor.capture()
        )

        assertEquals("sync_category_$testCalendarId", nameCaptor.firstValue)
        assertEquals(ExistingWorkPolicy.REPLACE, policyCaptor.firstValue)

        val workRequest = requestCaptor.firstValue
        assertEquals(NetworkType.CONNECTED, workRequest.workSpec.constraints.requiredNetworkType)
        assertEquals(testCalendarId, workRequest.workSpec.input.getString(CategoryWorker.KEY_CALENDAR_ID))
        assertEquals(BackoffPolicy.EXPONENTIAL, workRequest.workSpec.backoffPolicy)
    }

    // ========== TEST 8: Multiple operations ==========

    @Test
    fun multipleOperationsWithSameCalendar() = runTest {
        val cat1 = Category(
            id = UUID.randomUUID().toString(),
            name = "Work",
            color = "#FF5733",
            parentCalendarId = testCalendarId,
            syncStatus = 0
        )
        val cat2 = Category(
            id = UUID.randomUUID().toString(),
            name = "Personal",
            color = "#33FF57",
            parentCalendarId = testCalendarId,
            syncStatus = 0
        )

        repository.saveCategory(cat1)
        repository.saveCategory(cat2)

        verify(workManager, times(2)).enqueueUniqueWork(
            eq("sync_category_$testCalendarId"),
            eq(ExistingWorkPolicy.REPLACE),
            any<OneTimeWorkRequest>()
        )
    }
}
