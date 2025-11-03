package com.taskhero.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "xp_history",
    indices = [
        Index("taskUuid")
    ]
)
data class XpHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskUuid: String,
    val xpEarned: Long,
    val timestamp: Long,
    val reason: String
)
