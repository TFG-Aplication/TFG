package com.asistente.core.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.asistente.core.domain.models.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {


    // Reactivas para UI)

    @Query("SELECT * FROM categories where parentCalendarId = :id AND syncStatus != 2")
    fun getAllCategorysByCalendarIdFlow(id: String): Flow<List<Category>>

    @Query("SELECT * FROM categories where id = :id")
    fun getCategoryByIdFlow(id: String): Flow<Category?>

    // SUSPENDIDAS (para lógica de repositorio/sync)
    @Query("SELECT * FROM categories where parentCalendarId = :id AND syncStatus != 2")
    suspend fun getAllCategorysByCalendarId(id: String): List<Category>

    @Query("SELECT * FROM categories where id = :id")
    suspend fun getCategoryById(id: String): Category?

    //  INSERT / UPDATE / DELETE

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(categories: Category)

    @Update
    suspend fun updateCategory(category: Category)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: String)

    //  SINCRONIZACIÓN

    @Query("SELECT * FROM categories WHERE syncStatus = 0 AND parentCalendarId = :calendarId")
    suspend fun getUnsyncedCategories(calendarId: String): List<Category>

    @Query("SELECT * FROM categories WHERE syncStatus = :status AND parentCalendarId = :calendarId")
    suspend fun getCategoriesBySyncStatus(status: Int, calendarId: String): List<Category>
        
}