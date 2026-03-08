package com.asistente.core.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.asistente.core.data.local.AppDatabase
import com.asistente.core.data.local.daos.CalendarDao
import com.asistente.core.data.local.daos.CategoryDao
import com.asistente.core.data.local.daos.RecordatoryDao
import com.asistente.core.data.local.daos.TaskDao
import com.asistente.core.data.local.daos.UserDao
import com.asistente.core.data.remote.CalendarRemoteServices
import com.asistente.core.data.remote.CategoryRemoteServices
import com.asistente.core.data.remote.RecordatoryRemoteServices
import com.asistente.core.data.remote.TaskRemoteServices
import com.asistente.core.data.remote.UserRemoteService
import com.asistente.core.data.repository.CalendarRepository
import com.asistente.core.data.repository.CategoryRepository
import com.asistente.core.data.repository.RecordatoryRepository
import com.asistente.core.data.repository.TaskRepository
import com.asistente.core.data.repository.UserRepository
import com.asistente.core.data.seeders.category.seederCategory
import com.asistente.core.domain.ropositories.interfaz.CalendarRepositoryInterface
import com.asistente.core.domain.ropositories.interfaz.CategoryRepositoryInterface
import com.asistente.core.domain.ropositories.interfaz.RecodatoryRepositoryInterface
import com.asistente.core.domain.ropositories.interfaz.TaskRepositoryInterface
import com.asistente.core.domain.ropositories.interfaz.UserRepositoryInterface
import com.asistente.core.domain.usecase.alerts.Alerts
import com.asistente.core.domain.usecase.calendar.CreateCalendar
import com.asistente.core.domain.usecase.task.CreateTask
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object dataModule {

    // --- BASE DE DATOS ---
    @Provides
    @Singleton //una unica BD
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideCalendarDao(db: AppDatabase): CalendarDao = db.calendarDao()

    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideRecordatoryDao(db: AppDatabase): RecordatoryDao = db.recordatoryDao()

    @Provides
    fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()


    // --- FIREBASE ---
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideRemoteCalendar(firestore: FirebaseFirestore) = CalendarRemoteServices(firestore)

    @Provides
    @Singleton
    fun provideRemoteCategory(firestore: FirebaseFirestore) = CategoryRemoteServices(firestore)

    @Provides
    @Singleton
    fun provideRemoteTask(firestore: FirebaseFirestore) = TaskRemoteServices(firestore)

    @Provides
    @Singleton
    fun provideRemoteUser(firestore: FirebaseFirestore) = UserRemoteService(firestore)

    @Provides
    @Singleton
    fun provideRemoteRecordatory(firestore: FirebaseFirestore) = RecordatoryRemoteServices(firestore)


    // --- REPOSITORIOS ---
    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideCalendarRepository(
        local: CalendarDao,
        workManager: WorkManager
    ): CalendarRepositoryInterface {
        return CalendarRepository(local, workManager)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(
        local: CategoryDao,
        workManager: WorkManager
    ): CategoryRepositoryInterface {
        return CategoryRepository(local, workManager)
    }

    @Provides
    @Singleton
    fun provideTaskRepository(
        taskDao: TaskDao,
        workManager: WorkManager
    ): TaskRepositoryInterface {
        return TaskRepository(taskDao, workManager)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        local: UserDao,
        remote: UserRemoteService
    ): UserRepositoryInterface {
        return UserRepository(local, remote)
    }

    @Provides
    @Singleton
    fun provideRecordatoryRepository(
        local: RecordatoryDao,
        remote: RecordatoryRemoteServices
    ): RecodatoryRepositoryInterface {
        return RecordatoryRepository(local, remote)
    }



    // --- CASOS DE USO ---
    @Provides
    fun provideCreateCalendarUseCase(repo: CalendarRepositoryInterface, categorySeeder: seederCategory): CreateCalendar {
        return CreateCalendar(repo, categorySeeder)
    }

    @Provides
    fun provideCreateTaskUseCase(
        repo: TaskRepositoryInterface,
        scheduleTaskAlerts: Alerts
    ): CreateTask {
        return CreateTask(repo, scheduleTaskAlerts)
    }
}