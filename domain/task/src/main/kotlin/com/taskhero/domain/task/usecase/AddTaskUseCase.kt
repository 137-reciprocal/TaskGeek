package com.taskhero.domain.task.usecase

import com.taskhero.domain.task.model.Task
import com.taskhero.domain.task.repository.TaskRepository
import javax.inject.Inject

/**
 * Use case to add a new task.
 */
class AddTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task): Result<Unit> {
        return repository.insertTask(task)
    }
}
