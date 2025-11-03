package com.taskhero.feature.tasklist

import com.taskhero.domain.task.model.Task
import com.taskhero.domain.task.model.TaskFilter
import com.taskhero.domain.task.model.SortOrder
import kotlinx.collections.immutable.ImmutableList

/**
 * UI State for TaskList screen following MVI pattern.
 * Represents all possible states of the task list UI.
 */
sealed interface TaskListUiState {
    /**
     * Loading state while fetching tasks.
     */
    data object Loading : TaskListUiState

    /**
     * Success state with loaded tasks.
     *
     * @property tasks Immutable list of tasks to display
     * @property filter Current filter applied to tasks
     * @property sortOrder Current sort order of tasks
     */
    data class Success(
        val tasks: ImmutableList<Task>,
        val filter: TaskFilter,
        val sortOrder: SortOrder
    ) : TaskListUiState

    /**
     * Error state when task loading fails.
     *
     * @property message Error message to display
     */
    data class Error(val message: String) : TaskListUiState
}
