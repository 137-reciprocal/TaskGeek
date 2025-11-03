package com.taskhero.domain.filter.model

import com.taskhero.domain.task.model.TaskFilter
import kotlinx.serialization.Serializable

/**
 * Represents a saved filter preset that can be reused.
 *
 * @property id Unique identifier for the preset
 * @property name User-friendly name for the preset
 * @property filter The filter configuration
 * @property createdAt Timestamp when preset was created
 */
@Serializable
data class FilterPreset(
    val id: String,
    val name: String,
    val filter: TaskFilter,
    val createdAt: Long
)
