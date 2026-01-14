package com.asistente.planificador.ui


import android.app.Application
import androidx.room.Room
import com.asistente.core.data.local.AppDatabase

class MyApp : Application() {

    companion object {
        lateinit var database: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "asistente_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}