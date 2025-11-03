package com.taskhero.data.timetracking.di

import com.taskhero.core.database.TaskHeroDatabase
import com.taskhero.core.database.dao.TimeEntryDao
import com.taskhero.data.timetracking.repository.TimeTrackingRepositoryImpl
import com.taskhero.domain.timetracking.repository.TimeTrackingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing time tracking dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataTimeTrackingModule {

    @Provides
    @Singleton
    fun provideTimeEntryDao(database: TaskHeroDatabase): TimeEntryDao {
        return database.timeEntryDao()
    }

    @Provides
    @Singleton
    fun provideTimeTrackingRepository(
        timeEntryDao: TimeEntryDao
    ): TimeTrackingRepository {
        return TimeTrackingRepositoryImpl(timeEntryDao)
    }
}
