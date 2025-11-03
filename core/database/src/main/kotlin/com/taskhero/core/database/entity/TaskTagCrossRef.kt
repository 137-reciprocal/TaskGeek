package com.taskhero.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "task_tags",
    primaryKeys = ["taskUuid", "tagName"],
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["uuid"],
            childColumns = ["taskUuid"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["name"],
            childColumns = ["tagName"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("tagName")
    ]
)
data class TaskTagCrossRef(
    val taskUuid: String,
    val tagName: String
)
