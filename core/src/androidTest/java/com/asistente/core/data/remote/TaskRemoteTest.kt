package com.asistente.core.data.com.asistente.core.data.remote

import com.asistente.core.data.remote.TaskRemoteServices
import com.asistente.core.domain.models.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TaskRemoteTest {

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

    private lateinit var remoteServices: TaskRemoteServices

    private val testUserId = "user123"
    private val calendarId = "calendar123"
    private val initDate = Date()
    private val finishDate = Date(System.currentTimeMillis() + 86400000)


    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        whenever(firestore.collection("tasks")).thenReturn(collectionReference)

        remoteServices = TaskRemoteServices(firestore)

    }

    // ========== TEST 1: getAllTasksByUserIdRemote - Success ==========

    @Test
    fun getAllTasksByUserIdRemoteReturnsTasksSuccessfully() = runTest {
        val task1 = Task (
            id =  UUID.randomUUID().toString(),
            name = "Task 1",
            owners = listOf(testUserId),
            parentCalendarId = calendarId,
            init_date = initDate,
            finish_date = finishDate
        )
        val task2 = Task (
            id =  UUID.randomUUID().toString(),
            name = "Task 1",
            owners = listOf(testUserId),
            parentCalendarId = calendarId,
            init_date = initDate,
            finish_date = finishDate
        )
        val tasks = listOf(task1, task2)

        whenever(collectionReference.whereArrayContains("owners", testUserId))
            .thenReturn(query)
        whenever(query.get()).thenReturn(Tasks.forResult(querySnapshot))
        whenever(querySnapshot.toObjects(Task::class.java)).thenReturn(tasks)

        val result = remoteServices.getAllTasksByUserIdRemote(testUserId)

        assertEquals(2, result.size)
        assertEquals("Task 1", result[0].name)
        assertEquals("Task 1", result[1].name)

        verify(collectionReference).whereArrayContains("owners", testUserId)
        verify(query).get()


    }

    @Test
    fun getAllTaskByUserIdRemoteReturnsEmptyListOnException() = runTest {
        whenever(collectionReference.whereArrayContains("owners", testUserId))
            .thenReturn(query)
        whenever(query.get()).thenReturn(Tasks.forException(Exception("Network error")))

        val result = remoteServices.getAllTasksByUserIdRemote(testUserId)

        assertTrue(result.isEmpty())
        verify(collectionReference).whereArrayContains("owners", testUserId)
    }


    // ========== TEST 2: getTaskByCalendarIdRemote - Success ==========
    @Test
    fun getAllTasksByCalendarIdRemoteReturnsTasksSuccessfully() = runTest {

    }

    @Test
    fun getAllTaskByCalendarIdRemoteReturnsEmptyListOnException() = runTest {
    }

    // ========== TEST 3: getTaskByIdRemote - Success ==========
    @Test
    fun getTaskByIdRemoteReturnsTaskSuccessfully() = runTest {
        val taskId = UUID.randomUUID().toString()
        val task1 = Task (
            id =  taskId,
            name = "Task 1",
            owners = listOf(testUserId),
            parentCalendarId = calendarId,
            init_date = initDate,
            finish_date = finishDate
        )

        whenever(collectionReference.document(taskId)).thenReturn(documentReference)
        whenever(documentReference.get()).thenReturn(Tasks.forResult(documentSnapshot))
        whenever(documentSnapshot.toObject(Task::class.java)).thenReturn(task1)

        val result = remoteServices.getTaskByIdRemote(taskId)

        assertNotNull(result)
        assertEquals("Task 1", result.name)
        assertEquals(taskId, result.id)

        verify(collectionReference).document(taskId)
        verify(documentReference).get()
    }

    @Test
    fun getTaskByIdRemoteReturnsNullWhenTaskDoesNotExist() = runTest {
        val taskId = UUID.randomUUID().toString()

        whenever(collectionReference.document(taskId)).thenReturn(documentReference)
        whenever(documentReference.get()).thenReturn(Tasks.forResult(documentSnapshot))
        whenever(documentSnapshot.toObject(Task::class.java)).thenReturn(null)

        val result = remoteServices.getTaskByIdRemote(taskId)

        assertNull(result)
        verify(collectionReference).document(taskId)
    }

    @Test
    fun getTaskByIdRemoteReturnsNullOnException() = runTest {
        val taskId = UUID.randomUUID().toString()

        whenever(collectionReference.document(taskId)).thenReturn(documentReference)
        whenever(documentReference.get()).thenReturn(Tasks.forException(Exception("Network error")))

        val result = remoteServices.getTaskByIdRemote(taskId)

        assertNull(result)
        verify(collectionReference).document(taskId)
    }

    // ========== TEST 4: saveTaskRemote - Success ==========

    @Test
    fun saveTaskRemoteReturnsTrueOnSuccess() = runTest {
        val taskId = UUID.randomUUID().toString()
        val task1 = Task (
            id =  taskId,
            name = "Task 1",
            owners = listOf(testUserId),
            parentCalendarId = calendarId,
            init_date = initDate,
            finish_date = finishDate
        )

        whenever(collectionReference.document(taskId)).thenReturn(documentReference)
        whenever(documentReference.set(task1)).thenReturn(Tasks.forResult(null))

        val result = remoteServices.saveTaskRemote(task1)

        assertTrue(result)
        verify(collectionReference).document(taskId)
        verify(documentReference).set(task1)
    }

    @Test
    fun saveTaskRemoteReturnsFalseOnException() = runTest {
        val taskId = UUID.randomUUID().toString()
        val task1 = Task (
            id =  taskId,
            name = "Task 1",
            owners = listOf(testUserId),
            parentCalendarId = calendarId,
            init_date = initDate,
            finish_date = finishDate
        )

        whenever(collectionReference.document(taskId)).thenReturn(documentReference)
        whenever(documentReference.set(task1))
            .thenReturn(Tasks.forException(Exception("Save failed")))

        val result = remoteServices.saveTaskRemote(task1)

        assertFalse(result)
        verify(collectionReference).document(taskId)
        verify(documentReference).set(task1)
    }

    // ========== TEST 5: deleteTaskRemote - Success ==========

    @Test
    fun deleteTaskRemoteReturnsTrueOnSuccess() = runTest {
        val taskId = UUID.randomUUID().toString()

        whenever(collectionReference.document(taskId)).thenReturn(documentReference)
        whenever(documentReference.delete()).thenReturn(Tasks.forResult(null))

        val result = remoteServices.deleteTaskRemote(taskId)

        assertTrue(result)
        verify(collectionReference).document(taskId)
        verify(documentReference).delete()
    }

    @Test
    fun deleteTaskRemoteReturnsFalseOnException() = runTest {
        val taskId = UUID.randomUUID().toString()

        whenever(collectionReference.document(taskId)).thenReturn(documentReference)
        whenever(documentReference.delete())
            .thenReturn(Tasks.forException(Exception("Delete failed")))

        val result = remoteServices.deleteTaskRemote(taskId)

        assertFalse(result)
        verify(collectionReference).document(taskId)
        verify(documentReference).delete()
    }

    // ========== TEST 6: existsTask - Success ==========

    @Test
    fun existsTaskReturnsTrueWhenTaskExists() = runTest {
        val taskId = UUID.randomUUID().toString()

        whenever(collectionReference.document(taskId)).thenReturn(documentReference)
        whenever(documentReference.get()).thenReturn(Tasks.forResult(documentSnapshot))
        whenever(documentSnapshot.exists()).thenReturn(true)

        val result = remoteServices.existsTask(taskId)

        assertTrue(result)
        verify(collectionReference).document(taskId)
        verify(documentReference).get()
        verify(documentSnapshot).exists()
    }

    @Test
    fun existsTaskReturnsFalseWhenTaskDoesNotExist() = runTest {
        val taskId = UUID.randomUUID().toString()

        whenever(collectionReference.document(taskId)).thenReturn(documentReference)
        whenever(documentReference.get()).thenReturn(Tasks.forResult(documentSnapshot))
        whenever(documentSnapshot.exists()).thenReturn(false)

        val result = remoteServices.existsTask(taskId)

        assertFalse(result)
        verify(collectionReference).document(taskId)
        verify(documentSnapshot).exists()
    }

    @Test
    fun existsTaskReturnsFalseOnException() = runTest {
        val taskId = UUID.randomUUID().toString()

        whenever(collectionReference.document(taskId)).thenReturn(documentReference)
        whenever(documentReference.get())
            .thenReturn(Tasks.forException(Exception("Network error")))

        val result = remoteServices.existsTask(taskId)

        assertFalse(result)
        verify(collectionReference).document(taskId)
    }

    // ========== TEST 7: Multiple tasks from query ==========

    @Test
    fun getAllTaskByUserIdRemoteHandlesEmptyResult() = runTest {
        whenever(collectionReference.whereArrayContains("owners", testUserId))
            .thenReturn(query)
        whenever(query.get()).thenReturn(Tasks.forResult(querySnapshot))
        whenever(querySnapshot.toObjects(Task::class.java)).thenReturn(emptyList())

        val result = remoteServices.getAllTasksByUserIdRemote(testUserId)

        assertTrue(result.isEmpty())
        verify(query).get()
    }

    // ========== TEST 8: Verify collection name is correct ==========

    @Test
    fun servicesUsesCorrectCollectionName() = runTest {
        val taskId = UUID.randomUUID().toString()
        whenever(collectionReference.document(taskId)).thenReturn(documentReference)
        whenever(documentReference.get()).thenReturn(Tasks.forResult(documentSnapshot))
        whenever(documentSnapshot.toObject(Task::class.java)).thenReturn(null)

        remoteServices.getTaskByIdRemote(taskId)

        verify(firestore).collection("tasks")
    }

}