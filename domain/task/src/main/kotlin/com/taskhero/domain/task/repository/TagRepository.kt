package com.taskhero.domain.task.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for tag-related operations.
 * Manages the relationship between tasks and tags.
 */
interface TagRepository {
    /**
     * Get all available tags as a Flow stream.
     */
    fun getAllTags(): Flow<List<String>>

    /**
     * Add a tag to a task.
     *
     * @param taskUuid The UUID of the task
     * @param tag The tag to add
     */
    suspend fun addTagToTask(taskUuid: String, tag: String): Result<Unit>

    /**
     * Remove a tag from a task.
     *
     * @param taskUuid The UUID of the task
     * @param tag The tag to remove
     */
    suspend fun removeTagFromTask(taskUuid: String, tag: String): Result<Unit>

    /**
     * Get all tags associated with a specific task.
     *
     * @param taskUuid The UUID of the task
     */
    fun getTagsForTask(taskUuid: String): Flow<List<String>>

    /**
     * Get all tasks that have a specific tag.
     *
     * @param tag The tag to search for
     */
    fun getTasksForTag(tag: String): Flow<List<String>>
}
