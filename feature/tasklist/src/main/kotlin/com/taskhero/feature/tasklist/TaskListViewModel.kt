package com.taskhero.feature.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskhero.core.parser.ParsedTaskData
import com.taskhero.domain.task.model.SortOrder
import com.taskhero.domain.task.model.Task
import com.taskhero.domain.task.model.TaskFilter
import com.taskhero.domain.task.model.TaskPriority
import com.taskhero.domain.task.model.TaskStatus
import com.taskhero.domain.task.usecase.AddTaskUseCase
import com.taskhero.domain.task.usecase.CompleteTaskUseCase
import com.taskhero.domain.task.usecase.DeleteTaskUseCase
import com.taskhero.domain.task.usecase.GetTasksUseCase
import com.taskhero.domain.task.usecase.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for TaskList screen following MVI pattern.
 * Manages state, handles user intents, and emits side effects.
 */
@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TaskListUiState>(TaskListUiState.Loading)
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<TaskListEffect>()
    val effect: SharedFlow<TaskListEffect> = _effect.asSharedFlow()

    private var currentFilter: TaskFilter = TaskFilter(status = TaskStatus.PENDING)
    private var currentSortOrder: SortOrder = SortOrder.URGENCY

    init {
        loadTasks()
    }

    /**
     * Handle user intents.
     */
    fun onIntent(intent: TaskListIntent) {
        when (intent) {
            is TaskListIntent.LoadTasks -> loadTasks()
            is TaskListIntent.CreateTask -> createTask(intent.description)
            is TaskListIntent.CreateQuickTask -> createQuickTask(intent.parsedData)
            is TaskListIntent.UpdateTask -> updateTask(intent.task)
            is TaskListIntent.DeleteTask -> deleteTask(intent.uuid)
            is TaskListIntent.CompleteTask -> completeTask(intent.uuid)
            is TaskListIntent.FilterChanged -> changeFilter(intent.filter)
            is TaskListIntent.SortChanged -> changeSortOrder(intent.sortOrder)
        }
    }

    /**
     * Load tasks from repository and apply filter/sort.
     */
    private fun loadTasks() {
        viewModelScope.launch {
            _uiState.value = TaskListUiState.Loading

            getTasksUseCase()
                .catch { exception ->
                    _uiState.value = TaskListUiState.Error(
                        message = exception.message ?: "Failed to load tasks"
                    )
                }
                .collect { tasks ->
                    val filteredAndSorted = filterAndSortTasks(tasks)
                    _uiState.value = TaskListUiState.Success(
                        tasks = filteredAndSorted.toImmutableList(),
                        filter = currentFilter,
                        sortOrder = currentSortOrder
                    )
                }
        }
    }

    /**
     * Create a new task.
     */
    private fun createTask(description: String) {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            val newTask = Task(
                uuid = UUID.randomUUID().toString(),
                description = description,
                status = TaskStatus.PENDING,
                entry = currentTime,
                urgency = 0.0
            )

            val result = addTaskUseCase(newTask)
            if (result.isSuccess) {
                _effect.emit(TaskListEffect.ShowSnackbar("Task created successfully"))
            } else {
                _effect.emit(
                    TaskListEffect.ShowSnackbar(
                        result.exceptionOrNull()?.message ?: "Failed to create task"
                    )
                )
            }
        }
    }

    /**
     * Create a task from quick entry with parsed natural language data.
     */
    private fun createQuickTask(parsedData: ParsedTaskData) {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            val newTask = Task(
                uuid = UUID.randomUUID().toString(),
                description = parsedData.description,
                status = TaskStatus.PENDING,
                entry = currentTime,
                due = parsedData.dueDate,
                priority = parsedData.priority,
                project = parsedData.project,
                tags = parsedData.tags,
                urgency = 0.0
            )

            val result = addTaskUseCase(newTask)
            if (result.isSuccess) {
                _effect.emit(TaskListEffect.ShowSnackbar("Task created successfully"))
            } else {
                _effect.emit(
                    TaskListEffect.ShowSnackbar(
                        result.exceptionOrNull()?.message ?: "Failed to create task"
                    )
                )
            }
        }
    }

    /**
     * Update an existing task.
     */
    private fun updateTask(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(modified = System.currentTimeMillis())
            val result = updateTaskUseCase(updatedTask)

            if (result.isSuccess) {
                _effect.emit(TaskListEffect.ShowSnackbar("Task updated successfully"))
            } else {
                _effect.emit(
                    TaskListEffect.ShowSnackbar(
                        result.exceptionOrNull()?.message ?: "Failed to update task"
                    )
                )
            }
        }
    }

    /**
     * Delete a task by UUID.
     */
    private fun deleteTask(uuid: String) {
        viewModelScope.launch {
            val result = deleteTaskUseCase(uuid)

            if (result.isSuccess) {
                _effect.emit(TaskListEffect.ShowSnackbar("Task deleted successfully"))
            } else {
                _effect.emit(
                    TaskListEffect.ShowSnackbar(
                        result.exceptionOrNull()?.message ?: "Failed to delete task"
                    )
                )
            }
        }
    }

    /**
     * Complete a task by UUID.
     */
    private fun completeTask(uuid: String) {
        viewModelScope.launch {
            // Find the task in current state
            val currentState = _uiState.value
            if (currentState is TaskListUiState.Success) {
                val task = currentState.tasks.find { it.uuid == uuid }
                if (task != null) {
                    val result = completeTaskUseCase(task)

                    if (result.isSuccess) {
                        _effect.emit(TaskListEffect.ShowSnackbar("Task completed! XP awarded!"))
                    } else {
                        _effect.emit(
                            TaskListEffect.ShowSnackbar(
                                result.exceptionOrNull()?.message ?: "Failed to complete task"
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * Change the filter applied to tasks.
     */
    private fun changeFilter(filter: TaskFilter) {
        currentFilter = filter
        // Re-apply filter and sort to current data
        val currentState = _uiState.value
        if (currentState is TaskListUiState.Success) {
            val filteredAndSorted = filterAndSortTasks(currentState.tasks)
            _uiState.value = TaskListUiState.Success(
                tasks = filteredAndSorted.toImmutableList(),
                filter = currentFilter,
                sortOrder = currentSortOrder
            )
        }
    }

    /**
     * Change the sort order of tasks.
     */
    private fun changeSortOrder(sortOrder: SortOrder) {
        currentSortOrder = sortOrder
        // Re-apply filter and sort to current data
        val currentState = _uiState.value
        if (currentState is TaskListUiState.Success) {
            val filteredAndSorted = filterAndSortTasks(currentState.tasks)
            _uiState.value = TaskListUiState.Success(
                tasks = filteredAndSorted.toImmutableList(),
                filter = currentFilter,
                sortOrder = currentSortOrder
            )
        }
    }

    /**
     * Filter and sort tasks based on current filter and sort order.
     */
    private fun filterAndSortTasks(tasks: List<Task>): List<Task> {
        var filtered = tasks

        // Apply filter
        currentFilter.status?.let { status ->
            filtered = filtered.filter { it.status == status }
        }
        currentFilter.priority?.let { priority ->
            filtered = filtered.filter { it.priority == priority }
        }
        currentFilter.project?.let { project ->
            filtered = filtered.filter { it.project == project }
        }
        currentFilter.description?.let { description ->
            filtered = filtered.filter { it.description.contains(description, ignoreCase = true) }
        }
        if (currentFilter.tags.isNotEmpty()) {
            filtered = filtered.filter { task ->
                currentFilter.tags.any { tag -> task.tags.contains(tag) }
            }
        }

        // Apply sort order
        val sorted = when (currentSortOrder) {
            SortOrder.URGENCY -> filtered.sortedByDescending { it.urgency }
            SortOrder.DUE_DATE -> filtered.sortedBy { it.due ?: Long.MAX_VALUE }
            SortOrder.CREATED -> filtered.sortedByDescending { it.entry }
            SortOrder.MODIFIED -> filtered.sortedByDescending { it.modified ?: it.entry }
            SortOrder.PROJECT -> filtered.sortedBy { it.project ?: "" }
            SortOrder.PRIORITY -> filtered.sortedByDescending { it.priority?.ordinal ?: -1 }
        }

        return sorted
    }
}
