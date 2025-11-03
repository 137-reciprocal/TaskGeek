package com.taskhero.core.database.converter

import androidx.room.TypeConverter
import com.taskhero.domain.task.model.TaskPriority

/**
 * Room type converter for TaskPriority enum
 * Converts between enum and String for database storage
 */
class TaskPriorityConverter {
    @TypeConverter
    fun fromTaskPriority(priority: TaskPriority?): String? {
        return priority?.name
    }

    @TypeConverter
    fun toTaskPriority(value: String?): TaskPriority? {
        return value?.let {
            try {
                TaskPriority.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null // Return null for invalid values
            }
        }
    }
}
