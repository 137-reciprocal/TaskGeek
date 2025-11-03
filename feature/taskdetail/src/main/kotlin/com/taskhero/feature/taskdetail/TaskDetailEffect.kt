package com.taskhero.feature.taskdetail

/**
 * Side effects for TaskDetail screen following MVI pattern.
 * Represents one-time events that should trigger UI actions.
 */
sealed interface TaskDetailEffect {
    /**
     * Show a snackbar message to the user.
     *
     * @property message The message to display
     */
    data class ShowSnackbar(val message: String) : TaskDetailEffect

    /**
     * Navigate back to the previous screen.
     */
    data object NavigateBack : TaskDetailEffect

    /**
     * Show the date picker dialog.
     */
    data object ShowDatePicker : TaskDetailEffect

    /**
     * Show the tag selector dialog.
     */
    data object ShowTagSelector : TaskDetailEffect
}
