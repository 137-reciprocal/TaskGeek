package com.taskhero.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "annotations",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["uuid"],
            childColumns = ["taskUuid"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("taskUuid")
    ]
)
data class AnnotationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskUuid: String,
    val timestamp: Long,
    val description: String
)
