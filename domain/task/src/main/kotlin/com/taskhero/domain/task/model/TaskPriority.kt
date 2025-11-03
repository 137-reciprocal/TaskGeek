package com.taskhero.domain.task.model

import kotlinx.serialization.Serializable

@Serializable
enum class TaskPriority {
    HIGH,
    MEDIUM,
    LOW,
    NONE
}
