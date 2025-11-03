package com.taskhero.domain.task.model

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val uuid: String,
    val description: String,
    val status: TaskStatus,
    val entry: Long,
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
    val imask: Int? = null,
    val mask: String? = null,
    val urgency: Double,
    val tags: List<String> = emptyList(),
    val annotations: List<Annotation> = emptyList(),
    val dependencies: List<String> = emptyList(),
    val udas: Map<String, Any?> = emptyMap()
)
