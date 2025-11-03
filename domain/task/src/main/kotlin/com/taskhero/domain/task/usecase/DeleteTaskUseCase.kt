package com.taskhero.domain.task.usecase

import com.taskhero.domain.task.repository.TaskRepository
import javax.inject.Inject

/**
 * Use case to delete a task by its UUID.
 */
class DeleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(uuid: String): Result<Unit> {
        return repository.deleteTask(uuid)
    }
}
