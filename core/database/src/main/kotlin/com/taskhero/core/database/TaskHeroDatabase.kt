package com.taskhero.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.taskhero.core.database.converter.TaskStatusConverter
import com.taskhero.core.database.converter.TaskPriorityConverter
import com.taskhero.core.database.dao.TaskDao
import com.taskhero.core.database.dao.AnnotationDao
import com.taskhero.core.database.dao.TagDao
import com.taskhero.core.database.dao.TaskTagCrossRefDao
import com.taskhero.core.database.dao.TaskDependencyCrossRefDao
import com.taskhero.core.database.dao.HeroDao
import com.taskhero.core.database.dao.UnlockedTitleDao
import com.taskhero.core.database.dao.XpHistoryDao
import com.taskhero.core.database.dao.TimeEntryDao
import com.taskhero.core.database.entity.TaskEntity
import com.taskhero.core.database.entity.AnnotationEntity
import com.taskhero.core.database.entity.TagEntity
import com.taskhero.core.database.entity.TaskTagCrossRef
import com.taskhero.core.database.entity.TaskDependencyCrossRef
import com.taskhero.core.database.entity.HeroEntity
import com.taskhero.core.database.entity.UnlockedTitleEntity
import com.taskhero.core.database.entity.XpHistoryEntity
import com.taskhero.core.database.entity.TimeEntryEntity

@Database(
    version = 2,
    entities = [
        TaskEntity::class,
        AnnotationEntity::class,
        TagEntity::class,
        TaskTagCrossRef::class,
        TaskDependencyCrossRef::class,
        HeroEntity::class,
        UnlockedTitleEntity::class,
        XpHistoryEntity::class,
        TimeEntryEntity::class
    ],
    exportSchema = false
)
@TypeConverters(TaskStatusConverter::class, TaskPriorityConverter::class)
abstract class TaskHeroDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun annotationDao(): AnnotationDao
    abstract fun tagDao(): TagDao
    abstract fun taskTagCrossRefDao(): TaskTagCrossRefDao
    abstract fun taskDependencyCrossRefDao(): TaskDependencyCrossRefDao
    abstract fun heroDao(): HeroDao
    abstract fun unlockedTitleDao(): UnlockedTitleDao
    abstract fun xpHistoryDao(): XpHistoryDao
    abstract fun timeEntryDao(): TimeEntryDao

    companion object {
        const val DATABASE_NAME = "taskhero_database"
    }
}
