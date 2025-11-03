package com.taskhero.domain.filter.usecase

import com.taskhero.domain.task.model.Task
import com.taskhero.domain.task.model.TaskFilter
import com.taskhero.domain.task.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for counting tasks that match a given filter.
 * Returns a Flow that updates as tasks change.
 */
class CountMatchingTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val applyFilterUseCase: ApplyFilterUseCase
) {

    /**
     * Count tasks matching the filter.
     *
     * @param filter The filter criteria
     * @return Flow emitting the count of matching tasks
     */
    operator fun invoke(filter: TaskFilter): Flow<Int> {
        return taskRepository.getAllTasks().map { tasks ->
            applyFilterUseCase(tasks, filter).size
        }
    }
}
