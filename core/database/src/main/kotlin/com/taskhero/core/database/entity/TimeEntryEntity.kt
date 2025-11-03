package com.taskhero.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for time tracking entries.
 * Stores time tracking information for tasks with foreign key relationship.
 */
@Entity(
    tableName = "time_entries",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["uuid"],
            childColumns = ["task_uuid"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("task_uuid"),
        Index("start_time"),
        Index("end_time")
    ]
)
data class TimeEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "task_uuid")
    val taskUuid: String,

    @ColumnInfo(name = "start_time")
    val startTime: Long,

    @ColumnInfo(name = "end_time")
    val endTime: Long? = null,

    @ColumnInfo(name = "tags")
    val tags: String = "" // Comma-separated tags
)
