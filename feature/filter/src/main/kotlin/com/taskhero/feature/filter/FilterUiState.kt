package com.taskhero.feature.filter

import com.taskhero.domain.task.model.TaskFilter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * UI State for Filter screen following MVI pattern.
 * Represents all possible states of the filter builder UI.
 */
data class FilterUiState(
    val currentFilter: TaskFilter = TaskFilter(),
    val availableProjects: ImmutableList<String> = persistentListOf(),
    val availableTags: ImmutableList<String> = persistentListOf(),
    val matchingTaskCount: Int = 0,
    val isLoading: Boolean = false,
    val dueFromDate: Long? = null,
    val dueToDate: Long? = null,
    val urgencyMin: Float = 0f,
    val urgencyMax: Float = 20f
)
