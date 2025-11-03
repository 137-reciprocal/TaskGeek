package com.taskhero.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.taskhero.domain.task.model.TaskStatus
import com.taskhero.domain.task.model.TaskPriority
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "tasks",
    indices = [
        Index("status"),
        Index("project"),
        Index("due"),
        Index("modified"),
        Index(value = ["status", "due"], name = "idx_status_due")
    ]
)
data class TaskEntity(
    @PrimaryKey
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
    val urgency: Double
)
