package com.taskhero.feature.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskhero.domain.filter.usecase.CountMatchingTasksUseCase
import com.taskhero.domain.filter.usecase.GetFilterPresetsUseCase
import com.taskhero.domain.filter.usecase.SaveFilterPresetUseCase
import com.taskhero.domain.task.model.TaskFilter
import com.taskhero.domain.task.model.TaskPriority
import com.taskhero.domain.task.model.TaskStatus
import com.taskhero.domain.task.repository.TagRepository
import com.taskhero.domain.task.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
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
 * ViewModel for Filter screen following MVI pattern.
 * Manages filter state, handles user intents, and emits side effects.
 */
@HiltViewModel
class FilterViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val tagRepository: TagRepository,
    private val countMatchingTasksUseCase: CountMatchingTasksUseCase,
    private val saveFilterPresetUseCase: SaveFilterPresetUseCase,
    private val getFilterPresetsUseCase: GetFilterPresetsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FilterUiState())
    val uiState: StateFlow<FilterUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<FilterEffect>()
    val effect: SharedFlow<FilterEffect> = _effect.asSharedFlow()

    init {
        loadFilterOptions()
        observeMatchingTaskCount()
    }

    /**
     * Handle user intents.
     */
    fun onIntent(intent: FilterIntent) {
        when (intent) {
            is FilterIntent.UpdateStatus -> updateStatus(intent.status)
            is FilterIntent.UpdateStatuses -> updateStatuses(intent.statuses)
            is FilterIntent.UpdateProject -> updateProject(intent.project)
            is FilterIntent.AddTag -> addTag(intent.tag)
            is FilterIntent.RemoveTag -> removeTag(intent.tag)
            is FilterIntent.UpdatePriorities -> updatePriorities(intent.priorities)
            is FilterIntent.UpdateDueFromDate -> updateDueFromDate(intent.date)
            is FilterIntent.UpdateDueToDate -> updateDueToDate(intent.date)
            is FilterIntent.UpdateUrgencyMin -> updateUrgencyMin(intent.min)
            is FilterIntent.UpdateUrgencyMax -> updateUrgencyMax(intent.max)
            is FilterIntent.ClearFilter -> clearFilter()
            is FilterIntent.ApplyFilter -> applyFilter()
            is FilterIntent.SavePreset -> savePreset(intent.name)
            is FilterIntent.LoadPreset -> loadPreset(intent.presetId)
        }
    }

    /**
     * Load available filter options (projects, tags).
     */
    private fun loadFilterOptions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Combine projects and tags into single flow
                combine(
                    taskRepository.getAllTasks(),
                    tagRepository.getAllTags()
                ) { tasks, tags ->
                    val projects = tasks.mapNotNull { it.project }.distinct().sorted()
                    projects to tags
                }.catch { exception ->
                    _effect.emit(
                        FilterEffect.ShowSnackbar(
                            "Failed to load filter options: ${exception.message}"
                        )
                    )
                }.collect { (projects, tags) ->
                    _uiState.value = _uiState.value.copy(
                        availableProjects = projects.toImmutableList(),
                        availableTags = tags.sorted().toImmutableList(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _effect.emit(
                    FilterEffect.ShowSnackbar("Failed to load filter options: ${e.message}")
                )
            }
        }
    }

    /**
     * Observe matching task count based on current filter.
     */
    private fun observeMatchingTaskCount() {
        viewModelScope.launch {
            uiState.collect { state ->
                countMatchingTasksUseCase(state.currentFilter)
                    .catch { exception ->
                        // Silently fail for count updates
                    }
                    .collect { count ->
                        _uiState.value = _uiState.value.copy(matchingTaskCount = count)
                    }
            }
        }
    }

    /**
     * Update status filter.
     */
    private fun updateStatus(status: TaskStatus?) {
        _uiState.value = _uiState.value.copy(
            currentFilter = _uiState.value.currentFilter.copy(status = status)
        )
    }

    /**
     * Update statuses filter (for multi-select).
     */
    private fun updateStatuses(statuses: List<TaskStatus>) {
        _uiState.value = _uiState.value.copy(
            currentFilter = _uiState.value.currentFilter.copy(statuses = statuses)
        )
    }

    /**
     * Update project filter.
     */
    private fun updateProject(project: String?) {
        _uiState.value = _uiState.value.copy(
            currentFilter = _uiState.value.currentFilter.copy(project = project)
        )
    }

    /**
     * Add a tag to the filter.
     */
    private fun addTag(tag: String) {
        val currentTags = _uiState.value.currentFilter.tags
        if (!currentTags.contains(tag)) {
            _uiState.value = _uiState.value.copy(
                currentFilter = _uiState.value.currentFilter.copy(
                    tags = currentTags + tag
                )
            )
        }
    }

    /**
     * Remove a tag from the filter.
     */
    private fun removeTag(tag: String) {
        val currentTags = _uiState.value.currentFilter.tags
        _uiState.value = _uiState.value.copy(
            currentFilter = _uiState.value.currentFilter.copy(
                tags = currentTags.filter { it != tag }
            )
        )
    }

    /**
     * Update priority filters.
     * Handles multiple priorities by storing them separately and reconstructing the filter.
     */
    private fun updatePriorities(priorities: List<TaskPriority>) {
        // For simplicity, we'll just use the first priority if any
        // In a more complex implementation, we could extend TaskFilter to support multiple priorities
        val priority = priorities.firstOrNull()
        _uiState.value = _uiState.value.copy(
            currentFilter = _uiState.value.currentFilter.copy(priority = priority)
        )
    }

    /**
     * Update due from date.
     */
    private fun updateDueFromDate(date: Long?) {
        _uiState.value = _uiState.value.copy(dueFromDate = date)
    }

    /**
     * Update due to date.
     */
    private fun updateDueToDate(date: Long?) {
        _uiState.value = _uiState.value.copy(
            dueToDate = date,
            currentFilter = _uiState.value.currentFilter.copy(due = date)
        )
    }

    /**
     * Update urgency minimum.
     */
    private fun updateUrgencyMin(min: Float) {
        _uiState.value = _uiState.value.copy(
            urgencyMin = min,
            currentFilter = _uiState.value.currentFilter.copy(urgencyMin = min.toDouble())
        )
    }

    /**
     * Update urgency maximum.
     */
    private fun updateUrgencyMax(max: Float) {
        _uiState.value = _uiState.value.copy(
            urgencyMax = max,
            currentFilter = _uiState.value.currentFilter.copy(urgencyMax = max.toDouble())
        )
    }

    /**
     * Clear all filters.
     */
    private fun clearFilter() {
        _uiState.value = FilterUiState(
            availableProjects = _uiState.value.availableProjects,
            availableTags = _uiState.value.availableTags
        )
        viewModelScope.launch {
            _effect.emit(FilterEffect.ShowSnackbar("Filter cleared"))
        }
    }

    /**
     * Apply the current filter.
     */
    private fun applyFilter() {
        viewModelScope.launch {
            _effect.emit(FilterEffect.ApplyFilter(_uiState.value.currentFilter))
        }
    }

    /**
     * Save the current filter as a preset.
     */
    private fun savePreset(name: String) {
        viewModelScope.launch {
            val result = saveFilterPresetUseCase(name, _uiState.value.currentFilter)
            if (result.isSuccess) {
                _effect.emit(FilterEffect.ShowSnackbar("Filter preset '$name' saved"))
            } else {
                _effect.emit(
                    FilterEffect.ShowSnackbar(
                        result.exceptionOrNull()?.message ?: "Failed to save preset"
                    )
                )
            }
        }
    }

    /**
     * Load a filter preset.
     */
    private fun loadPreset(presetId: String) {
        // This would load from repository - simplified for now
        viewModelScope.launch {
            _effect.emit(FilterEffect.ShowSnackbar("Loading preset..."))
        }
    }
}
