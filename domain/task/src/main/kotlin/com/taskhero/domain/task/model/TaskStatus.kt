package com.taskhero.domain.task.model

import kotlinx.serialization.Serializable

@Serializable
enum class TaskStatus {
    PENDING,
    COMPLETED,
    DELETED,
    WAITING,
    RECURRING
}
