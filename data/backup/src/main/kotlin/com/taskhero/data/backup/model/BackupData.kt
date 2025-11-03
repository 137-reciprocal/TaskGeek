package com.taskhero.data.backup.model

import com.taskhero.core.database.entity.AnnotationEntity
import com.taskhero.core.database.entity.HeroEntity
import com.taskhero.core.database.entity.TagEntity
import com.taskhero.core.database.entity.TaskDependencyCrossRef
import com.taskhero.core.database.entity.TaskEntity
import com.taskhero.core.database.entity.TaskTagCrossRef
import com.taskhero.core.database.entity.UnlockedTitleEntity
import com.taskhero.core.database.entity.XpHistoryEntity
import kotlinx.serialization.Serializable

/**
 * Data class representing a complete database backup.
 */
@Serializable
data class BackupData(
    val tasks: List<TaskEntity>,
    val annotations: List<AnnotationEntity>,
    val tags: List<TagEntity>,
    val taskTags: List<TaskTagCrossRef>,
    val taskDependencies: List<TaskDependencyCrossRef>,
    val heroes: List<HeroEntity>,
    val unlockedTitles: List<UnlockedTitleEntity>,
    val xpHistory: List<XpHistoryEntity>,
    val timestamp: Long
)
