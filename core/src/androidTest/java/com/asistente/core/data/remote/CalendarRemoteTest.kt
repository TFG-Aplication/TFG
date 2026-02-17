package com.asistente.core.data.remote

import com.asistente.core.domain.models.Calendar
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

class CalendarRemoteTest {

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

    private lateinit var remoteServices: CalendarRemoteServices

    private val testUserId = "user123"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Mock Firestore collection
        whenever(firestore.collection("calendars")).thenReturn(collectionReference)

        remoteServices = CalendarRemoteServices(firestore)
    }

    // ========== TEST 1: getAllCalendarByUserIdRemote - Success ==========

    @Test
    fun getAllCalendarByUserIdRemoteReturnsCalendarsSuccessfully() = runTest {
        val calendar1 = Calendar(
            id = UUID.randomUUID().toString(),
            name = "Calendar 1",
            code = "CAL1",
            owners = listOf(testUserId)
        )
        val calendar2 = Calendar(
            id = UUID.randomUUID().toString(),
            name = "Calendar 2",
            code = "CAL2",
            owners = listOf(testUserId)
        )

        val calendars = listOf(calendar1, calendar2)

        whenever(collectionReference.whereArrayContains("owners", testUserId))
            .thenReturn(query)
        whenever(query.get()).thenReturn(Tasks.forResult(querySnapshot))
        whenever(querySnapshot.toObjects(Calendar::class.java)).thenReturn(calendars)


        val result = remoteServices.getAllCalendarByUserIdRemote(testUserId)

        assertEquals(2, result.size)
        assertEquals("Calendar 1", result[0].name)
        assertEquals("Calendar 2", result[1].name)

        verify(collectionReference).whereArrayContains("owners", testUserId)
        verify(query).get()
    }

    @Test
    fun getAllCalendarByUserIdRemoteReturnsEmptyListOnException() = runTest {
        whenever(collectionReference.whereArrayContains("owners", testUserId))
            .thenReturn(query)
        whenever(query.get()).thenReturn(Tasks.forException(Exception("Network error")))

        val result = remoteServices.getAllCalendarByUserIdRemote(testUserId)

        assertTrue(result.isEmpty())
        verify(collectionReference).whereArrayContains("owners", testUserId)
    }

    // ========== TEST 2: getCalendarByIdRemote - Success ==========

    @Test
    fun getCalendarByIdRemoteReturnsCalendarSuccessfully() = runTest {
        val calendarId = UUID.randomUUID().toString()
        val expectedCalendar = Calendar(
            id = calendarId,
            name = "Test Calendar",
            code = "TEST",
            owners = listOf(testUserId)
        )

        whenever(collectionReference.document(calendarId)).thenReturn(documentReference)
        whenever(documentReference.get()).thenReturn(Tasks.forResult(documentSnapshot))
        whenever(documentSnapshot.toObject(Calendar::class.java)).thenReturn(expectedCalendar)

        val result = remoteServices.getCalendarByIdRemote(calendarId)

        assertNotNull(result)
        assertEquals("Test Calendar", result.name)
        assertEquals(calendarId, result.id)

        verify(collectionReference).document(calendarId)
        verify(documentReference).get()
    }

    @Test
    fun getCalendarByIdRemoteReturnsNullWhenCalendarDoesNotExist() = runTest {
        val calendarId = UUID.randomUUID().toString()

        whenever(collectionReference.document(calendarId)).thenReturn(documentReference)
        whenever(documentReference.get()).thenReturn(Tasks.forResult(documentSnapshot))
        whenever(documentSnapshot.toObject(Calendar::class.java)).thenReturn(null)

        val result = remoteServices.getCalendarByIdRemote(calendarId)

        assertNull(result)
        verify(collectionReference).document(calendarId)
    }

    @Test
    fun getCalendarByIdRemoteReturnsNullOnException() = runTest {
        val calendarId = UUID.randomUUID().toString()

        whenever(collectionReference.document(calendarId)).thenReturn(documentReference)
        whenever(documentReference.get()).thenReturn(Tasks.forException(Exception("Network error")))

        val result = remoteServices.getCalendarByIdRemote(calendarId)

        assertNull(result)
        verify(collectionReference).document(calendarId)
    }

    // ========== TEST 3: saveCalendarRemote - Success ==========

    @Test
    fun saveCalendarRemoteReturnsTrueOnSuccess() = runTest {
        val calendar = Calendar(
            id = UUID.randomUUID().toString(),
            name = "New Calendar",
            code = "NEW",
            owners = listOf(testUserId)
        )

        whenever(collectionReference.document(calendar.id)).thenReturn(documentReference)
        whenever(documentReference.set(calendar)).thenReturn(Tasks.forResult(null))

        val result = remoteServices.saveCalendarRemote(calendar)

        assertTrue(result)
        verify(collectionReference).document(calendar.id)
        verify(documentReference).set(calendar)
    }

    @Test
    fun saveCalendarRemoteReturnsFalseOnException() = runTest {
        val calendar = Calendar(
            id = UUID.randomUUID().toString(),
            name = "Failed Calendar",
            code = "FAIL",
            owners = listOf(testUserId)
        )

        whenever(collectionReference.document(calendar.id)).thenReturn(documentReference)
        whenever(documentReference.set(calendar))
            .thenReturn(Tasks.forException(Exception("Save failed")))

        val result = remoteServices.saveCalendarRemote(calendar)

        assertFalse(result)
        verify(collectionReference).document(calendar.id)
        verify(documentReference).set(calendar)
    }

    // ========== TEST 4: deleteCalendarRemote - Success ==========

    @Test
    fun deleteCalendarRemoteReturnsTrueOnSuccess() = runTest {
        val calendarId = UUID.randomUUID().toString()

        whenever(collectionReference.document(calendarId)).thenReturn(documentReference)
        whenever(documentReference.delete()).thenReturn(Tasks.forResult(null))

        val result = remoteServices.deleteCalendarRemote(calendarId)

        assertTrue(result)
        verify(collectionReference).document(calendarId)
        verify(documentReference).delete()
    }

    @Test
    fun deleteCalendarRemoteReturnsFalseOnException() = runTest {
        val calendarId = UUID.randomUUID().toString()

        whenever(collectionReference.document(calendarId)).thenReturn(documentReference)
        whenever(documentReference.delete())
            .thenReturn(Tasks.forException(Exception("Delete failed")))

        val result = remoteServices.deleteCalendarRemote(calendarId)

        assertFalse(result)
        verify(collectionReference).document(calendarId)
        verify(documentReference).delete()
    }

    // ========== TEST 5: existsCalendar - Success ==========

    @Test
    fun existsCalendarReturnsTrueWhenCalendarExists() = runTest {
        val calendarId = UUID.randomUUID().toString()

        whenever(collectionReference.document(calendarId)).thenReturn(documentReference)
        whenever(documentReference.get()).thenReturn(Tasks.forResult(documentSnapshot))
        whenever(documentSnapshot.exists()).thenReturn(true)

        val result = remoteServices.existsCalendar(calendarId)

        assertTrue(result)
        verify(collectionReference).document(calendarId)
        verify(documentReference).get()
        verify(documentSnapshot).exists()
    }

    @Test
    fun existsCalendarReturnsFalseWhenCalendarDoesNotExist() = runTest {
        val calendarId = UUID.randomUUID().toString()

        whenever(collectionReference.document(calendarId)).thenReturn(documentReference)
        whenever(documentReference.get()).thenReturn(Tasks.forResult(documentSnapshot))
        whenever(documentSnapshot.exists()).thenReturn(false)

        val result = remoteServices.existsCalendar(calendarId)

        assertFalse(result)
        verify(collectionReference).document(calendarId)
        verify(documentSnapshot).exists()
    }

    @Test
    fun existsCalendarReturnsFalseOnException() = runTest {
        val calendarId = UUID.randomUUID().toString()

        whenever(collectionReference.document(calendarId)).thenReturn(documentReference)
        whenever(documentReference.get())
            .thenReturn(Tasks.forException(Exception("Network error")))

        val result = remoteServices.existsCalendar(calendarId)

        assertFalse(result)
        verify(collectionReference).document(calendarId)
    }

    // ========== TEST 6: Multiple calendars from query ==========

    @Test
    fun getAllCalendarByUserIdRemoteHandlesEmptyResult() = runTest {
        whenever(collectionReference.whereArrayContains("owners", testUserId))
            .thenReturn(query)
        whenever(query.get()).thenReturn(Tasks.forResult(querySnapshot))
        whenever(querySnapshot.toObjects(Calendar::class.java)).thenReturn(emptyList())

        val result = remoteServices.getAllCalendarByUserIdRemote(testUserId)

        assertTrue(result.isEmpty())
        verify(query).get()
    }

    // ========== TEST 7: Verify collection name is correct ==========

    @Test
    fun servicesUsesCorrectCollectionName() = runTest {
        val calendarId = UUID.randomUUID().toString()
        whenever(collectionReference.document(calendarId)).thenReturn(documentReference)
        whenever(documentReference.get()).thenReturn(Tasks.forResult(documentSnapshot))
        whenever(documentSnapshot.toObject(Calendar::class.java)).thenReturn(null)

        remoteServices.getCalendarByIdRemote(calendarId)

        verify(firestore).collection("calendars")
    }
}
