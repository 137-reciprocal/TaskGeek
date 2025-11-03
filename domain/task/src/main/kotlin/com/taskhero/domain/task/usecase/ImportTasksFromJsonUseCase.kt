package com.taskhero.domain.task.usecase

import com.taskhero.domain.task.model.Task
import com.taskhero.domain.task.model.TaskwarriorJson
import com.taskhero.domain.task.repository.TaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Use case to import tasks from Taskwarrior JSON format.
 * Parses JSON string and inserts tasks into the repository.
 */
class ImportTasksFromJsonUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    /**
     * Import tasks from Taskwarrior JSON format.
     *
     * @param jsonString JSON string containing tasks in Taskwarrior format
     * @return Result containing count of imported tasks on success, or error on failure
     */
    suspend operator fun invoke(jsonString: String): Result<Int> {
        return try {
            // Parse JSON string
            val json = Json {
                ignoreUnknownKeys = true // Ignore unknown fields for compatibility
            }
            val taskwarriorTasks = json.decodeFromString<List<TaskwarriorJson>>(jsonString)

            // Get existing task UUIDs to detect conflicts
            val existingTasks = repository.getAllTasks().first()
            val existingUuids = existingTasks.map { it.uuid }.toSet()

            var importedCount = 0
            var skippedCount = 0

            // Convert and insert tasks
            for (taskwarriorTask in taskwarriorTasks) {
                val task = TaskwarriorJson.toTask(taskwarriorTask)

                // Handle UUID conflicts
                if (existingUuids.contains(task.uuid)) {
                    // Update existing task
                    repository.updateTask(task).onSuccess {
                        importedCount++
                    }.onFailure {
                        skippedCount++
                    }
                } else {
                    // Insert new task
                    repository.insertTask(task).onSuccess {
                        importedCount++
                    }.onFailure {
                        skippedCount++
                    }
                }
            }

            if (skippedCount > 0) {
                Result.success(importedCount) // Could include warning about skipped tasks
            } else {
                Result.success(importedCount)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
