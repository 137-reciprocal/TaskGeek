package com.taskhero.domain.task.usecase

import com.taskhero.domain.task.model.Task
import com.taskhero.domain.task.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get a single task by its UUID.
 */
class GetTaskByUuidUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(uuid: String): Flow<Task?> {
        return repository.getTaskByUuid(uuid)
    }
}
