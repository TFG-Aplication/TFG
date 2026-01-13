package com.asistente.core.data.local

import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.asistente.core.data.local.daos.CalendarDao
import com.asistente.core.data.local.daos.TaskDao
import com.asistente.core.data.local.daos.UserDao
import com.asistente.core.domain.models.User
import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.models.Task


@Database(
    entities = [
        User::class,
        Calendar::class,
        Task::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun calendarDao(): CalendarDao
    abstract fun taskDao(): TaskDao

    companion object {
        const val DATABASE_NAME = "asistente_db"
    }
}