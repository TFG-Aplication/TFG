package com.asistente.core.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.asistente.core.domain.models.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

        @Query("SELECT * FROM categories where parentCalendarId LIKE :id AND syncStatus != 2")
        fun getAllCategorysByCalendarId(id: String): Flow<List<Category>>

        @Query("SELECT * FROM categories where parentCalendarId LIKE :id")
        fun getAllCategoryList(id: String): List<Category>

        @Query("SELECT * FROM categories where id = :id")
        suspend fun getCategoryById(id: String): Category?

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertCategory(categories: Category)

        @Query("DELETE FROM categories WHERE id = :id")
        suspend fun deleteCategoryById(id: String)

        @Query("SELECT * FROM categories WHERE syncStatus = 0 AND parentCalendarId LIKE :calendarId")
        suspend fun getUnsyncedCategories(calendarId: String): List<Category>

        @Query("SELECT * FROM categories WHERE syncStatus = :status AND parentCalendarId LIKE :calendarId")
        suspend fun getCategoriesBySyncStatus(status: Int, calendarId: String): List<Category>
        
}