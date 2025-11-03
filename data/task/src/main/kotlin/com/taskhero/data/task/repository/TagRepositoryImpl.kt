package com.taskhero.data.task.repository

import com.taskhero.core.database.dao.TagDao
import com.taskhero.core.database.dao.TaskTagDao
import com.taskhero.core.database.entity.TagEntity
import com.taskhero.core.database.entity.TaskTagCrossRef
import com.taskhero.domain.task.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TagRepository that manages tag data using Room database.
 */
@Singleton
class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao,
    private val taskTagDao: TaskTagDao
) : TagRepository {

    override fun getAllTags(): Flow<List<String>> {
        return tagDao.getAllTags().map { tagEntities ->
            tagEntities.map { it.name }
        }
    }

    override suspend fun addTagToTask(taskUuid: String, tag: String): Result<Unit> {
        return try {
            // First ensure the tag exists in the tags table
            tagDao.insert(TagEntity(tag))

            // Then create the cross-reference
            taskTagDao.addTagToTask(TaskTagCrossRef(taskUuid, tag))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeTagFromTask(taskUuid: String, tag: String): Result<Unit> {
        return try {
            taskTagDao.removeTagFromTaskByName(taskUuid, tag)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getTagsForTask(taskUuid: String): Flow<List<String>> {
        return taskTagDao.getTagsForTask(taskUuid)
    }

    override fun getTasksForTag(tag: String): Flow<List<String>> {
        return taskTagDao.getTasksForTag(tag)
    }
}
