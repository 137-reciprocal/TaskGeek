package com.taskhero.feature.taskdetail

import com.taskhero.domain.task.model.TaskPriority
import com.taskhero.domain.task.model.TaskStatus

/**
 * User intents for TaskDetail screen following MVI pattern.
 * Represents all possible user actions on the task detail screen.
 */
sealed interface TaskDetailIntent {
    /**
     * Load task data by UUID.
     *
     * @property uuid The UUID of the task to load
     */
    data class LoadTask(val uuid: String) : TaskDetailIntent

    /**
     * Update the task description.
     *
     * @property description The new description
     */
    data class UpdateDescription(val description: String) : TaskDetailIntent

    /**
     * Update the task status.
     *
     * @property status The new status
     */
    data class UpdateStatus(val status: TaskStatus) : TaskDetailIntent

    /**
     * Update the task priority.
     *
     * @property priority The new priority (null to remove)
     */
    data class UpdatePriority(val priority: TaskPriority?) : TaskDetailIntent

    /**
     * Update the task due date.
     *
     * @property due The new due date timestamp (null to remove)
     */
    data class UpdateDueDate(val due: Long?) : TaskDetailIntent

    /**
     * Update the task project.
     *
     * @property project The new project name (null to remove)
     */
    data class UpdateProject(val project: String?) : TaskDetailIntent

    /**
     * Add a tag to the task.
     *
     * @property tag The tag to add
     */
    data class AddTag(val tag: String) : TaskDetailIntent

    /**
     * Remove a tag from the task.
     *
     * @property tag The tag to remove
     */
    data class RemoveTag(val tag: String) : TaskDetailIntent

    /**
     * Add a dependency to the task.
     *
     * @property dependsOnUuid The UUID of the task this task depends on
     */
    data class AddDependency(val dependsOnUuid: String) : TaskDetailIntent

    /**
     * Remove a dependency from the task.
     *
     * @property dependsOnUuid The UUID of the dependency to remove
     */
    data class RemoveDependency(val dependsOnUuid: String) : TaskDetailIntent

    /**
     * Add an annotation to the task.
     *
     * @property description The annotation description
     */
    data class AddAnnotation(val description: String) : TaskDetailIntent

    /**
     * Delete an annotation from the task.
     *
     * @property annotationId The ID of the annotation to delete
     */
    data class DeleteAnnotation(val annotationId: Long) : TaskDetailIntent

    /**
     * Add or update a UDA (User Defined Attribute).
     *
     * @property key The UDA key
     * @property value The UDA value (can be null)
     */
    data class AddOrUpdateUda(val key: String, val value: Any?) : TaskDetailIntent

    /**
     * Delete a UDA from the task.
     *
     * @property key The UDA key to delete
     */
    data class DeleteUda(val key: String) : TaskDetailIntent

    /**
     * Save the current task changes.
     */
    data object SaveTask : TaskDetailIntent

    /**
     * Delete the current task.
     */
    data object DeleteTask : TaskDetailIntent

    /**
     * Start time tracking for the current task.
     */
    data object StartTimeTracking : TaskDetailIntent

    /**
     * Stop time tracking for the current task.
     */
    data object StopTimeTracking : TaskDetailIntent

    /**
     * Delete a time entry.
     *
     * @property entryId The ID of the time entry to delete
     */
    data class DeleteTimeEntry(val entryId: Long) : TaskDetailIntent
}
