package com.taskhero.core.database

import android.content.Context
import androidx.room.Room
import com.taskhero.core.database.dao.TaskDao
import com.taskhero.core.database.dao.AnnotationDao
import com.taskhero.core.database.dao.TagDao
import com.taskhero.core.database.dao.TaskTagCrossRefDao
import com.taskhero.core.database.dao.TaskDependencyCrossRefDao
import com.taskhero.core.database.dao.HeroDao
import com.taskhero.core.database.dao.UnlockedTitleDao
import com.taskhero.core.database.dao.XpHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TaskHeroDatabase {
        return Room.databaseBuilder(
            context,
            TaskHeroDatabase::class.java,
            TaskHeroDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideTaskDao(database: TaskHeroDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    fun provideAnnotationDao(database: TaskHeroDatabase): AnnotationDao {
        return database.annotationDao()
    }

    @Provides
    fun provideTagDao(database: TaskHeroDatabase): TagDao {
        return database.tagDao()
    }

    @Provides
    fun provideTaskTagCrossRefDao(database: TaskHeroDatabase): TaskTagCrossRefDao {
        return database.taskTagCrossRefDao()
    }

    @Provides
    fun provideTaskDependencyCrossRefDao(database: TaskHeroDatabase): TaskDependencyCrossRefDao {
        return database.taskDependencyCrossRefDao()
    }

    @Provides
    fun provideHeroDao(database: TaskHeroDatabase): HeroDao {
        return database.heroDao()
    }

    @Provides
    fun provideUnlockedTitleDao(database: TaskHeroDatabase): UnlockedTitleDao {
        return database.unlockedTitleDao()
    }

    @Provides
    fun provideXpHistoryDao(database: TaskHeroDatabase): XpHistoryDao {
        return database.xpHistoryDao()
    }
}
