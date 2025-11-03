package com.taskhero.data.task.mapper

import com.taskhero.core.database.entity.TaskEntity
import com.taskhero.domain.task.model.Annotation
import com.taskhero.domain.task.model.Task

/**
 * Mapper object for converting between TaskEntity and Task domain model.
 */
object TaskMapper {
    /**
     * Convert TaskEntity to Task domain model.
     *
     * @param tags List of tag names associated with this task
     * @param annotations List of annotations for this task
     * @param dependencies List of UUIDs of tasks this task depends on
     * @return Task domain model
     */
    fun TaskEntity.toDomain(
        tags: List<String>,
        annotations: List<Annotation>,
        dependencies: List<String>
    ): Task {
        return Task(
            uuid = uuid,
            description = description,
            status = status,
            entry = entry,
            modified = modified,
            start = start,
            end = end,
            due = due,
            wait = wait,
            scheduled = scheduled,
            until = until,
            project = project,
            priority = priority,
            recur = recur,
            parent = parent,
            imask = imask,
            mask = mask,
            urgency = urgency,
            tags = tags,
            annotations = annotations,
            dependencies = dependencies,
            udas = emptyMap() // UDAs not stored in entity yet
        )
    }

    /**
     * Convert Task domain model to TaskEntity.
     * Note: tags, annotations, and dependencies are stored in separate tables
     * and must be handled separately.
     *
     * @return TaskEntity for database storage
     */
    fun Task.toEntity(): TaskEntity {
        return TaskEntity(
            uuid = uuid,
            description = description,
            status = status,
            entry = entry,
            modified = modified,
            start = start,
            end = end,
            due = due,
            wait = wait,
            scheduled = scheduled,
            until = until,
            project = project,
            priority = priority,
            recur = recur,
            parent = parent,
            imask = imask,
            mask = mask,
            urgency = urgency
        )
    }
}
