package com.asistente.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.asistente.core.data.local.daos.CalendarDao
import com.asistente.core.data.local.daos.CategoryDao
import com.asistente.core.data.local.daos.RecordatoryDao
import com.asistente.core.data.local.daos.TaskDao
import com.asistente.core.data.local.daos.UserDao
import com.asistente.core.domain.models.User
import com.asistente.core.domain.models.Recordatory
import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.models.Category
import com.asistente.core.domain.models.Task


@Database(
    entities = [
        User::class,
        Calendar::class,
        Task::class,
        Category::class,
        Recordatory::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun calendarDao(): CalendarDao
    abstract fun taskDao(): TaskDao

    abstract fun categoryDao(): CategoryDao

    abstract fun recordatoryDao(): RecordatoryDao

    companion object {
        const val DATABASE_NAME = "asistente_db"
    }
}