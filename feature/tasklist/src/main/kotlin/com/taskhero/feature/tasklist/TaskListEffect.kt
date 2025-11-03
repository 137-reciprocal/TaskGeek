package com.taskhero.feature.tasklist

/**
 * Side effects for TaskList screen following MVI pattern.
 * Represents one-time events that should trigger UI actions.
 */
sealed interface TaskListEffect {
    /**
     * Show a snackbar message to the user.
     *
     * @property message The message to display
     */
    data class ShowSnackbar(val message: String) : TaskListEffect

    /**
     * Navigate to task detail screen.
     *
     * @property uuid The UUID of task to view
     */
    data class NavigateToDetail(val uuid: String) : TaskListEffect

    /**
     * Navigate to add task screen.
     */
    data object NavigateToAddTask : TaskListEffect

    /**
     * Show brain dump dialog for adding multiple tasks.
     */
    data object ShowBrainDumpDialog : TaskListEffect
}
