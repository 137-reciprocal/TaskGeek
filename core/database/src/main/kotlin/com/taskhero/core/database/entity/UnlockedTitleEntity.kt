package com.taskhero.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "unlocked_titles")
data class UnlockedTitleEntity(
    @PrimaryKey
    val titleId: String,
    val unlockedAt: Long
)
