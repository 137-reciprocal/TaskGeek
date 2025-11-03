package com.taskhero.domain.task.model

import kotlinx.serialization.Serializable

@Serializable
data class Annotation(
    val id: Long,
    val taskUuid: String,
    val timestamp: Long,
    val description: String
)
