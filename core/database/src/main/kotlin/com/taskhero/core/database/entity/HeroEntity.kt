package com.taskhero.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "hero")
data class HeroEntity(
    @PrimaryKey
    val id: Int = 1,
    val displayName: String,
    val avatarUri: String? = null,
    val classType: String,
    val currentTitle: String,
    val level: Int = 1,
    val currentXp: Long = 0L,
    val totalXpEarned: Long = 0L,
    val strength: Int = 10,
    val dexterity: Int = 10,
    val constitution: Int = 10,
    val intelligence: Int = 10,
    val wisdom: Int = 10,
    val charisma: Int = 10,
    val tasksCompleted: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val createdAt: Long,
    val lastActivityDate: String? = null
)
