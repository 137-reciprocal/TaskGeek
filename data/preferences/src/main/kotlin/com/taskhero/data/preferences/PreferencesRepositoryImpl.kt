package com.taskhero.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.taskhero.core.common.model.ThemeMode
import com.taskhero.core.common.model.UrgencyConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of PreferencesRepository using DataStore.
 */
@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PreferencesRepository {

    companion object {
        // Urgency coefficient keys
        private val PRIORITY_HIGH = doublePreferencesKey("priority_high")
        private val PRIORITY_MEDIUM = doublePreferencesKey("priority_medium")
        private val PRIORITY_LOW = doublePreferencesKey("priority_low")
        private val TAG_NEXT = doublePreferencesKey("tag_next")
        private val ACTIVE_COEFFICIENT = doublePreferencesKey("active_coefficient")
        private val SCHEDULED_COEFFICIENT = doublePreferencesKey("scheduled_coefficient")
        private val DUE_IMMINENT = doublePreferencesKey("due_imminent")
        private val DUE_VERY_SOON = doublePreferencesKey("due_very_soon")
        private val DUE_SOON = doublePreferencesKey("due_soon")
        private val DUE_NEAR = doublePreferencesKey("due_near")
        private val DUE_FAR = doublePreferencesKey("due_far")
        private val DUE_DISTANT = doublePreferencesKey("due_distant")
        private val BLOCKING_COEFFICIENT = doublePreferencesKey("blocking_coefficient")
        private val BLOCKED_COEFFICIENT = doublePreferencesKey("blocked_coefficient")
        private val AGE_COEFFICIENT = doublePreferencesKey("age_coefficient")
        private val MAX_AGE_BONUS = doublePreferencesKey("max_age_bonus")

        // Other preferences
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val DEFAULT_PROJECT = stringPreferencesKey("default_project")
        private val RECURRENCE_LIMIT = intPreferencesKey("recurrence_limit")

        // Default values
        private val DEFAULT_URGENCY_CONFIG = UrgencyConfig.DEFAULT
        private const val DEFAULT_RECURRENCE_LIMIT = 10
    }

    override fun getUrgencyConfig(): Flow<UrgencyConfig> {
        return dataStore.data.map { preferences ->
            UrgencyConfig(
                priorityHigh = preferences[PRIORITY_HIGH] ?: DEFAULT_URGENCY_CONFIG.priorityHigh,
                priorityMedium = preferences[PRIORITY_MEDIUM] ?: DEFAULT_URGENCY_CONFIG.priorityMedium,
                priorityLow = preferences[PRIORITY_LOW] ?: DEFAULT_URGENCY_CONFIG.priorityLow,
                tagNext = preferences[TAG_NEXT] ?: DEFAULT_URGENCY_CONFIG.tagNext,
                activeCoefficient = preferences[ACTIVE_COEFFICIENT] ?: DEFAULT_URGENCY_CONFIG.activeCoefficient,
                scheduledCoefficient = preferences[SCHEDULED_COEFFICIENT] ?: DEFAULT_URGENCY_CONFIG.scheduledCoefficient,
                dueImminent = preferences[DUE_IMMINENT] ?: DEFAULT_URGENCY_CONFIG.dueImminent,
                dueVerySoon = preferences[DUE_VERY_SOON] ?: DEFAULT_URGENCY_CONFIG.dueVerySoon,
                dueSoon = preferences[DUE_SOON] ?: DEFAULT_URGENCY_CONFIG.dueSoon,
                dueNear = preferences[DUE_NEAR] ?: DEFAULT_URGENCY_CONFIG.dueNear,
                dueFar = preferences[DUE_FAR] ?: DEFAULT_URGENCY_CONFIG.dueFar,
                dueDistant = preferences[DUE_DISTANT] ?: DEFAULT_URGENCY_CONFIG.dueDistant,
                blockingCoefficient = preferences[BLOCKING_COEFFICIENT] ?: DEFAULT_URGENCY_CONFIG.blockingCoefficient,
                blockedCoefficient = preferences[BLOCKED_COEFFICIENT] ?: DEFAULT_URGENCY_CONFIG.blockedCoefficient,
                ageCoefficient = preferences[AGE_COEFFICIENT] ?: DEFAULT_URGENCY_CONFIG.ageCoefficient,
                maxAgeBonus = preferences[MAX_AGE_BONUS] ?: DEFAULT_URGENCY_CONFIG.maxAgeBonus
            )
        }
    }

    override suspend fun updateUrgencyConfig(config: UrgencyConfig) {
        dataStore.edit { preferences ->
            preferences[PRIORITY_HIGH] = config.priorityHigh
            preferences[PRIORITY_MEDIUM] = config.priorityMedium
            preferences[PRIORITY_LOW] = config.priorityLow
            preferences[TAG_NEXT] = config.tagNext
            preferences[ACTIVE_COEFFICIENT] = config.activeCoefficient
            preferences[SCHEDULED_COEFFICIENT] = config.scheduledCoefficient
            preferences[DUE_IMMINENT] = config.dueImminent
            preferences[DUE_VERY_SOON] = config.dueVerySoon
            preferences[DUE_SOON] = config.dueSoon
            preferences[DUE_NEAR] = config.dueNear
            preferences[DUE_FAR] = config.dueFar
            preferences[DUE_DISTANT] = config.dueDistant
            preferences[BLOCKING_COEFFICIENT] = config.blockingCoefficient
            preferences[BLOCKED_COEFFICIENT] = config.blockedCoefficient
            preferences[AGE_COEFFICIENT] = config.ageCoefficient
            preferences[MAX_AGE_BONUS] = config.maxAgeBonus
        }
    }

    override suspend fun updateUrgencyCoefficient(coefficient: String, value: Double) {
        dataStore.edit { preferences ->
            when (coefficient.lowercase()) {
                "priorityhigh", "priority_high" -> preferences[PRIORITY_HIGH] = value
                "prioritymedium", "priority_medium" -> preferences[PRIORITY_MEDIUM] = value
                "prioritylow", "priority_low" -> preferences[PRIORITY_LOW] = value
                "tagnext", "tag_next" -> preferences[TAG_NEXT] = value
                "activecoefficient", "active_coefficient" -> preferences[ACTIVE_COEFFICIENT] = value
                "scheduledcoefficient", "scheduled_coefficient" -> preferences[SCHEDULED_COEFFICIENT] = value
                "dueimminent", "due_imminent" -> preferences[DUE_IMMINENT] = value
                "dueverysoon", "due_very_soon" -> preferences[DUE_VERY_SOON] = value
                "duesoon", "due_soon" -> preferences[DUE_SOON] = value
                "duenear", "due_near" -> preferences[DUE_NEAR] = value
                "duefar", "due_far" -> preferences[DUE_FAR] = value
                "duedistant", "due_distant" -> preferences[DUE_DISTANT] = value
                "blockingcoefficient", "blocking_coefficient" -> preferences[BLOCKING_COEFFICIENT] = value
                "blockedcoefficient", "blocked_coefficient" -> preferences[BLOCKED_COEFFICIENT] = value
                "agecoefficient", "age_coefficient" -> preferences[AGE_COEFFICIENT] = value
                "maxagebonus", "max_age_bonus" -> preferences[MAX_AGE_BONUS] = value
            }
        }
    }

    override fun getThemeMode(): Flow<ThemeMode> {
        return dataStore.data.map { preferences ->
            val themeName = preferences[THEME_MODE] ?: ThemeMode.SYSTEM.name
            try {
                ThemeMode.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }
        }
    }

    override suspend fun updateThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode.name
        }
    }

    override fun getDynamicColors(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[DYNAMIC_COLORS] ?: true
        }
    }

    override suspend fun updateDynamicColors(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DYNAMIC_COLORS] = enabled
        }
    }

    override fun getNotificationsEnabled(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[NOTIFICATIONS_ENABLED] ?: true
        }
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    override fun getDefaultProject(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[DEFAULT_PROJECT]
        }
    }

    override suspend fun updateDefaultProject(project: String?) {
        dataStore.edit { preferences ->
            if (project != null) {
                preferences[DEFAULT_PROJECT] = project
            } else {
                preferences.remove(DEFAULT_PROJECT)
            }
        }
    }

    override fun getRecurrenceLimit(): Flow<Int> {
        return dataStore.data.map { preferences ->
            preferences[RECURRENCE_LIMIT] ?: DEFAULT_RECURRENCE_LIMIT
        }
    }

    override suspend fun updateRecurrenceLimit(limit: Int) {
        dataStore.edit { preferences ->
            preferences[RECURRENCE_LIMIT] = limit
        }
    }

    override suspend fun resetToDefaults() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
