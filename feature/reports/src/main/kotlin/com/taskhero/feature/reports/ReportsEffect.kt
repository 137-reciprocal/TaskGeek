package com.taskhero.feature.reports

/**
 * Side effects for Reports screen following MVI pattern.
 * Represents one-time events that should trigger UI actions.
 */
sealed interface ReportsEffect {
    /**
     * Show a snackbar message to the user.
     *
     * @property message The message to display
     */
    data class ShowSnackbar(val message: String) : ReportsEffect
}
