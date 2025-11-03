package com.taskhero.domain.task.model

import kotlinx.serialization.Serializable

@Serializable
data class TaskFilter(
    val uuid: String? = null,
    val description: String? = null,
    val status: TaskStatus? = null,
    val entryStarting: Long? = null,
    val entryUntil: Long? = null,
    val modified: Long? = null,
    val start: Long? = null,
    val end: Long? = null,
    val due: Long? = null,
    val wait: Long? = null,
    val scheduled: Long? = null,
    val until: Long? = null,
    val project: String? = null,
    val priority: TaskPriority? = null,
    val recur: String? = null,
    val parent: String? = null,
    val urgencyMin: Double? = null,
    val urgencyMax: Double? = null,
    val tags: List<String> = emptyList(),
    val dependencies: List<String> = emptyList(),
    val statuses: List<TaskStatus> = emptyList()
)
