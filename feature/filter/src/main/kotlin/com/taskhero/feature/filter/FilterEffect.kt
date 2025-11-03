package com.taskhero.feature.filter

import com.taskhero.domain.task.model.TaskFilter

/**
 * Side effects for Filter screen following MVI pattern.
 * Represents one-time events that should trigger UI actions.
 */
sealed interface FilterEffect {
    /**
     * Show a snackbar message to the user.
     *
     * @property message The message to display
     */
    data class ShowSnackbar(val message: String) : FilterEffect

    /**
     * Apply the filter and navigate back.
     *
     * @property filter The filter to apply
     */
    data class ApplyFilter(val filter: TaskFilter) : FilterEffect

    /**
     * Navigate back without applying the filter.
     */
    data object NavigateBack : FilterEffect
}
