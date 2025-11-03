package com.taskhero.domain.task.repository

import com.taskhero.domain.task.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for task-related operations.
 * All methods return Flow for reactive data streams or Result for operations.
 */
interface TaskRepository {
    /**
     * Get all tasks as a Flow stream.
     */
    fun getAllTasks(): Flow<List<Task>>

    /**
     * Get a task by its UUID.
     */
    fun getTaskByUuid(uuid: String): Flow<Task?>

    /**
     * Get the next tasks to be executed (typically by due date and priority).
     */
    fun getNextTasks(): Flow<List<Task>>

    /**
     * Insert a new task.
     */
    suspend fun insertTask(task: Task): Result<Unit>

    /**
     * Update an existing task.
     */
    suspend fun updateTask(task: Task): Result<Unit>

    /**
     * Delete a task by its UUID.
     */
    suspend fun deleteTask(uuid: String): Result<Unit>

    /**
     * Get all tasks belonging to a specific project.
     */
    fun getTasksByProject(projectName: String): Flow<List<Task>>

    /**
     * Get all tasks with a specific status.
     */
    fun getTasksByStatus(status: String): Flow<List<Task>>

    /**
     * Get all recurring task templates (tasks with recur field and no parent).
     */
    fun getRecurringTemplates(): Flow<List<Task>>

    /**
     * Get all task instances for a given parent UUID (recurring task template).
     */
    fun getTasksByParent(parentUuid: String): Flow<List<Task>>

    /**
     * Delete all tasks with a specific parent UUID.
     */
    suspend fun deleteTasksByParent(parentUuid: String): Result<Unit>
}
