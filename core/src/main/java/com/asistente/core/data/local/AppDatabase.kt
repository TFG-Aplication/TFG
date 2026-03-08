package com.asistente.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.asistente.core.data.local.daos.ActivityDao
import com.asistente.core.data.local.daos.CalendarDao
import com.asistente.core.data.local.daos.CategoryDao
import com.asistente.core.data.local.daos.RecordatoryDao
import com.asistente.core.data.local.daos.TaskDao
import com.asistente.core.data.local.daos.TimeSlotDao
import com.asistente.core.domain.models.TimeSlot
import com.asistente.core.data.local.daos.UserDao
import com.asistente.core.domain.models.User
import com.asistente.core.domain.models.Recordatory
import com.asistente.core.domain.models.Calendar
import com.asistente.core.domain.models.Category
import com.asistente.core.domain.models.Task
import com.asistente.core.domain.models.Activity


@Database (
    entities = [
        User::class,
        Calendar::class,
        Task::class,
        Category::class,
        Recordatory::class,
        Activity::class,
        TimeSlot::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun calendarDao(): CalendarDao
    abstract fun taskDao(): TaskDao

    abstract fun categoryDao(): CategoryDao

    abstract fun recordatoryDao(): RecordatoryDao

    abstract fun activityDao(): ActivityDao

    abstract fun timeSlotDao(): TimeSlotDao

    companion object {
        const val DATABASE_NAME = "asistente_db"
    }
}