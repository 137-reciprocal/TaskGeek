package com.taskhero.domain.task.usecase

import com.taskhero.domain.task.model.TaskwarriorJson
import com.taskhero.domain.task.repository.TaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Use case to export all tasks to Taskwarrior JSON format.
 * Converts all tasks from the repository to JSON string.
 */
class ExportTasksToJsonUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    /**
     * Export all tasks to Taskwarrior JSON format.
     *
     * @return Result containing JSON string on success, or error on failure
     */
    suspend operator fun invoke(): Result<String> {
        return try {
            // Get all tasks from repository
            val tasks = repository.getAllTasks().first()

            // Convert to Taskwarrior JSON format
            val taskwarriorTasks = tasks.map { TaskwarriorJson.fromTask(it) }

            // Serialize to JSON string with pretty print
            val json = Json {
                prettyPrint = true
                encodeDefaults = false // Don't include null fields
            }
            val jsonString = json.encodeToString(taskwarriorTasks)

            Result.success(jsonString)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
