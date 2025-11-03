package com.taskhero.domain.task.usecase

import com.taskhero.domain.task.model.Task
import com.taskhero.domain.task.model.TaskStatus
import com.taskhero.domain.task.repository.TaskRepository
import javax.inject.Inject

/**
 * Use case to mark a task as completed.
 * Updates task status, end timestamp, and modified timestamp.
 * For recurring task instances, generates the next instance after completion.
 */
class CompleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val calculateXpRewardUseCase: com.taskhero.domain.hero.usecase.CalculateXpRewardUseCase,
    private val addXpToHeroUseCase: com.taskhero.domain.hero.usecase.AddXpToHeroUseCase,
    private val generateRecurringTasksUseCase: GenerateRecurringTasksUseCase
) {
    suspend operator fun invoke(task: Task): Result<Unit> {
        // Mark task as completed with updated timestamps
        val currentTime = System.currentTimeMillis()
        val completedTask = task.copy(
            status = TaskStatus.COMPLETED,
            end = currentTime,
            modified = currentTime
        )

        // Update the task in repository
        val updateResult = repository.updateTask(completedTask)

        // If task update successful, calculate and award XP
        if (updateResult.isSuccess) {
            val xpReward = calculateXpRewardUseCase(task)
            addXpToHeroUseCase(xpReward.totalXp)

            // If this is a recurring task instance, generate the next instance
            if (task.parent != null) {
                generateNextRecurringInstance(task.parent)
            }
        }

        return updateResult
    }

    /**
     * Generate the next instance for a recurring task template.
     * Fetches the template and generates one new instance.
     */
    private suspend fun generateNextRecurringInstance(templateUuid: String) {
        try {
            // Get the template task
            var template: Task? = null
            repository.getTaskByUuid(templateUuid).collect { task ->
                template = task
            }

            // Generate next instance if template exists and has recurrence
            template?.let { tmpl ->
                if (tmpl.recur != null) {
                    generateRecurringTasksUseCase(tmpl, count = 1)
                }
            }
        } catch (e: Exception) {
            // Log error but don't fail the completion operation
            // In a production app, you might want to log this properly
        }
    }
}
