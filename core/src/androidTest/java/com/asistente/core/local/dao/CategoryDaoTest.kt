package com.asistente.core.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asistente.core.data.local.AppDatabase
import com.asistente.core.data.local.daos.CalendarDao
import com.asistente.core.data.local.daos.CategoryDao
import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.models.Category
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
class CategoryDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var calendarDao: CalendarDao

    private lateinit var testCalendarId: String

    @Before
    fun setup() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        categoryDao = database.categoryDao()
        calendarDao = database.calendarDao()

        // Crear un calendario de prueba
        val calendar = Calendar(
            id = UUID.randomUUID().toString(),
            name = "Test Calendar",
            code = "TEST",
            owners = listOf("user1")
        )
        calendarDao.insertCalendar(calendar)
        testCalendarId = calendar.id
    }

    @After
    fun teardown() {
        database.close()
    }

    // ========== TEST 1: Inserción y recuperación básica ==========
    @Test
    fun insertCategory_canBeRetrievedById() = runTest {
        // Arrange
        val category = Category(
            id = UUID.randomUUID().toString(),
            name = "Work",
            parentCalendarId = testCalendarId,
            color = "#FF5733",
            syncStatus = 0
        )

        // Act
        categoryDao.insertCategory(category)
        val retrieved = categoryDao.getCategoryById(category.id)

        // Assert
        assertNotNull(retrieved)
        assertEquals("Work", retrieved.name)
        assertEquals(testCalendarId, retrieved.parentCalendarId)
        assertEquals("#FF5733", retrieved.color)
    }

    // ========== TEST 2: Flow reactivo con getAllCategorysByCalendarIdFlow ==========
    @Test
    fun getAllCategorysByCalendarIdFlow_returnsCorrectCategories() = runTest {
        // Arrange
        val cat1 = Category(
            id = UUID.randomUUID().toString(),
            name = "Work",
            parentCalendarId = testCalendarId,
            color = "#FF0000"
        )
        val cat2 = Category(
            id = UUID.randomUUID().toString(),
            name = "Personal",
            parentCalendarId = testCalendarId,
            color = "#00FF00"
        )
        val cat3 = Category(
            id = UUID.randomUUID().toString(),
            name = "Other Calendar Category",
            parentCalendarId = "different-calendar-id",
            color = "#0000FF"
        )

        // Act
        categoryDao.insertCategory(cat1)
        categoryDao.insertCategory(cat2)
        categoryDao.insertCategory(cat3)

        val categories = categoryDao.getAllCategorysByCalendarIdFlow(testCalendarId).first()

        // Assert
        assertEquals(2, categories.size)
        assertTrue(categories.any { it.name == "Work" })
        assertTrue(categories.any { it.name == "Personal" })
        assertTrue(categories.none { it.name == "Other Calendar Category" })
    }

    // ========== TEST 3: Flow con getCategoryByIdFlow ==========
    @Test
    fun getCategoryByIdFlow_returnsCorrectCategory() = runTest {
        val category = Category(
            id = "test-cat-123",
            name = "My Category",
            parentCalendarId = testCalendarId,
            color = "#FFFFFF"
        )

        categoryDao.insertCategory(category)

        val retrieved = categoryDao.getCategoryByIdFlow("test-cat-123").first()

        assertNotNull(retrieved)
        assertEquals("My Category", retrieved.name)
    }

    // ========== TEST 4: Soft delete (syncStatus = 2) oculta categorías ==========
    @Test
    fun syncStatusDeleted_hidesCategoryFromQueries() = runTest {
        val category = Category(
            id = UUID.randomUUID().toString(),
            name = "ToDelete",
            parentCalendarId = testCalendarId,
            color = "#000000",
            syncStatus = 0
        )

        categoryDao.insertCategory(category)

        // Verifica que aparece inicialmente
        val allBefore = categoryDao.getAllCategorysByCalendarIdFlow(testCalendarId).first()
        assertEquals(1, allBefore.size)

        // Marca como eliminada (syncStatus = 2)
        val deleted = category.copy(syncStatus = 2)
        categoryDao.insertCategory(deleted) // REPLACE actualiza

        // Verifica que ya no aparece en queries con syncStatus != 2
        val allAfter = categoryDao.getAllCategorysByCalendarIdFlow(testCalendarId).first()
        assertEquals(0, allAfter.size)

        // Pero sigue existiendo con getCategoryById (no filtra syncStatus)
        val stillExists = categoryDao.getCategoryById(category.id)
        assertNotNull(stillExists)
        assertEquals(2, stillExists.syncStatus)
    }

    // ========== TEST 5: Update category ==========
    @Test
    fun updateCategory_changesAreReflected() = runTest {
        val category = Category(
            id = UUID.randomUUID().toString(),
            name = "Original Name",
            parentCalendarId = testCalendarId,
            color = "#111111"
        )

        categoryDao.insertCategory(category)

        // Update
        val updated = category.copy(name = "Updated Name", color = "#222222")
        categoryDao.updateCategory(updated)

        val retrieved = categoryDao.getCategoryById(category.id)

        assertNotNull(retrieved)
        assertEquals("Updated Name", retrieved.name)
        assertEquals("#222222", retrieved.color)
    }

    // ========== TEST 6: Hard delete ==========
    @Test
    fun deleteCategoryById_removesCategory() = runTest {
        val category = Category(
            id = UUID.randomUUID().toString(),
            name = "To Delete",
            parentCalendarId = testCalendarId,
            color = "#333333"
        )

        categoryDao.insertCategory(category)

        // Verifica que existe
        assertNotNull(categoryDao.getCategoryById(category.id))

        // Elimina
        categoryDao.deleteCategoryById(category.id)

        // Verifica que ya no existe
        assertNull(categoryDao.getCategoryById(category.id))
    }

    // ========== TEST 7: Get unsynced categories ==========
    @Test
    fun getUnsyncedCategories_returnsOnlyUnsyncedOnes() = runTest {
        val synced = Category(
            id = UUID.randomUUID().toString(),
            name = "Synced",
            parentCalendarId = testCalendarId,
            color = "#444444",
            syncStatus = 1
        )
        val unsynced = Category(
            id = UUID.randomUUID().toString(),
            name = "Unsynced",
            parentCalendarId = testCalendarId,
            color = "#555555",
            syncStatus = 0
        )

        categoryDao.insertCategory(synced)
        categoryDao.insertCategory(unsynced)

        val unsyncedCategories = categoryDao.getUnsyncedCategories(testCalendarId)

        assertEquals(1, unsyncedCategories.size)
        assertEquals("Unsynced", unsyncedCategories[0].name)
    }

    // ========== TEST 8: Get categories by sync status ==========
    @Test
    fun getCategoriesBySyncStatus_filtersCorrectly() = runTest {
        val cat0 = Category(
            id = UUID.randomUUID().toString(),
            name = "Status 0",
            parentCalendarId = testCalendarId,
            color = "#666666",
            syncStatus = 0
        )
        val cat1 = Category(
            id = UUID.randomUUID().toString(),
            name = "Status 1",
            parentCalendarId = testCalendarId,
            color = "#777777",
            syncStatus = 1
        )
        val cat2 = Category(
            id = UUID.randomUUID().toString(),
            name = "Status 2",
            parentCalendarId = testCalendarId,
            color = "#888888",
            syncStatus = 2
        )

        categoryDao.insertCategory(cat0)
        categoryDao.insertCategory(cat1)
        categoryDao.insertCategory(cat2)

        val status0 = categoryDao.getCategoriesBySyncStatus(0, testCalendarId)
        val status1 = categoryDao.getCategoriesBySyncStatus(1, testCalendarId)
        val status2 = categoryDao.getCategoriesBySyncStatus(2, testCalendarId)

        assertEquals(1, status0.size)
        assertEquals("Status 0", status0[0].name)

        assertEquals(1, status1.size)
        assertEquals("Status 1", status1[0].name)

        assertEquals(1, status2.size)
        assertEquals("Status 2", status2[0].name)
    }

    // ========== TEST 9: LIKE en queries funciona con calendarId exacto ==========
    @Test
    fun getUnsyncedCategories_likeQuery_worksWithExactMatch() = runTest {
        val category = Category(
            id = UUID.randomUUID().toString(),
            name = "Test",
            parentCalendarId = testCalendarId,
            color = "#999999",
            syncStatus = 0
        )

        categoryDao.insertCategory(category)

        // LIKE debería funcionar igual que = en este caso
        val result = categoryDao.getUnsyncedCategories(testCalendarId)

        assertEquals(1, result.size)
        assertEquals("Test", result[0].name)
    }

    // ========== TEST 10: Multiple categories con mismo calendarId ==========
    @Test
    fun getAllCategoriesByCalendarId_returnsMultipleCategories() = runTest {
        val cat1 = Category(
            id = UUID.randomUUID().toString(),
            name = "Cat 1",
            parentCalendarId = testCalendarId,
            color = "#AAAAAA"
        )
        val cat2 = Category(
            id = UUID.randomUUID().toString(),
            name = "Cat 2",
            parentCalendarId = testCalendarId,
            color = "#BBBBBB"
        )
        val cat3 = Category(
            id = UUID.randomUUID().toString(),
            name = "Cat 3",
            parentCalendarId = testCalendarId,
            color = "#CCCCCC"
        )

        categoryDao.insertCategory(cat1)
        categoryDao.insertCategory(cat2)
        categoryDao.insertCategory(cat3)

        val allCategories = categoryDao.getAllCategorysByCalendarId(testCalendarId)

        assertEquals(3, allCategories.size)
    }

    // ========== TEST 11: OnConflictStrategy.REPLACE funciona ==========
    @Test
    fun insertCategory_withSameId_replacesExisting() = runTest {
        val categoryId = UUID.randomUUID().toString()

        val original = Category(
            id = categoryId,
            name = "Original",
            parentCalendarId = testCalendarId,
            color = "#DDDDDD"
        )

        categoryDao.insertCategory(original)

        val replaced = Category(
            id = categoryId, // Mismo ID
            name = "Replaced",
            parentCalendarId = testCalendarId,
            color = "#EEEEEE"
        )

        categoryDao.insertCategory(replaced)

        val retrieved = categoryDao.getCategoryById(categoryId)

        assertNotNull(retrieved)
        assertEquals("Replaced", retrieved.name)
        assertEquals("#EEEEEE", retrieved.color)
    }

    // ========== TEST 12: Categories de diferentes calendarios no se mezclan ==========
    @Test
    fun categories_fromDifferentCalendars_areIsolated() = runTest {
        // Crear segundo calendario
        val calendar2 = Calendar(
            id = UUID.randomUUID().toString(),
            name = "Second Calendar",
            code = "CAL2",
            owners = listOf("user1")
        )
        calendarDao.insertCalendar(calendar2)

        // Categorías del primer calendario
        categoryDao.insertCategory(
            Category(
                id = UUID.randomUUID().toString(),
                name = "Cal1 Cat",
                parentCalendarId = testCalendarId,
                color = "#FFFFFF"
            )
        )

        // Categorías del segundo calendario
        categoryDao.insertCategory(
            Category(
                id = UUID.randomUUID().toString(),
                name = "Cal2 Cat",
                parentCalendarId = calendar2.id,
                color = "#000000"
            )
        )

        val cal1Categories = categoryDao.getAllCategorysByCalendarId(testCalendarId)
        val cal2Categories = categoryDao.getAllCategorysByCalendarId(calendar2.id)

        assertEquals(1, cal1Categories.size)
        assertEquals("Cal1 Cat", cal1Categories[0].name)

        assertEquals(1, cal2Categories.size)
        assertEquals("Cal2 Cat", cal2Categories[0].name)
    }
}