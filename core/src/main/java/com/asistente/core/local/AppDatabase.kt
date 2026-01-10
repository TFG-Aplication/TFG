package com.asistente.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.asistente.core.local.daos.CalendarDao
import com.asistente.core.local.daos.TaskDao
import com.asistente.core.local.daos.UserDao
import com.asistente.core.models.*

// 1. Registramos todas las entidades que creamos
@Database(
    entities = [
        User::class,
        Calendar::class,
        Task::class
    ],
    version = 1,
    exportSchema = false
)
// 2. Registramos el conversor para las fechas, categorías y la lista de owners
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // 3. Aquí declaramos los DAOs
    abstract fun userDao(): UserDao
    abstract fun calendarDao(): CalendarDao
    abstract fun taskDao(): TaskDao

    companion object {
        const val DATABASE_NAME = "asistente_db"
    }
}