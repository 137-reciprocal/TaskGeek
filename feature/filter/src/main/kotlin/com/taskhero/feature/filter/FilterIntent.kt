package com.taskhero.feature.filter

import com.taskhero.domain.task.model.TaskPriority
import com.taskhero.domain.task.model.TaskStatus

/**
 * User intents for Filter screen following MVI pattern.
 * Represents all possible user actions on the filter builder.
 */
sealed interface FilterIntent {
    /**
     * Update the status filter.
     */
    data class UpdateStatus(val status: TaskStatus?) : FilterIntent

    /**
     * Update the list of statuses to filter by (for multi-select).
     */
    data class UpdateStatuses(val statuses: List<TaskStatus>) : FilterIntent

    /**
     * Update the project filter.
     */
    data class UpdateProject(val project: String?) : FilterIntent

    /**
     * Add a tag to the filter.
     */
    data class AddTag(val tag: String) : FilterIntent

    /**
     * Remove a tag from the filter.
     */
    data class RemoveTag(val tag: String) : FilterIntent

    /**
     * Update priority filters.
     */
    data class UpdatePriorities(val priorities: List<TaskPriority>) : FilterIntent

    /**
     * Update due date range (from date).
     */
    data class UpdateDueFromDate(val date: Long?) : FilterIntent

    /**
     * Update due date range (to date).
     */
    data class UpdateDueToDate(val date: Long?) : FilterIntent

    /**
     * Update urgency minimum value.
     */
    data class UpdateUrgencyMin(val min: Float) : FilterIntent

    /**
     * Update urgency maximum value.
     */
    data class UpdateUrgencyMax(val max: Float) : FilterIntent

    /**
     * Clear all filters and reset to default.
     */
    data object ClearFilter : FilterIntent

    /**
     * Apply the current filter.
     */
    data object ApplyFilter : FilterIntent

    /**
     * Save the current filter as a preset.
     */
    data class SavePreset(val name: String) : FilterIntent

    /**
     * Load a filter preset.
     */
    data class LoadPreset(val presetId: String) : FilterIntent
}
