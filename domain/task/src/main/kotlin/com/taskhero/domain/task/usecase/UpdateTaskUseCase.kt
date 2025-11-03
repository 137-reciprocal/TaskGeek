package com.taskhero.domain.task.usecase

import com.taskhero.domain.task.model.Task
import com.taskhero.domain.task.repository.TaskRepository
import javax.inject.Inject

/**
 * Use case to update an existing task.
 */
class UpdateTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task): Result<Unit> {
        return repository.updateTask(task)
    }
}
