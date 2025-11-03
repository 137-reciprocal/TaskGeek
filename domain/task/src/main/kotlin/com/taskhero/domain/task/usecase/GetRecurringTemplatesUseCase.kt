package com.taskhero.domain.task.usecase

import com.taskhero.domain.task.model.Task
import com.taskhero.domain.task.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get all recurring task templates.
 *
 * Returns a Flow of tasks that have a recurrence pattern (recur field is not null)
 * and are templates (parent field is null).
 */
class GetRecurringTemplatesUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    /**
     * Get all recurring task templates.
     *
     * @return Flow emitting list of recurring task templates
     */
    operator fun invoke(): Flow<List<Task>> {
        return repository.getRecurringTemplates()
    }
}
