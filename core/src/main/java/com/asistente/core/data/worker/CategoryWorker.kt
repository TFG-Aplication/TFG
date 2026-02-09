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
    private val localCategory: CategoryDao,
    private val remoteCategory: CategoryRemoteServices,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val calendarId = inputData.getString("calendar_id") ?: return Result.failure()

        return try {

            val unsyncedCategories = localCategory.getUnsyncedCategories(calendarId)
            // SUBIDA: Buscar en Room   syncStatus = 0 para subirlas
            // en room + syn = 0 -> sube a fb
            unsyncedCategories.forEach { local ->
                    remoteCategory.saveCategoryRemote(local)
                    localCategory.insertCategory(local.copy(syncStatus = 1))

            }

            // Si está en Firebase pero no en mi Room ->
            // 2 opciones:
            // - estaba en room y se borro sin internet (sync = 2) -> lo borro de fb
            // - otro lo creo y nunca estuvo en room -> lo bajo

            // opcion 1
            val categoriesToDelete = localCategory.getCategoriesBySyncStatus(2, calendarId)
            categoriesToDelete.forEach { local ->

                    remoteCategory.deleteCategoryRemote(local.id)
                    localCategory.deleteCategoryById(local.id)


            }
            val remoteCategories = remoteCategory.getAllCategorysByCalendarIdRemote(calendarId)

            //opcion 2
            // Solo bajamos lo que no tenemos Y que no esté marcado para borrar (estado 2)
            remoteCategories.forEach { remote ->
                val localVersion = localCategory.getCategoryById(remote.id)
                if (localVersion == null) { // no existe en local
                    // Es nuevo de otra persona -> Lo bajo
                    localCategory.insertCategory(remote.copy(syncStatus = 1))
                } else if (localVersion.syncStatus == 1) {
                    // Ya lo tengo, solo actualizo por si el otro usuario cambió
                    localCategory.insertCategory(remote.copy(syncStatus = 1))
                }
                // Si localVersion.syncStatus es 2, NO HACEMOS NADA (ignoramos Firebase hasta que se borre)
            }

            val remoteIds = remoteCategories.map { it.id }.toSet()

            // BAJADA Y LIMPIEZA: Solo si el calendario es compartido
            // Si está en mi Room con syncStatus = 1 pero NO está en Firebase -> Alguien lo borró
            //en room + syn =1 + no fb -> borra de room
            val localCategories = localCategory.getAllCategoryList(calendarId)
            localCategories.forEach { local ->
                if (local.syncStatus == 1 && !remoteIds.contains(local.id)) {
                    localCategory.deleteCategoryById(local.id)
                }
            }

            Result.success()
        } catch (e: Exception) {
            // Si hay error de red, WorkManager no borra nada y reintenta luego
            Result.retry()
        }
    }
}