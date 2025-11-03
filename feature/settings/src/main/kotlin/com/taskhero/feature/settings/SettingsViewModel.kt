package com.taskhero.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.taskhero.core.common.model.ThemeMode
import com.taskhero.data.backup.BackupWorker
import com.taskhero.data.preferences.PreferencesRepository
import com.taskhero.domain.backup.DriveBackupRepository
import com.taskhero.domain.task.usecase.ExportTasksToJsonUseCase
import com.taskhero.domain.task.usecase.ImportTasksFromJsonUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * ViewModel for Settings screen following MVI pattern.
 * Manages settings state, handles user intents, and emits side effects.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository,
    private val exportTasksToJsonUseCase: ExportTasksToJsonUseCase,
    private val importTasksFromJsonUseCase: ImportTasksFromJsonUseCase,
    private val backupRepository: DriveBackupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SettingsEffect>()
    val effect: SharedFlow<SettingsEffect> = _effect.asSharedFlow()

    init {
        loadSettings()
    }

    /**
     * Handle user intents.
     */
    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.LoadSettings -> loadSettings()
            is SettingsIntent.UpdateUrgencyCoefficient -> updateUrgencyCoefficient(
                intent.coefficient,
                intent.value
            )
            is SettingsIntent.UpdateTheme -> updateTheme(intent.theme)
            is SettingsIntent.ToggleDynamicColors -> toggleDynamicColors()
            is SettingsIntent.ToggleNotifications -> toggleNotifications()
            is SettingsIntent.UpdateDefaultProject -> updateDefaultProject(intent.project)
            is SettingsIntent.UpdateRecurrenceLimit -> updateRecurrenceLimit(intent.limit)
            is SettingsIntent.ExportData -> exportData()
            is SettingsIntent.ImportData -> importData()
            is SettingsIntent.ResetToDefaults -> resetToDefaults()
            is SettingsIntent.SignInWithGoogle -> signInWithGoogle()
            is SettingsIntent.SignOutFromGoogle -> signOutFromGoogle()
            is SettingsIntent.ToggleAutomaticBackup -> toggleAutomaticBackup()
            is SettingsIntent.UpdateBackupFrequency -> updateBackupFrequency(intent.frequency)
            is SettingsIntent.BackupNow -> backupNow()
            is SettingsIntent.RestoreFromBackup -> restoreFromBackup(intent.fileId)
        }
    }

    /**
     * Load settings from repository.
     */
    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                preferencesRepository.getUrgencyConfig(),
                preferencesRepository.getThemeMode(),
                preferencesRepository.getDynamicColors(),
                preferencesRepository.getNotificationsEnabled(),
                preferencesRepository.getDefaultProject(),
                preferencesRepository.getRecurrenceLimit()
            ) { urgencyConfig, theme, dynamicColors, notificationsEnabled, defaultProject, recurrenceLimit ->
                SettingsUiState(
                    urgencyConfig = urgencyConfig,
                    theme = theme,
                    dynamicColors = dynamicColors,
                    notificationsEnabled = notificationsEnabled,
                    defaultProject = defaultProject,
                    recurrenceLimit = recurrenceLimit,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    /**
     * Update a specific urgency coefficient.
     */
    private fun updateUrgencyCoefficient(coefficient: String, value: Double) {
        viewModelScope.launch {
            try {
                preferencesRepository.updateUrgencyCoefficient(coefficient, value)
                _effect.emit(SettingsEffect.ShowSnackbar("Urgency coefficient updated"))
            } catch (e: Exception) {
                _effect.emit(
                    SettingsEffect.ShowSnackbar(
                        e.message ?: "Failed to update coefficient"
                    )
                )
            }
        }
    }

    /**
     * Update the theme mode.
     */
    private fun updateTheme(theme: ThemeMode) {
        viewModelScope.launch {
            try {
                preferencesRepository.updateThemeMode(theme)
                _effect.emit(SettingsEffect.ShowSnackbar("Theme updated"))
            } catch (e: Exception) {
                _effect.emit(
                    SettingsEffect.ShowSnackbar(
                        e.message ?: "Failed to update theme"
                    )
                )
            }
        }
    }

    /**
     * Toggle dynamic colors on/off.
     */
    private fun toggleDynamicColors() {
        viewModelScope.launch {
            try {
                val currentValue = _uiState.value.dynamicColors
                preferencesRepository.updateDynamicColors(!currentValue)
                _effect.emit(SettingsEffect.ShowSnackbar("Dynamic colors ${if (!currentValue) "enabled" else "disabled"}"))
            } catch (e: Exception) {
                _effect.emit(
                    SettingsEffect.ShowSnackbar(
                        e.message ?: "Failed to update dynamic colors"
                    )
                )
            }
        }
    }

    /**
     * Toggle notifications on/off.
     */
    private fun toggleNotifications() {
        viewModelScope.launch {
            try {
                val currentValue = _uiState.value.notificationsEnabled
                preferencesRepository.updateNotificationsEnabled(!currentValue)
                _effect.emit(SettingsEffect.ShowSnackbar("Notifications ${if (!currentValue) "enabled" else "disabled"}"))
            } catch (e: Exception) {
                _effect.emit(
                    SettingsEffect.ShowSnackbar(
                        e.message ?: "Failed to update notifications"
                    )
                )
            }
        }
    }

    /**
     * Update the default project.
     */
    private fun updateDefaultProject(project: String?) {
        viewModelScope.launch {
            try {
                preferencesRepository.updateDefaultProject(project)
                _effect.emit(SettingsEffect.ShowSnackbar("Default project updated"))
            } catch (e: Exception) {
                _effect.emit(
                    SettingsEffect.ShowSnackbar(
                        e.message ?: "Failed to update default project"
                    )
                )
            }
        }
    }

    /**
     * Update the recurrence limit.
     */
    private fun updateRecurrenceLimit(limit: Int) {
        viewModelScope.launch {
            try {
                preferencesRepository.updateRecurrenceLimit(limit)
                _effect.emit(SettingsEffect.ShowSnackbar("Recurrence limit updated"))
            } catch (e: Exception) {
                _effect.emit(
                    SettingsEffect.ShowSnackbar(
                        e.message ?: "Failed to update recurrence limit"
                    )
                )
            }
        }
    }

    /**
     * Export app data to JSON.
     */
    private fun exportData() {
        viewModelScope.launch {
            try {
                // Export tasks to JSON
                val result = exportTasksToJsonUseCase()
                result.onSuccess { jsonString ->
                    // Emit effect with JSON data for the UI to save to file
                    _effect.emit(SettingsEffect.ShowExportDialog(jsonString))
                }.onFailure { error ->
                    _effect.emit(
                        SettingsEffect.ShowSnackbar(
                            error.message ?: "Failed to export data"
                        )
                    )
                }
            } catch (e: Exception) {
                _effect.emit(
                    SettingsEffect.ShowSnackbar(
                        e.message ?: "Failed to export data"
                    )
                )
            }
        }
    }

    /**
     * Import app data from JSON.
     */
    private fun importData() {
        viewModelScope.launch {
            try {
                // Emit effect to show file picker
                _effect.emit(SettingsEffect.ShowFilePicker)
            } catch (e: Exception) {
                _effect.emit(
                    SettingsEffect.ShowSnackbar(
                        e.message ?: "Failed to import data"
                    )
                )
            }
        }
    }

    /**
     * Process imported JSON data.
     *
     * @param jsonString JSON string from imported file
     */
    fun processImportedData(jsonString: String) {
        viewModelScope.launch {
            try {
                // Import tasks from JSON
                val result = importTasksFromJsonUseCase(jsonString)
                result.onSuccess { count ->
                    _effect.emit(SettingsEffect.ShowSnackbar("Successfully imported $count tasks"))
                }.onFailure { error ->
                    _effect.emit(
                        SettingsEffect.ShowSnackbar(
                            error.message ?: "Failed to import data"
                        )
                    )
                }
            } catch (e: Exception) {
                _effect.emit(
                    SettingsEffect.ShowSnackbar(
                        e.message ?: "Failed to import data"
                    )
                )
            }
        }
    }

    /**
     * Reset all settings to defaults.
     */
    private fun resetToDefaults() {
        viewModelScope.launch {
            try {
                preferencesRepository.resetToDefaults()
                _effect.emit(SettingsEffect.ShowSnackbar("Settings reset to defaults"))
            } catch (e: Exception) {
                _effect.emit(
                    SettingsEffect.ShowSnackbar(
                        e.message ?: "Failed to reset settings"
                    )
                )
            }
        }
    }

    /**
     * Sign in with Google for Drive backup.
     */
    private fun signInWithGoogle() {
        viewModelScope.launch {
            try {
                _effect.emit(SettingsEffect.LaunchGoogleSignIn)
            } catch (e: Exception) {
                _effect.emit(
                    SettingsEffect.ShowSnackbar(
                        e.message ?: "Failed to sign in with Google"
                    )
                )
            }
        }
    }

    /**
     * Handle Google Sign-In success.
     */
    fun onGoogleSignInSuccess(accountEmail: String) {
        viewModelScope.launch {
            try {
                // Save account email to preferences
                val prefs = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
                prefs.edit().putString("google_account_email", accountEmail).apply()

                _uiState.value = _uiState.value.copy(
                    isGoogleSignedIn = true,
                    googleAccountEmail = accountEmail
                )
                _effect.emit(SettingsEffect.ShowSnackbar("Signed in as $accountEmail"))
            } catch (e: Exception) {
                _effect.emit(
                    SettingsEffect.ShowSnackbar(
                        e.message ?: "Failed to complete sign in"
                    )
                )
            }
        }
    }

    /**
     * Sign out from Google Drive.
     */
    private fun signOutFromGoogle() {
        viewModelScope.launch {
            try {
                // Clear account email from preferences
                val prefs = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
                prefs.edit().remove("google_account_email").apply()

                // Cancel automatic backup
                if (_uiState.value.automaticBackupEnabled) {
                    cancelAutomaticBackup()
                }

                _uiState.value = _uiState.value.copy(
                    isGoogleSignedIn = false,
                    googleAccountEmail = null,
                    automaticBackupEnabled = false
                )
                _effect.emit(SettingsEffect.ShowSnackbar("Signed out from Google"))
            } catch (e: Exception) {
                _effect.emit(
                    SettingsEffect.ShowSnackbar(
                        e.message ?: "Failed to sign out"
                    )
                )
            }
        }
    }

    /**
     * Toggle automatic backup on/off.
     */
    private fun toggleAutomaticBackup() {
        viewModelScope.launch {
            try {
                if (!_uiState.value.isGoogleSignedIn) {
                    _effect.emit(SettingsEffect.ShowSnackbar("Please sign in with Google first"))
                    return@launch
                }

                val newValue = !_uiState.value.automaticBackupEnabled
                if (newValue) {
                    scheduleAutomaticBackup(_uiState.value.backupFrequency)
                } else {
                    cancelAutomaticBackup()
                }

                _uiState.value = _uiState.value.copy(automaticBackupEnabled = newValue)
                _effect.emit(
                    SettingsEffect.ShowSnackbar(
                        "Automatic backup ${if (newValue) "enabled" else "disabled"}"
                    )
                )
            } catch (e: Exception) {
                _effect.emit(
                    SettingsEffect.ShowSnackbar(
                        e.message ?: "Failed to toggle automatic backup"
                    )
                )
            }
        }
    }

    /**
     * Update backup frequency.
     */
    private fun updateBackupFrequency(frequency: BackupFrequency) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(backupFrequency = frequency)

                // Reschedule backup if automatic backup is enabled
                if (_uiState.value.automaticBackupEnabled) {
                    scheduleAutomaticBackup(frequency)
                }

                _effect.emit(SettingsEffect.ShowSnackbar("Backup frequency updated"))
            } catch (e: Exception) {
                _effect.emit(
                    SettingsEffect.ShowSnackbar(
                        e.message ?: "Failed to update backup frequency"
                    )
                )
            }
        }
    }

    /**
     * Trigger manual backup now.
     */
    private fun backupNow() {
        viewModelScope.launch {
            try {
                if (!_uiState.value.isGoogleSignedIn) {
                    _effect.emit(SettingsEffect.ShowSnackbar("Please sign in with Google first"))
                    return@launch
                }

                val result = backupRepository.backupToGoogleDrive()
                if (result.isSuccess) {
                    _effect.emit(SettingsEffect.ShowSnackbar("Backup successful"))
                } else {
                    val error = result.exceptionOrNull()
                    _effect.emit(
                        SettingsEffect.ShowSnackbar(
                            error?.message ?: "Backup failed"
                        )
                    )
                }
            } catch (e: Exception) {
                _effect.emit(
                    SettingsEffect.ShowSnackbar(
                        e.message ?: "Backup failed"
                    )
                )
            }
        }
    }

    /**
     * Restore from Google Drive backup.
     */
    private fun restoreFromBackup(fileId: String) {
        viewModelScope.launch {
            try {
                if (!_uiState.value.isGoogleSignedIn) {
                    _effect.emit(SettingsEffect.ShowSnackbar("Please sign in with Google first"))
                    return@launch
                }

                val result = backupRepository.restoreFromGoogleDrive(fileId)
                if (result.isSuccess) {
                    _effect.emit(SettingsEffect.ShowSnackbar("Restore successful"))
                } else {
                    val error = result.exceptionOrNull()
                    _effect.emit(
                        SettingsEffect.ShowSnackbar(
                            error?.message ?: "Restore failed"
                        )
                    )
                }
            } catch (e: Exception) {
                _effect.emit(
                    SettingsEffect.ShowSnackbar(
                        e.message ?: "Restore failed"
                    )
                )
            }
        }
    }

    /**
     * Schedule automatic backup with WorkManager.
     */
    private fun scheduleAutomaticBackup(frequency: BackupFrequency) {
        val repeatInterval = when (frequency) {
            BackupFrequency.DAILY -> 1L
            BackupFrequency.WEEKLY -> 7L
            BackupFrequency.MONTHLY -> 30L
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val backupWorkRequest = PeriodicWorkRequestBuilder<BackupWorker>(
            repeatInterval,
            TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            BackupWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            backupWorkRequest
        )
    }

    /**
     * Cancel automatic backup.
     */
    private fun cancelAutomaticBackup() {
        WorkManager.getInstance(context).cancelUniqueWork(BackupWorker.WORK_NAME)
    }
}
