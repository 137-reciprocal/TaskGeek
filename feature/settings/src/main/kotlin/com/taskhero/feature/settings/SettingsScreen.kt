package com.taskhero.feature.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.taskhero.core.common.model.ThemeMode
import com.taskhero.core.common.model.UrgencyConfig
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Settings screen composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // State for export/import dialogs
    var exportData by remember { mutableStateOf<String?>(null) }
    var showImportPicker by remember { mutableStateOf(false) }

    // Export file launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    exportData?.let { data ->
                        outputStream.write(data.toByteArray())
                        outputStream.flush()
                    }
                }
                // Clear export data after successful write
                exportData = null
            } catch (e: Exception) {
                // Handle error - could emit an effect to show error snackbar
            }
        }
    }

    // Import file launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val jsonString = reader.readText()
                    reader.close()
                    viewModel.processImportedData(jsonString)
                }
            } catch (e: Exception) {
                // Handle error - could emit an effect to show error snackbar
            }
        }
        showImportPicker = false
    }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SettingsEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is SettingsEffect.ShowExportSuccess -> {
                    snackbarHostState.showSnackbar("Data exported to ${effect.path}")
                }
                is SettingsEffect.ShowFilePicker -> {
                    showImportPicker = true
                }
                is SettingsEffect.ShowExportDialog -> {
                    exportData = effect.jsonData
                }
            }
        }
    }

    // Launch import picker when needed
    LaunchedEffect(showImportPicker) {
        if (showImportPicker) {
            importLauncher.launch("application/json")
        }
    }

    // Launch export dialog when data is ready
    LaunchedEffect(exportData) {
        exportData?.let {
            exportLauncher.launch("taskhero_export.json")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Section
            PreferenceCategory(title = "Appearance") {
                ThemeSelector(
                    selectedTheme = uiState.theme,
                    onThemeSelected = { theme ->
                        viewModel.onIntent(SettingsIntent.UpdateTheme(theme))
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                PreferenceSwitch(
                    title = "Dynamic Colors",
                    description = "Use Material You color scheme",
                    checked = uiState.dynamicColors,
                    onCheckedChange = {
                        viewModel.onIntent(SettingsIntent.ToggleDynamicColors)
                    }
                )
            }

            // Notifications Section
            PreferenceCategory(title = "Notifications") {
                PreferenceSwitch(
                    title = "Enable Notifications",
                    description = "Receive task reminders and updates",
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = {
                        viewModel.onIntent(SettingsIntent.ToggleNotifications)
                    }
                )
            }

            // Urgency Coefficients Section
            UrgencyCoefficientsSection(
                config = uiState.urgencyConfig,
                onCoefficientChanged = { coefficient, value ->
                    viewModel.onIntent(
                        SettingsIntent.UpdateUrgencyCoefficient(coefficient, value)
                    )
                }
            )

            // Task Preferences Section
            PreferenceCategory(title = "Task Preferences") {
                var projectText by remember(uiState.defaultProject) {
                    mutableStateOf(uiState.defaultProject ?: "")
                }

                OutlinedTextField(
                    value = projectText,
                    onValueChange = { projectText = it },
                    label = { Text("Default Project") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Leave empty for no default") },
                    supportingText = { Text("New tasks will be assigned to this project") }
                )

                Button(
                    onClick = {
                        viewModel.onIntent(
                            SettingsIntent.UpdateDefaultProject(
                                projectText.takeIf { it.isNotBlank() }
                            )
                        )
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save Default Project")
                }

                Spacer(modifier = Modifier.height(16.dp))

                PreferenceSlider(
                    title = "Recurrence Limit",
                    description = "Maximum number of recurring tasks to generate: ${uiState.recurrenceLimit}",
                    value = uiState.recurrenceLimit.toFloat(),
                    onValueChange = { value ->
                        viewModel.onIntent(SettingsIntent.UpdateRecurrenceLimit(value.toInt()))
                    },
                    valueRange = 1f..50f,
                    steps = 48
                )
            }

            // Google Drive Backup Section
            PreferenceCategory(title = "Google Drive Backup") {
                if (!uiState.isGoogleSignedIn) {
                    Button(
                        onClick = {
                            viewModel.onIntent(SettingsIntent.SignInWithGoogle)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sign in with Google")
                    }
                } else {
                    Text(
                        text = "Signed in as ${uiState.googleAccountEmail}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PreferenceSwitch(
                        title = "Enable Automatic Backup",
                        description = "Automatically backup to Google Drive",
                        checked = uiState.automaticBackupEnabled,
                        onCheckedChange = {
                            viewModel.onIntent(SettingsIntent.ToggleAutomaticBackup)
                        }
                    )

                    if (uiState.automaticBackupEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Backup Frequency",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        BackupFrequency.entries.forEach { frequency ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.backupFrequency == frequency,
                                    onClick = {
                                        viewModel.onIntent(
                                            SettingsIntent.UpdateBackupFrequency(frequency)
                                        )
                                    }
                                )
                                Text(
                                    text = when (frequency) {
                                        BackupFrequency.DAILY -> "Daily"
                                        BackupFrequency.WEEKLY -> "Weekly"
                                        BackupFrequency.MONTHLY -> "Monthly"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.onIntent(SettingsIntent.BackupNow)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Backup Now")
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.onIntent(SettingsIntent.RestoreFromBackup(""))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Restore")
                        }
                    }

                    uiState.lastBackupTime?.let { timestamp ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Last backup: ${formatTimestamp(timestamp)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.onIntent(SettingsIntent.SignOutFromGoogle)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sign Out")
                    }
                }
            }

            // Data Management Section
            PreferenceCategory(title = "Data Management") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.onIntent(SettingsIntent.ExportData)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Export Data")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.onIntent(SettingsIntent.ImportData)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Import Data")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.onIntent(SettingsIntent.ResetToDefaults)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reset to Defaults")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Preference category card with title.
 */
@Composable
private fun PreferenceCategory(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                content()
            }
        }
    }
}

/**
 * Theme selector with radio buttons.
 */
@Composable
private fun ThemeSelector(
    selectedTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Theme",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        ThemeMode.entries.forEach { theme ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedTheme == theme,
                    onClick = { onThemeSelected(theme) }
                )
                Text(
                    text = when (theme) {
                        ThemeMode.LIGHT -> "Light"
                        ThemeMode.DARK -> "Dark"
                        ThemeMode.SYSTEM -> "System Default"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

/**
 * Preference switch item.
 */
@Composable
private fun PreferenceSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * Preference slider item.
 */
@Composable
private fun PreferenceSlider(
    title: String,
    description: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Urgency coefficients section with expandable sliders.
 */
@Composable
private fun UrgencyCoefficientsSection(
    config: UrgencyConfig,
    onCoefficientChanged: (String, Double) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    PreferenceCategory(title = "Urgency Calculation") {
        Text(
            text = "Configure how task urgency is calculated",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (expanded) "Hide Coefficients" else "Show Coefficients")
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))

            // Priority Coefficients
            Text(
                text = "Priority",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            CoefficientSlider(
                title = "High Priority",
                value = config.priorityHigh,
                onValueChange = { onCoefficientChanged("priority_high", it) }
            )

            CoefficientSlider(
                title = "Medium Priority",
                value = config.priorityMedium,
                onValueChange = { onCoefficientChanged("priority_medium", it) }
            )

            CoefficientSlider(
                title = "Low Priority",
                value = config.priorityLow,
                onValueChange = { onCoefficientChanged("priority_low", it) }
            )

            // Tag Coefficients
            Text(
                text = "Tags",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            CoefficientSlider(
                title = "Next Tag",
                value = config.tagNext,
                onValueChange = { onCoefficientChanged("tag_next", it) },
                valueRange = 0f..20f
            )

            // Status Coefficients
            Text(
                text = "Status",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            CoefficientSlider(
                title = "Active",
                value = config.activeCoefficient,
                onValueChange = { onCoefficientChanged("active_coefficient", it) }
            )

            CoefficientSlider(
                title = "Scheduled",
                value = config.scheduledCoefficient,
                onValueChange = { onCoefficientChanged("scheduled_coefficient", it) }
            )

            // Due Date Coefficients
            Text(
                text = "Due Date Proximity",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            CoefficientSlider(
                title = "Imminent (< 24h)",
                value = config.dueImminent,
                onValueChange = { onCoefficientChanged("due_imminent", it) },
                valueRange = 0f..20f
            )

            CoefficientSlider(
                title = "Very Soon (< 3d)",
                value = config.dueVerySoon,
                onValueChange = { onCoefficientChanged("due_very_soon", it) }
            )

            CoefficientSlider(
                title = "Soon (< 7d)",
                value = config.dueSoon,
                onValueChange = { onCoefficientChanged("due_soon", it) }
            )

            CoefficientSlider(
                title = "Near (< 14d)",
                value = config.dueNear,
                onValueChange = { onCoefficientChanged("due_near", it) }
            )

            CoefficientSlider(
                title = "Far (< 30d)",
                value = config.dueFar,
                onValueChange = { onCoefficientChanged("due_far", it) }
            )

            CoefficientSlider(
                title = "Distant (> 30d)",
                value = config.dueDistant,
                onValueChange = { onCoefficientChanged("due_distant", it) }
            )

            // Dependency Coefficients
            Text(
                text = "Dependencies",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            CoefficientSlider(
                title = "Blocking Others",
                value = config.blockingCoefficient,
                onValueChange = { onCoefficientChanged("blocking_coefficient", it) }
            )

            CoefficientSlider(
                title = "Blocked By Others",
                value = config.blockedCoefficient,
                onValueChange = { onCoefficientChanged("blocked_coefficient", it) },
                valueRange = -10f..10f
            )

            // Age Coefficients
            Text(
                text = "Age",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            CoefficientSlider(
                title = "Age Coefficient (per day)",
                value = config.ageCoefficient,
                onValueChange = { onCoefficientChanged("age_coefficient", it) },
                valueRange = 0f..1f
            )

            CoefficientSlider(
                title = "Max Age Bonus",
                value = config.maxAgeBonus,
                onValueChange = { onCoefficientChanged("max_age_bonus", it) },
                valueRange = 0f..10f
            )
        }
    }
}

/**
 * Coefficient slider with value display.
 */
@Composable
private fun CoefficientSlider(
    title: String,
    value: Double,
    onValueChange: (Double) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..10f
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = String.format("%.2f", value),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toDouble()) },
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Format timestamp for display.
 */
private fun formatTimestamp(timestamp: Long): String {
    val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
    return dateFormat.format(java.util.Date(timestamp))
}
