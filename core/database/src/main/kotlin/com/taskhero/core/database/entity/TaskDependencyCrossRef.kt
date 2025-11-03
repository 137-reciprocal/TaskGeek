package com.taskhero.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "task_dependencies",
    primaryKeys = ["taskUuid", "dependsOnUuid"],
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["uuid"],
            childColumns = ["taskUuid"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["uuid"],
            childColumns = ["dependsOnUuid"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("dependsOnUuid")
    ]
)
data class TaskDependencyCrossRef(
    val taskUuid: String,
    val dependsOnUuid: String
)
