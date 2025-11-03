package com.taskhero.core.database.converter

import androidx.room.TypeConverter
import com.taskhero.domain.task.model.TaskStatus

/**
 * Room type converter for TaskStatus enum
 * Converts between enum and String for database storage
 */
class TaskStatusConverter {
    @TypeConverter
    fun fromTaskStatus(status: TaskStatus): String {
        return status.name
    }

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus {
        return try {
            TaskStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            TaskStatus.PENDING // Default fallback
        }
    }
}
