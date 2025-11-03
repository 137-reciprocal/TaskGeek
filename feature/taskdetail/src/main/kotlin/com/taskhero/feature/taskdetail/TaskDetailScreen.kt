package com.taskhero.feature.taskdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taskhero.domain.task.model.Annotation
import com.taskhero.domain.task.model.Task
import com.taskhero.domain.task.model.TaskPriority
import com.taskhero.domain.task.model.TaskStatus
import com.taskhero.domain.timetracking.model.TimeEntry
import com.taskhero.core.ui.components.EmptyTimeEntries
import com.taskhero.feature.taskdetail.components.PrioritySelector
import com.taskhero.feature.taskdetail.components.TagChip
import com.taskhero.feature.taskdetail.components.TaskDatePickerDialog
import com.taskhero.feature.taskdetail.components.TagSelectorDialog
import com.taskhero.feature.taskdetail.components.UdaEditor
import com.taskhero.core.parser.DateExpressionParser
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Main TaskDetail screen composable following MVI pattern.
 * Displays task details in a scrollable form for editing.
 *
 * @param taskUuid UUID of the task to display/edit
 * @param viewModel ViewModel for managing state and intents
 * @param onNavigateBack Callback for navigating back
 * @param modifier Modifier for the screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskUuid: String,
    viewModel: TaskDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTagSelector by remember { mutableStateOf(false) }

    // Get current task state for dialogs
    val currentState = uiState
    val currentDueDate = if (currentState is TaskDetailUiState.Success) currentState.task.due else null
    val availableTags = if (currentState is TaskDetailUiState.Success) currentState.availableTags else emptyList()
    val selectedTags = if (currentState is TaskDetailUiState.Success) currentState.task.tags else emptyList()

    // Show date picker dialog
    if (showDatePicker) {
        TaskDatePickerDialog(
            currentDate = currentDueDate,
            onDismiss = { showDatePicker = false },
            onDateSelected = { millis ->
                viewModel.onIntent(TaskDetailIntent.UpdateDueDate(millis))
                showDatePicker = false
            }
        )
    }

    // Show tag selector dialog
    if (showTagSelector) {
        TagSelectorDialog(
            availableTags = availableTags,
            selectedTags = selectedTags,
            onDismiss = { showTagSelector = false },
            onTagsSelected = { newTags ->
                // Remove old tags and add new ones
                selectedTags.forEach { tag ->
                    if (!newTags.contains(tag)) {
                        viewModel.onIntent(TaskDetailIntent.RemoveTag(tag))
                    }
                }
                newTags.forEach { tag ->
                    if (!selectedTags.contains(tag)) {
                        viewModel.onIntent(TaskDetailIntent.AddTag(tag))
                    }
                }
                showTagSelector = false
            }
        )
    }

    // Load task on first composition
    LaunchedEffect(taskUuid) {
        viewModel.onIntent(TaskDetailIntent.LoadTask(taskUuid))
    }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TaskDetailEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is TaskDetailEffect.NavigateBack -> {
                    onNavigateBack()
                }
                is TaskDetailEffect.ShowDatePicker -> {
                    showDatePicker = true
                }
                is TaskDetailEffect.ShowTagSelector -> {
                    showTagSelector = true
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Task Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is TaskDetailUiState.Loading -> {
                    LoadingState(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is TaskDetailUiState.Success -> {
                    SuccessState(
                        state = state,
                        onIntent = viewModel::onIntent,
                        onShowDatePicker = { showDatePicker = true },
                        onShowTagSelector = { showTagSelector = true }
                    )
                }

                is TaskDetailUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = {
                            viewModel.onIntent(TaskDetailIntent.LoadTask(taskUuid))
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

/**
 * Loading state composable.
 */
@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Success state composable showing task details form.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SuccessState(
    state: TaskDetailUiState.Success,
    onIntent: (TaskDetailIntent) -> Unit,
    onShowDatePicker: () -> Unit,
    onShowTagSelector: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Description
        DescriptionField(
            description = state.task.description,
            onDescriptionChange = { onIntent(TaskDetailIntent.UpdateDescription(it)) }
        )

        // Status
        StatusDropdown(
            status = state.task.status,
            onStatusChange = { onIntent(TaskDetailIntent.UpdateStatus(it)) }
        )

        // Priority
        PrioritySection(
            priority = state.task.priority,
            onPriorityChange = { onIntent(TaskDetailIntent.UpdatePriority(it)) }
        )

        // Due Date
        DueDateSection(
            dueDate = state.task.due,
            onDueDateChange = { onIntent(TaskDetailIntent.UpdateDueDate(it)) },
            onShowDatePicker = onShowDatePicker
        )

        // Project
        ProjectField(
            project = state.task.project,
            onProjectChange = { onIntent(TaskDetailIntent.UpdateProject(it)) }
        )

        // Tags
        TagsSection(
            tags = state.task.tags,
            availableTags = state.availableTags,
            onAddTag = { onIntent(TaskDetailIntent.AddTag(it)) },
            onRemoveTag = { onIntent(TaskDetailIntent.RemoveTag(it)) },
            onShowTagSelector = onShowTagSelector
        )

        // Dependencies
        DependenciesSection(
            dependencies = state.task.dependencies,
            allTasks = state.allTasks,
            currentTaskUuid = state.task.uuid,
            onAddDependency = { onIntent(TaskDetailIntent.AddDependency(it)) },
            onRemoveDependency = { onIntent(TaskDetailIntent.RemoveDependency(it)) }
        )

        // Annotations
        AnnotationsSection(
            annotations = state.task.annotations,
            onAddAnnotation = { onIntent(TaskDetailIntent.AddAnnotation(it)) },
            onDeleteAnnotation = { onIntent(TaskDetailIntent.DeleteAnnotation(it)) }
        )

        // Time Tracking
        TimeTrackingSection(
            activeTimeEntry = state.activeTimeEntry,
            timeEntries = state.timeEntries,
            totalTimeSpent = state.totalTimeSpent,
            onStartTracking = { onIntent(TaskDetailIntent.StartTimeTracking) },
            onStopTracking = { onIntent(TaskDetailIntent.StopTimeTracking) },
            onDeleteEntry = { onIntent(TaskDetailIntent.DeleteTimeEntry(it)) }
        )

        // UDAs (User Defined Attributes)
        UdaEditor(
            udas = state.task.udas,
            onAddUda = { key, value, _ -> onIntent(TaskDetailIntent.AddOrUpdateUda(key, value)) },
            onUpdateUda = { key, value, _ -> onIntent(TaskDetailIntent.AddOrUpdateUda(key, value)) },
            onDeleteUda = { key -> onIntent(TaskDetailIntent.DeleteUda(key)) }
        )

        // Action Buttons
        ActionButtons(
            onSave = { onIntent(TaskDetailIntent.SaveTask) },
            onDelete = { onIntent(TaskDetailIntent.DeleteTask) }
        )

        // Add some bottom padding
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Error state composable.
 */
@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error: $message",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

/**
 * Description field composable.
 */
@Composable
private fun DescriptionField(
    description: String,
    onDescriptionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChange,
        label = { Text("Description") },
        modifier = modifier.fillMaxWidth(),
        minLines = 3,
        maxLines = 6
    )
}

/**
 * Status dropdown composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusDropdown(
    status: TaskStatus,
    onStatusChange: (TaskStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = status.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Status") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            TaskStatus.entries.forEach { statusOption ->
                DropdownMenuItem(
                    text = { Text(statusOption.name) },
                    onClick = {
                        onStatusChange(statusOption)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Priority section composable.
 */
@Composable
private fun PrioritySection(
    priority: TaskPriority?,
    onPriorityChange: (TaskPriority?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Priority",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        PrioritySelector(
            selectedPriority = priority,
            onPrioritySelected = onPriorityChange
        )
    }
}

/**
 * Due date section composable.
 */
@Composable
private fun DueDateSection(
    dueDate: Long?,
    onDueDateChange: (Long?) -> Unit,
    onShowDatePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    var textInput by remember { mutableStateOf("") }
    var showParseError by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = "Due Date",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = if (textInput.isNotEmpty()) textInput else (dueDate?.let { dateFormat.format(Date(it)) } ?: ""),
                onValueChange = { input ->
                    textInput = input
                    showParseError = false

                    // Try to parse date expression on enter or when user stops typing
                    if (input.isNotBlank()) {
                        val parsed = DateExpressionParser.parse(input)
                        if (parsed != null) {
                            val millis = parsed.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                            onDueDateChange(millis)
                            textInput = ""
                            showParseError = false
                        }
                    }
                },
                label = { Text("Due") },
                modifier = Modifier.weight(1f),
                placeholder = { Text("e.g., tomorrow, +3d, 2025-12-31") },
                isError = showParseError,
                supportingText = if (showParseError) {
                    { Text("Invalid date format") }
                } else null,
                trailingIcon = {
                    IconButton(onClick = onShowDatePicker) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Pick date"
                        )
                    }
                }
            )
            if (dueDate != null) {
                TextButton(onClick = {
                    onDueDateChange(null)
                    textInput = ""
                }) {
                    Text("Clear")
                }
            }
        }
    }
}

