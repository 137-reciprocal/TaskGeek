package com.taskhero.feature.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.taskhero.domain.task.model.TaskPriority
import com.taskhero.domain.task.model.TaskStatus
import com.taskhero.feature.filter.components.FilterCriteriaCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Filter screen composable following MVI pattern.
 * Provides a visual filter builder UI with various filter criteria.
 *
 * @param viewModel ViewModel for managing filter state
 * @param onNavigateBack Callback for navigating back
 * @param onFilterApplied Callback when filter is applied
 * @param modifier Modifier for the screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    viewModel: FilterViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onFilterApplied: ((com.taskhero.domain.task.model.TaskFilter) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is FilterEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is FilterEffect.ApplyFilter -> {
                    onFilterApplied?.invoke(effect.filter)
                    onNavigateBack()
                }
                is FilterEffect.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Filter Tasks") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onIntent(FilterIntent.ClearFilter) }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear filter"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            FilterBottomBar(
                matchingTaskCount = uiState.matchingTaskCount,
                onApply = { viewModel.onIntent(FilterIntent.ApplyFilter) },
                onClear = { viewModel.onIntent(FilterIntent.ClearFilter) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Status Filter
                FilterCriteriaCard(
                    title = "Status",
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    StatusFilterChips(
                        selectedStatus = uiState.currentFilter.status,
                        onStatusSelected = { status ->
                            viewModel.onIntent(FilterIntent.UpdateStatus(status))
                        }
                    )
                }
            }

            item {
                // Project Selector
                FilterCriteriaCard(title = "Project") {
                    ProjectSelector(
                        availableProjects = uiState.availableProjects,
                        selectedProject = uiState.currentFilter.project,
                        onProjectSelected = { project ->
                            viewModel.onIntent(FilterIntent.UpdateProject(project))
                        }
                    )
                }
            }

            item {
                // Tags Selector
                FilterCriteriaCard(title = "Tags") {
                    TagsSelector(
                        availableTags = uiState.availableTags,
                        selectedTags = uiState.currentFilter.tags,
                        onAddTag = { tag ->
                            viewModel.onIntent(FilterIntent.AddTag(tag))
                        },
                        onRemoveTag = { tag ->
                            viewModel.onIntent(FilterIntent.RemoveTag(tag))
                        }
                    )
                }
            }

            item {
                // Priority Filter
                FilterCriteriaCard(title = "Priority") {
                    PriorityFilterChips(
                        selectedPriority = uiState.currentFilter.priority,
                        onPrioritiesSelected = { priorities ->
                            viewModel.onIntent(FilterIntent.UpdatePriorities(priorities))
                        }
                    )
                }
            }

            item {
                // Urgency Range
                FilterCriteriaCard(title = "Urgency Range") {
                    UrgencyRangeSlider(
                        minValue = uiState.urgencyMin,
                        maxValue = uiState.urgencyMax,
                        onMinChanged = { min ->
                            viewModel.onIntent(FilterIntent.UpdateUrgencyMin(min))
                        },
                        onMaxChanged = { max ->
                            viewModel.onIntent(FilterIntent.UpdateUrgencyMax(max))
                        }
                    )
                }
            }

            item {
                // Add bottom padding
                Text(
                    text = "",
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}

/**
 * Status filter chips for selecting task status.
 */
@Composable
private fun StatusFilterChips(
    selectedStatus: TaskStatus?,
    onStatusSelected: (TaskStatus?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedStatus == null,
            onClick = { onStatusSelected(null) },
            label = { Text("All") }
        )
        FilterChip(
            selected = selectedStatus == TaskStatus.PENDING,
            onClick = {
                onStatusSelected(
                    if (selectedStatus == TaskStatus.PENDING) null else TaskStatus.PENDING
                )
            },
            label = { Text("Pending") }
        )
        FilterChip(
            selected = selectedStatus == TaskStatus.COMPLETED,
            onClick = {
                onStatusSelected(
                    if (selectedStatus == TaskStatus.COMPLETED) null else TaskStatus.COMPLETED
                )
            },
            label = { Text("Completed") }
        )
        FilterChip(
            selected = selectedStatus == TaskStatus.DELETED,
            onClick = {
                onStatusSelected(
                    if (selectedStatus == TaskStatus.DELETED) null else TaskStatus.DELETED
                )
            },
            label = { Text("Deleted") }
        )
    }
}

/**
 * Project selector dropdown.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectSelector(
    availableProjects: List<String>,
    selectedProject: String?,
    onProjectSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedProject ?: "All Projects",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("All Projects") },
                onClick = {
                    onProjectSelected(null)
                    expanded = false
                }
            )
            availableProjects.forEach { project ->
                DropdownMenuItem(
                    text = { Text(project) },
                    onClick = {
                        onProjectSelected(project)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Tags selector with multi-select chips.
 */
@Composable
private fun TagsSelector(
    availableTags: List<String>,
    selectedTags: List<String>,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Selected tags
        if (selectedTags.isNotEmpty()) {
            Text(
                text = "Selected:",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                selectedTags.forEach { tag ->
                    FilterChip(
                        selected = true,
                        onClick = { onRemoveTag(tag) },
                        label = { Text(tag) }
                    )
                }
            }
        }

        // Available tags
        if (availableTags.isNotEmpty()) {
            Text(
                text = "Available:",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableTags.filter { !selectedTags.contains(it) }.take(5).forEach { tag ->
                    FilterChip(
                        selected = false,
                        onClick = { onAddTag(tag) },
                        label = { Text(tag) }
                    )
                }
            }
        }

        if (availableTags.isEmpty() && selectedTags.isEmpty()) {
            Text(
                text = "No tags available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Priority filter chips.
 */
@Composable
private fun PriorityFilterChips(
    selectedPriority: TaskPriority?,
    onPrioritiesSelected: (List<TaskPriority>) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedPriority == TaskPriority.HIGH,
            onClick = {
                val newPriority = if (selectedPriority == TaskPriority.HIGH) {
                    null
                } else {
                    TaskPriority.HIGH
                }
                onPrioritiesSelected(listOfNotNull(newPriority))
            },
            label = { Text("High") }
        )
        FilterChip(
            selected = selectedPriority == TaskPriority.MEDIUM,
            onClick = {
                val newPriority = if (selectedPriority == TaskPriority.MEDIUM) {
                    null
                } else {
                    TaskPriority.MEDIUM
                }
                onPrioritiesSelected(listOfNotNull(newPriority))
            },
            label = { Text("Medium") }
        )
        FilterChip(
            selected = selectedPriority == TaskPriority.LOW,
            onClick = {
                val newPriority = if (selectedPriority == TaskPriority.LOW) {
                    null
                } else {
                    TaskPriority.LOW
                }
                onPrioritiesSelected(listOfNotNull(newPriority))
            },
            label = { Text("Low") }
        )
        FilterChip(
            selected = selectedPriority == null,
            onClick = {
                onPrioritiesSelected(emptyList())
            },
            label = { Text("Any") }
        )
    }
}

/**
 * Urgency range slider.
 */
@Composable
private fun UrgencyRangeSlider(
    minValue: Float,
    maxValue: Float,
    onMinChanged: (Float) -> Unit,
    onMaxChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Range: ${minValue.toInt()} - ${maxValue.toInt()}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        RangeSlider(
            value = minValue..maxValue,
            onValueChange = { range ->
                onMinChanged(range.start)
                onMaxChanged(range.endInclusive)
            },
            valueRange = 0f..20f,
            steps = 19
        )
    }
}

/**
 * Bottom bar showing matching task count and action buttons.
 */
@Composable
private fun FilterBottomBar(
    matchingTaskCount: Int,
    onApply: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "$matchingTaskCount task${if (matchingTaskCount != 1) "s" else ""} match this filter",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onClear,
                modifier = Modifier.weight(1f)
            ) {
                Text("Clear")
            }
            Button(
                onClick = onApply,
                modifier = Modifier.weight(1f)
            ) {
                Text("Apply")
            }
        }
    }
}
