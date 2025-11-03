package com.taskhero.feature.settings

/**
 * Side effects for Settings screen following MVI pattern.
 * Represents one-time events that should trigger UI actions.
 */
sealed interface SettingsEffect {
    /**
     * Show a snackbar message to the user.
     *
     * @property message The message to display
     */
    data class ShowSnackbar(val message: String) : SettingsEffect

    /**
     * Show file picker for importing data.
     */
    data object ShowFilePicker : SettingsEffect

    /**
     * Show export success message with file path.
     *
     * @property path The path where data was exported
     */
    data class ShowExportSuccess(val path: String) : SettingsEffect

    /**
     * Show export dialog to save JSON data to file.
     *
     * @property jsonData The JSON data to export
     */
    data class ShowExportDialog(val jsonData: String) : SettingsEffect

    /**
     * Launch Google Sign-In flow.
     */
    data object LaunchGoogleSignIn : SettingsEffect

    /**
     * Show backup file picker for restore.
     */
    data object ShowBackupFilePicker : SettingsEffect
}
