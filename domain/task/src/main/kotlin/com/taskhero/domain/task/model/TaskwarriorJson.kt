package com.taskhero.domain.task.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Taskwarrior JSON format data class.
 * Matches the Taskwarrior JSON schema for import/export.
 */
@Serializable
data class TaskwarriorJson(
    val uuid: String,
    val description: String,
    val status: String,
    val entry: String,
    val modified: String? = null,
    val start: String? = null,
    val end: String? = null,
    val due: String? = null,
    val wait: String? = null,
    val scheduled: String? = null,
    val until: String? = null,
    val project: String? = null,
    val priority: String? = null,
    val recur: String? = null,
    val parent: String? = null,
    val imask: Int? = null,
    val mask: String? = null,
    val urgency: Double? = null,
    val tags: List<String>? = null,
    val annotations: List<TaskwarriorAnnotation>? = null,
    val depends: String? = null // Comma-separated UUIDs
) {
    companion object {
        /**
         * Convert Task domain model to Taskwarrior JSON format.
         */
        fun fromTask(task: Task): TaskwarriorJson {
            return TaskwarriorJson(
                uuid = task.uuid,
                description = task.description,
                status = task.status.name.lowercase(),
                entry = formatTimestamp(task.entry),
                modified = task.modified?.let { formatTimestamp(it) },
                start = task.start?.let { formatTimestamp(it) },
                end = task.end?.let { formatTimestamp(it) },
                due = task.due?.let { formatTimestamp(it) },
                wait = task.wait?.let { formatTimestamp(it) },
                scheduled = task.scheduled?.let { formatTimestamp(it) },
                until = task.until?.let { formatTimestamp(it) },
                project = task.project,
                priority = task.priority?.name?.first()?.toString()?.uppercase(), // H, M, L
                recur = task.recur,
                parent = task.parent,
                imask = task.imask,
                mask = task.mask,
                urgency = task.urgency,
                tags = if (task.tags.isNotEmpty()) task.tags else null,
                annotations = if (task.annotations.isNotEmpty()) {
                    task.annotations.map { TaskwarriorAnnotation.fromAnnotation(it) }
                } else null,
                depends = if (task.dependencies.isNotEmpty()) {
                    task.dependencies.joinToString(",")
                } else null
            )
        }

        /**
         * Convert Taskwarrior JSON format to Task domain model.
         */
        fun toTask(json: TaskwarriorJson): Task {
            return Task(
                uuid = json.uuid,
                description = json.description,
                status = TaskStatus.valueOf(json.status.uppercase()),
                entry = parseTimestamp(json.entry),
                modified = json.modified?.let { parseTimestamp(it) },
                start = json.start?.let { parseTimestamp(it) },
                end = json.end?.let { parseTimestamp(it) },
                due = json.due?.let { parseTimestamp(it) },
                wait = json.wait?.let { parseTimestamp(it) },
                scheduled = json.scheduled?.let { parseTimestamp(it) },
                until = json.until?.let { parseTimestamp(it) },
                project = json.project,
                priority = json.priority?.let { parsePriority(it) },
                recur = json.recur,
                parent = json.parent,
                imask = json.imask,
                mask = json.mask,
                urgency = json.urgency ?: 0.0,
                tags = json.tags ?: emptyList(),
                annotations = json.annotations?.map { it.toAnnotation(json.uuid) } ?: emptyList(),
                dependencies = json.depends?.split(",")?.map { it.trim() } ?: emptyList(),
                udas = emptyMap() // UDAs not supported yet
            )
        }

        /**
         * Format timestamp from epoch seconds to Taskwarrior format (YYYYMMDDTHHmmssZ).
         */
        private fun formatTimestamp(epochSeconds: Long): String {
            val instant = java.time.Instant.ofEpochSecond(epochSeconds)
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                .withZone(java.time.ZoneOffset.UTC)
            return formatter.format(instant)
        }

        /**
         * Parse timestamp from Taskwarrior format to epoch seconds.
         */
        private fun parseTimestamp(timestamp: String): Long {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                .withZone(java.time.ZoneOffset.UTC)
            return java.time.Instant.from(formatter.parse(timestamp)).epochSecond
        }

        /**
         * Parse priority from Taskwarrior format (H, M, L) to TaskPriority enum.
         */
        private fun parsePriority(priority: String): TaskPriority {
            return when (priority.uppercase()) {
                "H" -> TaskPriority.HIGH
                "M" -> TaskPriority.MEDIUM
                "L" -> TaskPriority.LOW
                else -> TaskPriority.NONE
            }
        }
    }
}

/**
 * Taskwarrior annotation JSON format.
 */
@Serializable
data class TaskwarriorAnnotation(
    val entry: String,
    val description: String
) {
    companion object {
        /**
         * Convert Annotation domain model to Taskwarrior JSON format.
         */
        fun fromAnnotation(annotation: Annotation): TaskwarriorAnnotation {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                .withZone(java.time.ZoneOffset.UTC)
            val timestamp = java.time.Instant.ofEpochSecond(annotation.timestamp)
            return TaskwarriorAnnotation(
                entry = formatter.format(timestamp),
                description = annotation.description
            )
        }

        /**
         * Convert Taskwarrior JSON format to Annotation domain model.
         * Note: This will generate a new ID as Taskwarrior doesn't store annotation IDs.
         *
         * @param taskUuid The UUID of the task this annotation belongs to
         */
        fun TaskwarriorAnnotation.toAnnotation(taskUuid: String): Annotation {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                .withZone(java.time.ZoneOffset.UTC)
            val timestamp = java.time.Instant.from(formatter.parse(entry)).epochSecond
            return Annotation(
                id = 0, // Will be auto-generated by database
                taskUuid = taskUuid,
                timestamp = timestamp,
                description = description
            )
        }
    }
}
