package com.taskhero.feature.tasklist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taskhero.core.ui.accessibility.AccessibilityConstants
import com.taskhero.core.ui.accessibility.semanticDescription
import com.taskhero.core.ui.accessibility.semanticHeading
import com.taskhero.feature.tasklist.components.TaskCard

/**
 * Main TaskList screen composable following MVI pattern.
 * Displays list of tasks with loading, success, and error states.
 *
 * @param viewModel ViewModel for managing state and intents
 * @param onNavigateToDetail Callback for navigating to task detail
 * @param modifier Modifier for the screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TaskListEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is TaskListEffect.NavigateToDetail -> {
                    onNavigateToDetail(effect.uuid)
                }
                is TaskListEffect.NavigateToAddTask -> {
                    // TODO: Implement navigation to add task screen
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Task Hero",
                        modifier = Modifier.semanticHeading()
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // For now, create a sample task
                    viewModel.onIntent(TaskListIntent.CreateTask("New Task"))
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semanticDescription(AccessibilityConstants.TaskList.FAB_ADD_TASK)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null // Handled by FAB semantics
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is TaskListUiState.Loading -> {
                    LoadingState(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .semanticDescription(AccessibilityConstants.Common.loading("tasks"))
                    )
                }

                is TaskListUiState.Success -> {
                    SuccessState(
                        state = state,
                        onTaskClick = { uuid ->
                            onNavigateToDetail(uuid)
                        },
                        onTaskComplete = { uuid ->
                            viewModel.onIntent(TaskListIntent.CompleteTask(uuid))
                        },
                        onTaskDelete = { uuid ->
                            viewModel.onIntent(TaskListIntent.DeleteTask(uuid))
                        }
                    )
                }

                is TaskListUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = {
                            viewModel.onIntent(TaskListIntent.LoadTasks)
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
 * Success state composable showing task list.
 */
@Composable
private fun SuccessState(
    state: TaskListUiState.Success,
    onTaskClick: (String) -> Unit,
    onTaskComplete: (String) -> Unit,
    onTaskDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.tasks.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .semanticDescription(AccessibilityConstants.TaskList.EMPTY_LIST),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No tasks found. Tap + to create one!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize()
        ) {
            items(
                items = state.tasks,
                key = { task -> task.uuid }
            ) { task ->
                TaskCard(
                    task = task,
                    onComplete = onTaskComplete,
                    onClick = onTaskClick,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
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
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Error: $message",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}
