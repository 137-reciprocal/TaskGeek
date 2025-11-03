package com.taskhero.domain.task.usecase

import com.taskhero.domain.task.repository.TaskRepository
import javax.inject.Inject

/**
 * Use case to delete a recurring task template and all its instances.
 *
 * This use case:
 * - Deletes the template task by its UUID
 * - Deletes all task instances that have the template's UUID as their parent
 * - Returns Result<Unit> indicating success or failure
 */
class DeleteRecurrenceUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    /**
     * Delete a recurring task template and all its instances.
     *
     * @param templateUuid The UUID of the recurring task template to delete
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(templateUuid: String): Result<Unit> {
        return try {
            // First, delete all instances (tasks with this parent UUID)
            val deleteInstancesResult = repository.deleteTasksByParent(templateUuid)
            if (deleteInstancesResult.isFailure) {
                return Result.failure(
                    deleteInstancesResult.exceptionOrNull()
                        ?: Exception("Failed to delete recurring task instances")
                )
            }

            // Then, delete the template itself
            val deleteTemplateResult = repository.deleteTask(templateUuid)
            if (deleteTemplateResult.isFailure) {
                return Result.failure(
                    deleteTemplateResult.exceptionOrNull()
                        ?: Exception("Failed to delete recurring task template")
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
