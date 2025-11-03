package com.taskhero.data.preferences

import com.taskhero.core.common.model.ThemeMode
import com.taskhero.core.common.model.UrgencyConfig
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user preferences.
 * Manages app settings using DataStore.
 */
interface PreferencesRepository {
    /**
     * Get the urgency configuration as a Flow.
     */
    fun getUrgencyConfig(): Flow<UrgencyConfig>

    /**
     * Update the urgency configuration.
     */
    suspend fun updateUrgencyConfig(config: UrgencyConfig)

    /**
     * Update a specific urgency coefficient.
     */
    suspend fun updateUrgencyCoefficient(coefficient: String, value: Double)

    /**
     * Get the theme mode as a Flow.
     */
    fun getThemeMode(): Flow<ThemeMode>

    /**
     * Update the theme mode.
     */
    suspend fun updateThemeMode(mode: ThemeMode)

    /**
     * Get dynamic colors preference as a Flow.
     */
    fun getDynamicColors(): Flow<Boolean>

    /**
     * Update dynamic colors preference.
     */
    suspend fun updateDynamicColors(enabled: Boolean)

    /**
     * Get notifications enabled preference as a Flow.
     */
    fun getNotificationsEnabled(): Flow<Boolean>

    /**
     * Update notifications enabled preference.
     */
    suspend fun updateNotificationsEnabled(enabled: Boolean)

    /**
     * Get default project as a Flow.
     */
    fun getDefaultProject(): Flow<String?>

    /**
     * Update default project.
     */
    suspend fun updateDefaultProject(project: String?)

    /**
     * Get recurrence limit as a Flow.
     */
    fun getRecurrenceLimit(): Flow<Int>

    /**
     * Update recurrence limit.
     */
    suspend fun updateRecurrenceLimit(limit: Int)

    /**
     * Reset all preferences to defaults.
     */
    suspend fun resetToDefaults()
}
