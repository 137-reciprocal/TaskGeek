package com.taskhero.feature.settings

import com.taskhero.core.common.model.ThemeMode

/**
 * User intents for Settings screen following MVI pattern.
 * Represents all possible user actions on the settings screen.
 */
sealed interface SettingsIntent {
    /**
     * Load settings from repository.
     */
    data object LoadSettings : SettingsIntent

    /**
     * Update a specific urgency coefficient.
     *
     * @property coefficient The coefficient name
     * @property value The new value
     */
    data class UpdateUrgencyCoefficient(
        val coefficient: String,
        val value: Double
    ) : SettingsIntent

    /**
     * Update the theme mode.
     *
     * @property theme The new theme mode
     */
    data class UpdateTheme(val theme: ThemeMode) : SettingsIntent

    /**
     * Toggle dynamic colors on/off.
     */
    data object ToggleDynamicColors : SettingsIntent

    /**
     * Toggle notifications on/off.
     */
    data object ToggleNotifications : SettingsIntent

    /**
     * Update the default project.
     *
     * @property project The new default project (null to clear)
     */
    data class UpdateDefaultProject(val project: String?) : SettingsIntent

    /**
     * Update the recurrence limit.
     *
     * @property limit The new recurrence limit
     */
    data class UpdateRecurrenceLimit(val limit: Int) : SettingsIntent

    /**
     * Export app data.
     */
    data object ExportData : SettingsIntent

    /**
     * Import app data.
     */
    data object ImportData : SettingsIntent

    /**
     * Reset all settings to defaults.
     */
    data object ResetToDefaults : SettingsIntent

    /**
     * Sign in with Google for Drive backup.
     */
    data object SignInWithGoogle : SettingsIntent

    /**
     * Sign out from Google Drive.
     */
    data object SignOutFromGoogle : SettingsIntent

    /**
     * Toggle automatic backup on/off.
     */
    data object ToggleAutomaticBackup : SettingsIntent

    /**
     * Update backup frequency.
     *
     * @property frequency The backup frequency (Daily, Weekly, Monthly)
     */
    data class UpdateBackupFrequency(val frequency: BackupFrequency) : SettingsIntent

    /**
     * Trigger manual backup now.
     */
    data object BackupNow : SettingsIntent

    /**
     * Restore from Google Drive backup.
     *
     * @property fileId The file ID to restore from
     */
    data class RestoreFromBackup(val fileId: String) : SettingsIntent
}

/**
 * Backup frequency options.
 */
enum class BackupFrequency {
    DAILY,
    WEEKLY,
    MONTHLY
}
