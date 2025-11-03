package com.taskhero.feature.taskdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskhero.domain.task.model.Annotation
import com.taskhero.domain.task.model.Task
import com.taskhero.domain.task.model.TaskPriority
import com.taskhero.domain.task.model.TaskStatus
import com.taskhero.domain.task.usecase.DeleteTaskUseCase
import com.taskhero.domain.task.usecase.GetTaskByUuidUseCase
import com.taskhero.domain.task.usecase.GetTasksUseCase
import com.taskhero.domain.task.usecase.UpdateTaskUseCase
import com.taskhero.domain.timetracking.repository.TimeTrackingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for TaskDetail screen following MVI pattern.
 * Manages state, handles user intents, and emits side effects.
 */
@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val getTaskByUuidUseCase: GetTaskByUuidUseCase,
    private val getTasksUseCase: GetTasksUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val timeTrackingRepository: TimeTrackingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TaskDetailUiState>(TaskDetailUiState.Loading)
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<TaskDetailEffect>()
    val effect: SharedFlow<TaskDetailEffect> = _effect.asSharedFlow()

    private var currentTask: Task? = null

    /**
     * Handle user intents.
     */
    fun onIntent(intent: TaskDetailIntent) {
        when (intent) {
            is TaskDetailIntent.LoadTask -> loadTask(intent.uuid)
            is TaskDetailIntent.UpdateDescription -> updateDescription(intent.description)
            is TaskDetailIntent.UpdateStatus -> updateStatus(intent.status)
            is TaskDetailIntent.UpdatePriority -> updatePriority(intent.priority)
            is TaskDetailIntent.UpdateDueDate -> updateDueDate(intent.due)
            is TaskDetailIntent.UpdateProject -> updateProject(intent.project)
            is TaskDetailIntent.AddTag -> addTag(intent.tag)
            is TaskDetailIntent.RemoveTag -> removeTag(intent.tag)
            is TaskDetailIntent.AddDependency -> addDependency(intent.dependsOnUuid)
            is TaskDetailIntent.RemoveDependency -> removeDependency(intent.dependsOnUuid)
            is TaskDetailIntent.AddAnnotation -> addAnnotation(intent.description)
            is TaskDetailIntent.DeleteAnnotation -> deleteAnnotation(intent.annotationId)
            is TaskDetailIntent.AddOrUpdateUda -> addOrUpdateUda(intent.key, intent.value)
            is TaskDetailIntent.DeleteUda -> deleteUda(intent.key)
            is TaskDetailIntent.SaveTask -> saveTask()
            is TaskDetailIntent.DeleteTask -> deleteTask()
            is TaskDetailIntent.StartTimeTracking -> startTimeTracking()
            is TaskDetailIntent.StopTimeTracking -> stopTimeTracking()
            is TaskDetailIntent.DeleteTimeEntry -> deleteTimeEntry(intent.entryId)
        }
    }

    /**
     * Load task by UUID along with all tasks, available tags, and time tracking data.
     */
    private fun loadTask(uuid: String) {
        viewModelScope.launch {
            _uiState.value = TaskDetailUiState.Loading

            try {
                combine(
                    getTaskByUuidUseCase(uuid),
                    getTasksUseCase(),
                    timeTrackingRepository.getActiveEntry(),
                    timeTrackingRepository.getEntriesForTask(uuid),
                    timeTrackingRepository.getTotalTimeForTask(uuid)
                ) { task, allTasks, activeEntry, timeEntries, totalTime ->
                    task to allTasks to activeEntry to timeEntries to totalTime
                }
                    .catch { exception ->
                        _uiState.value = TaskDetailUiState.Error(
                            message = exception.message ?: "Failed to load task"
                        )
                    }
                    .collect { data ->
                        val task = data.first.first.first.first
                        val allTasks = data.first.first.first.second
                        val activeEntry = data.first.first.second
                        val timeEntries = data.first.second
                        val totalTime = data.second

                        if (task == null) {
                            _uiState.value = TaskDetailUiState.Error(
                                message = "Task not found"
                            )
                        } else {
                            currentTask = task
                            val availableTags = allTasks.flatMap { it.tags }.distinct().sorted()

                            // Only show active entry if it belongs to this task
                            val relevantActiveEntry = if (activeEntry?.taskUuid == uuid) {
                                activeEntry
                            } else {
                                null
                            }

                            _uiState.value = TaskDetailUiState.Success(
                                task = task,
                                availableTags = availableTags,
                                allTasks = allTasks,
                                activeTimeEntry = relevantActiveEntry,
                                timeEntries = timeEntries,
                                totalTimeSpent = totalTime
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = TaskDetailUiState.Error(
                    message = e.message ?: "Failed to load task"
                )
            }
        }
    }

    /**
     * Update the task description in the current state.
     */
    private fun updateDescription(description: String) {
        updateCurrentTask { it.copy(description = description) }
    }

    /**
     * Update the task status in the current state.
     */
    private fun updateStatus(status: TaskStatus) {
        updateCurrentTask { it.copy(status = status) }
    }

    /**
     * Update the task priority in the current state.
     */
    private fun updatePriority(priority: TaskPriority?) {
        updateCurrentTask { it.copy(priority = priority) }
    }

    /**
     * Update the task due date in the current state.
     */
    private fun updateDueDate(due: Long?) {
        updateCurrentTask { it.copy(due = due) }
    }

    /**
     * Update the task project in the current state.
     */
    private fun updateProject(project: String?) {
        updateCurrentTask {
            it.copy(project = if (project.isNullOrBlank()) null else project)
        }
    }

    /**
     * Add a tag to the task.
     */
    private fun addTag(tag: String) {
        if (tag.isBlank()) return
        updateCurrentTask { task ->
            if (task.tags.contains(tag)) {
                task
            } else {
                task.copy(tags = task.tags + tag)
            }
        }
    }

    /**
     * Remove a tag from the task.
     */
    private fun removeTag(tag: String) {
        updateCurrentTask { task ->
            task.copy(tags = task.tags - tag)
        }
    }

    /**
     * Add a dependency to the task.
     */
    private fun addDependency(dependsOnUuid: String) {
        updateCurrentTask { task ->
            if (task.dependencies.contains(dependsOnUuid)) {
                task
            } else {
                task.copy(dependencies = task.dependencies + dependsOnUuid)
            }
        }
    }

    /**
     * Remove a dependency from the task.
     */
    private fun removeDependency(dependsOnUuid: String) {
        updateCurrentTask { task ->
            task.copy(dependencies = task.dependencies - dependsOnUuid)
        }
    }

    /**
     * Add an annotation to the task.
     */
    private fun addAnnotation(description: String) {
        if (description.isBlank()) return
        updateCurrentTask { task ->
            val newAnnotation = Annotation(
                id = System.currentTimeMillis(),
                taskUuid = task.uuid,
                timestamp = System.currentTimeMillis(),
                description = description
            )
            task.copy(annotations = task.annotations + newAnnotation)
        }
    }

    /**
     * Delete an annotation from the task.
     */
    private fun deleteAnnotation(annotationId: Long) {
        updateCurrentTask { task ->
            task.copy(
                annotations = task.annotations.filter { it.id != annotationId }
            )
        }
    }

    /**
     * Add or update a UDA (User Defined Attribute).
     */
    private fun addOrUpdateUda(key: String, value: Any?) {
        if (key.isBlank()) return
        updateCurrentTask { task ->
            val updatedUdas = task.udas.toMutableMap()
            updatedUdas[key] = value
            task.copy(udas = updatedUdas)
        }
    }

    /**
     * Delete a UDA from the task.
     */
    private fun deleteUda(key: String) {
        updateCurrentTask { task ->
            val updatedUdas = task.udas.toMutableMap()
            updatedUdas.remove(key)
            task.copy(udas = updatedUdas)
        }
    }

    /**
     * Save the current task to repository.
     */
    private fun saveTask() {
        viewModelScope.launch {
            val task = currentTask ?: return@launch
            val updatedTask = task.copy(modified = System.currentTimeMillis())

            val result = updateTaskUseCase(updatedTask)
            if (result.isSuccess) {
                _effect.emit(TaskDetailEffect.ShowSnackbar("Task saved successfully"))
                _effect.emit(TaskDetailEffect.NavigateBack)
            } else {
                _effect.emit(
                    TaskDetailEffect.ShowSnackbar(
                        result.exceptionOrNull()?.message ?: "Failed to save task"
                    )
                )
            }
        }
    }

    /**
     * Delete the current task.
     */
    private fun deleteTask() {
        viewModelScope.launch {
            val task = currentTask ?: return@launch

            val result = deleteTaskUseCase(task.uuid)
            if (result.isSuccess) {
                _effect.emit(TaskDetailEffect.ShowSnackbar("Task deleted successfully"))
                _effect.emit(TaskDetailEffect.NavigateBack)
            } else {
                _effect.emit(
                    TaskDetailEffect.ShowSnackbar(
                        result.exceptionOrNull()?.message ?: "Failed to delete task"
                    )
                )
            }
        }
    }

    /**
     * Start time tracking for the current task.
     */
    private fun startTimeTracking() {
        viewModelScope.launch {
            val task = currentTask ?: return@launch

            // Check if there's already an active time entry
            val state = _uiState.value
            if (state is TaskDetailUiState.Success && state.activeTimeEntry != null) {
                _effect.emit(TaskDetailEffect.ShowSnackbar("Time tracking already active"))
                return@launch
            }

            val result = timeTrackingRepository.startTracking(task.uuid)
            if (result.isSuccess) {
                _effect.emit(TaskDetailEffect.ShowSnackbar("Time tracking started"))
            } else {
                _effect.emit(
                    TaskDetailEffect.ShowSnackbar(
                        result.exceptionOrNull()?.message ?: "Failed to start time tracking"
                    )
                )
            }
        }
    }

    /**
     * Stop time tracking for the current task.
     */
    private fun stopTimeTracking() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state is TaskDetailUiState.Success) {
                val activeEntry = state.activeTimeEntry
                if (activeEntry == null) {
                    _effect.emit(TaskDetailEffect.ShowSnackbar("No active time tracking"))
                    return@launch
                }

                val result = timeTrackingRepository.stopTracking(activeEntry.id)
                if (result.isSuccess) {
                    _effect.emit(TaskDetailEffect.ShowSnackbar("Time tracking stopped"))
                } else {
                    _effect.emit(
                        TaskDetailEffect.ShowSnackbar(
                            result.exceptionOrNull()?.message ?: "Failed to stop time tracking"
                        )
                    )
                }
            }
        }
    }

    /**
     * Delete a time entry.
     */
    private fun deleteTimeEntry(entryId: Long) {
        viewModelScope.launch {
            val result = timeTrackingRepository.deleteEntry(entryId)
            if (result.isSuccess) {
                _effect.emit(TaskDetailEffect.ShowSnackbar("Time entry deleted"))
            } else {
                _effect.emit(
                    TaskDetailEffect.ShowSnackbar(
                        result.exceptionOrNull()?.message ?: "Failed to delete time entry"
                    )
                )
            }
        }
    }

    /**
     * Helper function to update the current task and UI state.
     */
    private fun updateCurrentTask(transform: (Task) -> Task) {
        val state = _uiState.value
        if (state is TaskDetailUiState.Success) {
            val updatedTask = transform(state.task)
            currentTask = updatedTask
            _uiState.value = state.copy(task = updatedTask)
        }
    }
}