/**
 * Project field composable.
 */
@Composable
private fun ProjectField(
    project: String?,
    onProjectChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = project ?: "",
        onValueChange = { onProjectChange(it.ifBlank { null }) },
        label = { Text("Project") },
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Enter project name") }
    )
}

/**
 * Tags section composable.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsSection(
    tags: List<String>,
    availableTags: List<String>,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    onShowTagSelector: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newTag by remember { mutableStateOf("") }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tags",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onShowTagSelector) {
                Text("Browse Tags")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Existing tags
        if (tags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                tags.forEach { tag ->
                    TagChip(
                        tag = tag,
                        onRemove = { onRemoveTag(tag) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Add new tag
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newTag,
                onValueChange = { newTag = it },
                label = { Text("New Tag") },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Enter tag name") }
            )
            IconButton(
                onClick = {
                    if (newTag.isNotBlank()) {
                        onAddTag(newTag)
                        newTag = ""
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add tag"
                )
            }
        }
    }
}

/**
 * Dependencies section composable.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun DependenciesSection(
    dependencies: List<String>,
    allTasks: List<Task>,
    currentTaskUuid: String,
    onAddDependency: (String) -> Unit,
    onRemoveDependency: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val availableTasks = remember(allTasks, dependencies, currentTaskUuid) {
        allTasks.filter { it.uuid != currentTaskUuid && !dependencies.contains(it.uuid) }
    }

    Column(modifier = modifier) {
        Text(
            text = "Dependencies",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Existing dependencies
        if (dependencies.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                dependencies.forEach { depUuid ->
                    val depTask = allTasks.find { it.uuid == depUuid }
                    TagChip(
                        tag = depTask?.description ?: depUuid.take(8),
                        onRemove = { onRemoveDependency(depUuid) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Add new dependency
        if (availableTasks.isNotEmpty()) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Add Dependency") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    availableTasks.forEach { task ->
                        DropdownMenuItem(
                            text = { Text(task.description) },
                            onClick = {
                                onAddDependency(task.uuid)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Annotations section composable.
 */
