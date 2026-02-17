package com.asistente.core.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.asistente.core.data.local.daos.CategoryDao
import com.asistente.core.data.remote.CategoryRemoteServices
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CategoryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val categoryDao: CategoryDao,
    private val categoryRemoteServices: CategoryRemoteServices
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "tag"
        const val KEY_CALENDAR_ID = "calendar_id"
    }

    override suspend fun doWork(): Result {
        val calendarId = inputData.getString(KEY_CALENDAR_ID) ?: return Result.failure()

        return try {
            // UPLOAD - Subir categorías pendientes
            uploadPendingCategories(calendarId)

            // DELETE - Eliminar categorías marcadas
            deleteMarkedCategories(calendarId)

            // DOWNLOAD - Descargar categorías de Firebase
            downloadRemoteCategories(calendarId)

            //LIMPIEZA - Limpiar categorías eliminadas en Firebase
            cleanupDeletedCategories(calendarId)

            Result.success()
        } catch (e: Exception) {
            // Si hay error de red, WorkManager reintenta automáticamente
            Result.retry()
        }
    }

    // SUBIDA: LOCAL → FIREBASE
    private suspend fun uploadPendingCategories(calendarId: String) {
        val unsyncedCategories = categoryDao.getUnsyncedCategories(calendarId)

        unsyncedCategories.forEach { category ->
            val success =  categoryRemoteServices.saveCategoryRemote(category)
            // Marcar como sincronizada
            if (success) {
                categoryDao.insertCategory(category.copy(syncStatus = 1))
            }
        }
    }

    //ELIMINACIÓN: BORRAR CALENDARIOS MARCADOS

    private suspend fun deleteMarkedCategories(calendarId: String) {
        val categoriesToDelete = categoryDao.getCategoriesBySyncStatus(2, calendarId)

        categoriesToDelete.forEach { category ->
            val deletedInFirebase = categoryRemoteServices.deleteCategoryRemote(category.id)

            val existsInFirebase = categoryRemoteServices.existsCategory(category.id)

            if (deletedInFirebase || !existsInFirebase) {
                // Eliminar definitivamente de Room
                categoryDao.deleteCategoryById(category.id)
            }
        }
    }

    //BAJADA: FIREBASE → LOCAL

    private suspend fun downloadRemoteCategories(calendarId: String) {
        val remoteCategories = categoryRemoteServices.getAllCategorysByCalendarIdRemote(calendarId)

        remoteCategories.forEach { remoteCategory ->
            val localVersion = categoryDao.getCategoryById(remoteCategory.id)

            when {
                localVersion == null -> {
                    // No existe en local → Descargar como sincronizada
                    categoryDao.insertCategory(remoteCategory.copy(syncStatus = 1))
                }
                localVersion.syncStatus == 1 -> {
                    // Existe y está sincronizada → Actualizar con versión remota
                    categoryDao.insertCategory(remoteCategory.copy(syncStatus = 1))
                }

                localVersion.syncStatus == 2 -> {
                    // Marcado para eliminar → NO sobrescribir
                    Log.d(TAG, "Ignorado (marcado para eliminar): ${remoteCategory.name}")
                }
                localVersion.syncStatus == 0 -> {
                    // Pendiente de subir → Conflicto ????
                    Log.d(TAG, "Conflicto, manteniendo local: ${remoteCategory.name}")

                }
            }
        }
    }

    // ELIMINAR LOCALES QUE NO EXISTEN EN FIREBASE

    private suspend fun cleanupDeletedCategories(calendarId: String) {
        val remoteCategories = categoryRemoteServices.getAllCategorysByCalendarIdRemote(calendarId)
        val remoteIds = remoteCategories.map { it.id }.toSet()

        val localCategories = categoryDao.getAllCategorysByCalendarId(calendarId)

        localCategories.forEach { localCategory ->
            // Solo eliminar si está sincronizada y no existe en Firebase
            if (localCategory.syncStatus == 1 && !remoteIds.contains(localCategory.id)) {
                categoryDao.deleteCategoryById(localCategory.id)
            }
        }
    }
}