package com.taskhero.domain.timetracking.model

/**
 * Represents a time tracking entry for a task.
 *
 * @property id Unique identifier for the time entry
 * @property taskUuid UUID of the associated task
 * @property startTime Start time in milliseconds since epoch
 * @property endTime End time in milliseconds since epoch (null if currently running)
 * @property tags Additional tags for categorization
 */
data class TimeEntry(
    val id: Long,
    val taskUuid: String,
    val startTime: Long,
    val endTime: Long? = null,
    val tags: List<String> = emptyList()
) {
    /**
     * Calculates the duration of this time entry in milliseconds.
     * For active entries (endTime is null), uses current time.
     */
    fun getDuration(): Long {
        val end = endTime ?: System.currentTimeMillis()
        return end - startTime
    }

    /**
     * Checks if this time entry is currently active (not stopped).
     */
    fun isActive(): Boolean = endTime == null

    /**
     * Formats the duration as a human-readable string (HH:MM:SS).
     */
    fun formatDuration(): String {
        val duration = getDuration()
        val hours = duration / 3600000
        val minutes = (duration % 3600000) / 60000
        val seconds = (duration % 60000) / 1000
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
