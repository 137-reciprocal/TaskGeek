package com.taskhero.feature.settings

import com.taskhero.core.common.model.ThemeMode
import com.taskhero.core.common.model.UrgencyConfig

/**
 * UI State for Settings screen following MVI pattern.
 */
data class SettingsUiState(
    val urgencyConfig: UrgencyConfig = UrgencyConfig.DEFAULT,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColors: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val defaultProject: String? = null,
    val recurrenceLimit: Int = 10,
    val isLoading: Boolean = false,
    // Google Drive Backup
    val isGoogleSignedIn: Boolean = false,
    val googleAccountEmail: String? = null,
    val automaticBackupEnabled: Boolean = false,
    val backupFrequency: BackupFrequency = BackupFrequency.DAILY,
    val lastBackupTime: Long? = null
)
