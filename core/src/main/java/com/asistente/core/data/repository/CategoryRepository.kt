package com.asistente.core.data.repository

import android.content.Context
import android.util.Log
import androidx.work.*
import com.asistente.core.data.local.daos.CategoryDao
import com.asistente.core.data.worker.CategoryWorker
import com.asistente.core.domain.models.Category
import com.asistente.core.domain.ropositories.interfaz.CategoryRepositoryInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val localCategory: CategoryDao,
    @ApplicationContext private val context: Context
) : CategoryRepositoryInterface {

    private val workManager = WorkManager.getInstance(context)

    override suspend fun getCategoryById(id: String): Category? {
        // tomamos lo que hay en Room para.
        // La sincronización se encargará de actualizarlo si es necesario.
        return localCategory.getCategoryById(id)
    }

    override fun getAllCategoryByCalendarId(calendarId: String): Flow<List<Category>> {
        // Lanzamos una sincronización única para refrescar datos al entrar a la pantalla
        syncWorker(calendarId)
        return localCategory.getAllCategorysByCalendarId(calendarId)
    }

    override suspend fun saveCategory(category: Category, isSharedCalendar: Boolean) {
        if (isSharedCalendar) {
            // Guardar en Room con estado "pendiente de subir" (0)
            localCategory.insertCategory(category.copy(syncStatus = 0))

            // poner en cola en el Worker para que lo suba cuando haya internet
            syncWorker(category.parentCalendarId)

        } else {
            // Si no es compartido, se guarda como sincronizado local (1)
            // y si quieres respaldo en FB sin garantías críticas, lanzas el worker igual
            localCategory.insertCategory(category.copy(syncStatus = 0)) // cambiarlo ???
            syncWorker(category.parentCalendarId)
        }
    }

    override suspend fun deleteCategory(categoryId: String, isShared: Boolean) {
        if (isShared) {
            val category = localCategory.getCategoryById(categoryId)
            category?.let {
                // Marca como "pendiente de borrar" (2) cuando haya conex
                localCategory.insertCategory(it.copy(syncStatus = 2))

                // El Worker se encargará de borrarlo en Firebase y luego en Room
                syncWorker(it.parentCalendarId)
            }
        } else {
            // Borrado local inmediato si no es compartido
            localCategory.deleteCategoryById(categoryId)
            // intentar borrar en remoto si existe
            workManager.enqueue(
                OneTimeWorkRequestBuilder<CategoryWorker>()
                    .setInputData(workDataOf("calendar_id" to "GLOBAL", "category_id" to categoryId))
                    .build()
            )
        }
    }

    /**
     * Configura y lanza el Worker de sincronización.
     * Usamos 'UniqueWork' para evitar que se ejecuten 2 workers al mismo tiempo para el mismo calendario.
     */
    private fun syncWorker(calendarId: String) {
        Log.d("CategoryRepository", "Encolando worker para el calendario: $calendarId")
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) //conexion a internet necesaria
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<CategoryWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf("calendar_id" to calendarId))
            // Reintento exponencial si falla Firebase
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniqueWork(
            "sync_category_$calendarId",
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            syncRequest
        )
    }

    // sincronizacion cada 15 min -> min time de android
    fun schedulePeriodicSync(calendarId: String) {
        val periodicRequest = PeriodicWorkRequestBuilder<CategoryWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setInputData(workDataOf("calendar_id" to calendarId))
            .build()

        workManager.enqueueUniquePeriodicWork(
            "periodic_sync_$calendarId",
            ExistingPeriodicWorkPolicy.KEEP, // Mantener si ya existe
            periodicRequest
        )
    }
}

