package com.taskhero.feature.taskdetail

import com.taskhero.domain.task.model.Task
import com.taskhero.domain.timetracking.model.TimeEntry

/**
 * UI State for TaskDetail screen following MVI pattern.
 * Represents all possible states of the task detail UI.
 */
sealed interface TaskDetailUiState {
    /**
     * Loading state while fetching task data.
     */
    data object Loading : TaskDetailUiState

    /**
     * Success state with loaded task and related data.
     *
     * @property task The task being edited
     * @property availableTags List of all available tags for autocomplete
     * @property allTasks List of all tasks for dependency selection
     * @property activeTimeEntry Currently active time entry for this task (if any)
     * @property timeEntries List of all time entries for this task
     * @property totalTimeSpent Total time spent on this task in milliseconds
     */
    data class Success(
        val task: Task,
        val availableTags: List<String>,
        val allTasks: List<Task>,
        val activeTimeEntry: TimeEntry? = null,
        val timeEntries: List<TimeEntry> = emptyList(),
        val totalTimeSpent: Long = 0L
    ) : TaskDetailUiState

    /**
     * Error state when task loading fails.
     *
     * @property message Error message to display
     */
    data class Error(val message: String) : TaskDetailUiState
}
