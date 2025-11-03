package com.taskhero.data.hero.mapper

import com.taskhero.core.database.entity.HeroEntity
import com.taskhero.domain.hero.model.Hero

/**
 * Mapper object for converting between HeroEntity and Hero domain model.
 */
object HeroMapper {
    /**
     * Convert HeroEntity to Hero domain model.
     *
     * @return Hero domain model
     */
    fun HeroEntity.toDomain(): Hero {
        return Hero(
            id = id,
            displayName = displayName,
            avatarUri = avatarUri,
            classType = classType,
            currentTitle = currentTitle,
            level = level,
            currentXp = currentXp,
            totalXpEarned = totalXpEarned,
            strength = strength,
            dexterity = dexterity,
            constitution = constitution,
            intelligence = intelligence,
            wisdom = wisdom,
            charisma = charisma,
            tasksCompleted = tasksCompleted,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            createdAt = createdAt,
            lastActivityDate = lastActivityDate
        )
    }

    /**
     * Convert Hero domain model to HeroEntity.
     *
     * @return HeroEntity for database storage
     */
    fun Hero.toEntity(): HeroEntity {
        return HeroEntity(
            id = id,
            displayName = displayName,
            avatarUri = avatarUri,
            classType = classType,
            currentTitle = currentTitle,
            level = level,
            currentXp = currentXp,
            totalXpEarned = totalXpEarned,
            strength = strength,
            dexterity = dexterity,
            constitution = constitution,
            intelligence = intelligence,
            wisdom = wisdom,
            charisma = charisma,
            tasksCompleted = tasksCompleted,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            createdAt = createdAt,
            lastActivityDate = lastActivityDate
        )
    }
}
