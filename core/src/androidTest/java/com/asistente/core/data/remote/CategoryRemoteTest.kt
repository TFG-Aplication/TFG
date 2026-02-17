package com.asistente.core.data.remote

import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.models.Category
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CategoryRemoteTest {

    @Mock
    private lateinit var firestore: FirebaseFirestore

    @Mock
    private lateinit var collectionReference: CollectionReference

    @Mock
    private lateinit var documentReference: DocumentReference

    @Mock
    private lateinit var query: Query

    @Mock
    private lateinit var querySnapshot: QuerySnapshot

    @Mock
    private lateinit var documentSnapshot: DocumentSnapshot

    private lateinit var remoteServices: CategoryRemoteServices

    private val testCalendarId = "calendar123"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Mock Firestore collection
        whenever(firestore.collection("categories")).thenReturn(collectionReference)

        remoteServices = CategoryRemoteServices(firestore)
    }

    // ========== TEST 1: getAllByUserIdRemote - Success ==========

    @Test
    fun getAllCategoryBycategoryId() = runTest {
        val categoryId = UUID.randomUUID().toString()
        val Category1 = Category(
            id = categoryId,
            name = "Work",
            color = "#FF5733",
            parentCalendarId = testCalendarId,
            syncStatus = 1
        )
        val Category2 = Category(
            id = categoryId,
            name = "Work",
            color = "#FF5733",
            parentCalendarId = testCalendarId,
            syncStatus = 1
        )

        val categories = listOf(Category1, Category2)
        whenever(collectionReference.whereEqualTo("parentCalendarId", testCalendarId))
            .thenReturn(query)
        whenever(query.get()).thenReturn(Tasks.forResult(querySnapshot))
        whenever(querySnapshot.toObjects(Category::class.java)).thenReturn(categories)


        val result = remoteServices.getAllCategorysByCalendarIdRemote(testCalendarId)

        assertEquals(2, result.size)
        assertEquals("Work", result[0].name)
        assertEquals("Work", result[1].name)

        verify(collectionReference).whereEqualTo("parentCalendarId", testCalendarId)
        verify(query).get()
    }

    @Test
    fun getAllCategoryByUserIdRemoteReturnsEmptyListOnException() = runTest {
        whenever(collectionReference.whereEqualTo("parentCalendarId", testCalendarId))
            .thenReturn(query)
        whenever(query.get()).thenReturn(Tasks.forException(Exception("Network error")))

        val result = remoteServices.getAllCategorysByCalendarIdRemote(testCalendarId)

        assertTrue(result.isEmpty())
        verify(collectionReference).whereEqualTo("parentCalendarId", testCalendarId)
    }

    // ========== TEST 2: getCalendarByIdRemote - Success ==========

    @Test
    fun getCategoryByIdRemoteReturnsCalendarSuccessfully() = runTest {
        val categoryId = UUID.randomUUID().toString()
        val expectedCategory = Category(
            id = categoryId,
            name = "Work",
            color = "#FF5733",
            parentCalendarId = testCalendarId,
            syncStatus = 1
        )

        whenever(collectionReference.document(categoryId)).thenReturn(documentReference)
        whenever(documentReference.get()).thenReturn(Tasks.forResult(documentSnapshot))
        whenever(documentSnapshot.toObject(Category::class.java)).thenReturn(expectedCategory)

        val result = remoteServices.getCategoryByIdRemote(categoryId)

        assertNotNull(result)
        assertEquals("Work", result.name)
        assertEquals(categoryId, result.id)

        verify(collectionReference).document(categoryId)
        verify(documentReference).get()
    }

    @Test
    fun getCategoryByIdRemoteReturnsNullWhenCategoryDoesNotExist() = runTest {
        val categoryId = UUID.randomUUID().toString()

        whenever(collectionReference.document(categoryId)).thenReturn(documentReference)
        whenever(documentReference.get()).thenReturn(Tasks.forResult(documentSnapshot))
        whenever(documentSnapshot.toObject(Category::class.java)).thenReturn(null)

        val result = remoteServices.getCategoryByIdRemote(categoryId)

        assertNull(result)
        verify(collectionReference).document(categoryId)
    }

    @Test
    fun getCategoryByIdRemoteReturnsNullOnException() = runTest {
        val categoryId = UUID.randomUUID().toString()

        whenever(collectionReference.document(categoryId)).thenReturn(documentReference)
        whenever(documentReference.get()).thenReturn(Tasks.forException(Exception("Network error")))

        val result = remoteServices.getCategoryByIdRemote(categoryId)

        assertNull(result)
        verify(collectionReference).document(categoryId)
    }

    // ========== TEST 3: saveCalendarRemote - Success ==========

    @Test
    fun saveCategory() = runTest {
        val categoryId = UUID.randomUUID().toString()
        val expectedCategory = Category(
            id = categoryId,
            name = "Work",
            color = "#FF5733",
            parentCalendarId = testCalendarId,
            syncStatus = 1
        )

        whenever(collectionReference.document(categoryId)).thenReturn(documentReference)
        whenever(documentReference.set(expectedCategory)).thenReturn(Tasks.forResult(null))

        val result = remoteServices.saveCategoryRemote(expectedCategory)

        assertTrue(result)
        verify(collectionReference).document(categoryId)
        verify(documentReference).set(expectedCategory)
    }

    @Test
    fun saveCategoryRemoteReturnsFalseOnException() = runTest {
        val categoryId = UUID.randomUUID().toString()
        val expectedCategory = Category(
            id = categoryId,
            name = "Work",
            color = "#FF5733",
            parentCalendarId = testCalendarId,
            syncStatus = 1
        )

        whenever(collectionReference.document(categoryId)).thenReturn(documentReference)
        whenever(documentReference.set(expectedCategory))
            .thenReturn(Tasks.forException(Exception("Save failed")))

        val result = remoteServices.saveCategoryRemote(expectedCategory)

        assertFalse(result)
        verify(collectionReference).document(categoryId)
        verify(documentReference).set(expectedCategory)
    }

    // ========== TEST 4: deleteCalendarRemote - Success ==========

    @Test
    fun deleteCategory() = runTest {
        val categoryId = UUID.randomUUID().toString()

        whenever(collectionReference.document(categoryId)).thenReturn(documentReference)
        whenever(documentReference.delete()).thenReturn(Tasks.forResult(null))

        val result = remoteServices.deleteCategoryRemote(categoryId)

        assertTrue(result)
        verify(collectionReference).document(categoryId)
        verify(documentReference).delete()
    }

    @Test
    fun deleteCategoryRemoteReturnsFalseOnException() = runTest {
        val categoryId = UUID.randomUUID().toString()

        whenever(collectionReference.document(categoryId)).thenReturn(documentReference)
        whenever(documentReference.delete())
            .thenReturn(Tasks.forException(Exception("Delete failed")))

        val result = remoteServices.deleteCategoryRemote(categoryId)

        assertFalse(result)
        verify(collectionReference).document(categoryId)
        verify(documentReference).delete()
    }

    // ========== TEST 5: existsCalendar - Success ==========

    @Test
    fun existsCategory() = runTest {
        val categoryId = UUID.randomUUID().toString()

        whenever(collectionReference.document(categoryId)).thenReturn(documentReference)
        whenever(documentReference.get()).thenReturn(Tasks.forResult(documentSnapshot))
        whenever(documentSnapshot.exists()).thenReturn(true)

        val result = remoteServices.existsCategory(categoryId)

        assertTrue(result)
        verify(collectionReference).document(categoryId)
        verify(documentReference).get()
        verify(documentSnapshot).exists()
    }

    @Test
    fun existsCategoryReturnsFalseWhenCategoryDoesNotExist() = runTest {
        val categoryId = UUID.randomUUID().toString()

        whenever(collectionReference.document(categoryId)).thenReturn(documentReference)
        whenever(documentReference.get()).thenReturn(Tasks.forResult(documentSnapshot))
        whenever(documentSnapshot.exists()).thenReturn(false)

        val result = remoteServices.existsCategory(categoryId)

        assertFalse(result)
        verify(collectionReference).document(categoryId)
        verify(documentSnapshot).exists()
    }

    @Test
    fun existsCategoryReturnsFalseOnException() = runTest {
        val categoryId = UUID.randomUUID().toString()

        whenever(collectionReference.document(categoryId)).thenReturn(documentReference)
        whenever(documentReference.get())
            .thenReturn(Tasks.forException(Exception("Network error")))

        val result = remoteServices.existsCategory(categoryId)

        assertFalse(result)
        verify(collectionReference).document(categoryId)
    }

    // ========== TEST 6: Multiple calendars from query ==========

    @Test
    fun getAllCategoryByCalendarIdRemoteHandlesEmptyResult() = runTest {
        whenever(collectionReference.whereEqualTo("parentCalendarId", testCalendarId))
            .thenReturn(query)
        whenever(query.get()).thenReturn(Tasks.forResult(querySnapshot))
        whenever(querySnapshot.toObjects(Calendar::class.java)).thenReturn(emptyList())

        val result = remoteServices.getAllCategorysByCalendarIdRemote(testCalendarId)

        assertTrue(result.isEmpty())
        verify(query).get()
    }

    // ========== TEST 7: Verify collection name is correct ==========

    @Test
    fun servicesUsesCorrectCollectionName() = runTest {
        val categoryId = UUID.randomUUID().toString()
        whenever(collectionReference.document(categoryId)).thenReturn(documentReference)
        whenever(documentReference.get()).thenReturn(Tasks.forResult(documentSnapshot))
        whenever(documentSnapshot.toObject(Category::class.java)).thenReturn(null)

        remoteServices.getCategoryByIdRemote(categoryId)

        verify(firestore).collection("categories")
    }
}
