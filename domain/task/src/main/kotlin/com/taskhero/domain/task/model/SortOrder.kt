package com.taskhero.domain.task.model

import kotlinx.serialization.Serializable

@Serializable
enum class SortOrder {
    URGENCY,
    DUE_DATE,
    CREATED,
    MODIFIED,
    PROJECT,
    PRIORITY
}
