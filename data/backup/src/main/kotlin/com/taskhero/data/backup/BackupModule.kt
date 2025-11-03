package com.taskhero.data.backup

import android.content.Context
import com.taskhero.core.database.TaskHeroDatabase
import com.taskhero.domain.backup.DriveBackupRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

/**
 * Hilt module for providing backup-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object BackupModule {

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }
    }

    @Provides
    @Singleton
    fun provideDriveBackupRepository(
        @ApplicationContext context: Context,
        database: TaskHeroDatabase,
        json: Json
    ): DriveBackupRepository {
        return DriveBackupRepositoryImpl(context, database, json)
    }
}
