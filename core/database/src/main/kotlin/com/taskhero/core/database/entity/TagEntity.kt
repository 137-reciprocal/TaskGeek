package com.taskhero.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey
    val name: String
)