@Composable
private fun AnnotationsSection(
    annotations: List<Annotation>,
    onAddAnnotation: (String) -> Unit,
    onDeleteAnnotation: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var newAnnotation by remember { mutableStateOf("") }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    Column(modifier = modifier) {
        Text(
            text = "Annotations",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Existing annotations
        if (annotations.isNotEmpty()) {
            annotations.forEach { annotation ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = annotation.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = dateFormat.format(Date(annotation.timestamp)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onDeleteAnnotation(annotation.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete annotation",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Add new annotation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newAnnotation,
                onValueChange = { newAnnotation = it },
                label = { Text("New Annotation") },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Add a note...") }
            )
            IconButton(
                onClick = {
                    if (newAnnotation.isNotBlank()) {
                        onAddAnnotation(newAnnotation)
                        newAnnotation = ""
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add annotation"
                )
            }
        }
    }
}

/**
 * Action buttons composable.
 */
@Composable
private fun ActionButtons(
    onSave: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onSave,
            modifier = Modifier.weight(1f)
        ) {
            Text("Save")
        }
        OutlinedButton(
            onClick = onDelete,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Delete")
        }
    }
}

/**
 * Time tracking section composable.
 */
@Composable
private fun TimeTrackingSection(
    activeTimeEntry: TimeEntry?,
    timeEntries: List<TimeEntry>,
    totalTimeSpent: Long,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onDeleteEntry: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    Column(modifier = modifier) {
        Text(
            text = "Time Tracking",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Timer control and display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (activeTimeEntry != null) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (activeTimeEntry != null) "Tracking Time" else "Not Tracking",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (activeTimeEntry != null) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        if (activeTimeEntry != null) {
                            Text(
                                text = "Started: ${dateFormat.format(Date(activeTimeEntry.startTime))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    if (activeTimeEntry != null) {
                        Button(onClick = onStopTracking) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop tracking"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop")
                        }
                    } else {
                        Button(onClick = onStartTracking) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Start tracking"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start")
                        }
                    }
                }

                // Total time display
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Total Time: ${formatDuration(totalTimeSpent)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (activeTimeEntry != null) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }

        // Time entries history
        if (timeEntries.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Time Entries",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            timeEntries.forEach { entry ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = dateFormat.format(Date(entry.startTime)),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = if (entry.endTime != null) {
                                    "Duration: ${formatDuration(entry.getDuration())}"
                                } else {
                                    "Active"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (entry.endTime != null) {
                            IconButton(onClick = { onDeleteEntry(entry.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete entry",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        } else if (activeTimeEntry == null && totalTimeSpent == 0L) {
            Spacer(modifier = Modifier.height(8.dp))
            EmptyTimeEntries(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

/**
 * Format duration in milliseconds to HH:MM:SS format.
 */
private fun formatDuration(durationMs: Long): String {
    val hours = durationMs / 3600000
    val minutes = (durationMs % 3600000) / 60000
    val seconds = (durationMs % 60000) / 1000
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
