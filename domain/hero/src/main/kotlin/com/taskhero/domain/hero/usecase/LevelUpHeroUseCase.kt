package com.taskhero.domain.hero.usecase

import com.taskhero.domain.hero.model.Hero
import com.taskhero.domain.hero.model.HeroStats
import com.taskhero.domain.hero.model.LevelUpInfo
import javax.inject.Inject
import kotlin.math.pow

/**
 * Use case to handle hero level up logic.
 *
 * Calculates:
 * - XP required for next level using polynomial curve: baseXp * level^3
 * - Stat increases based on level milestones
 * - Title unlocks at specific levels
 */
class LevelUpHeroUseCase @Inject constructor() {

    companion object {
        // Base XP for level calculation
        private const val BASE_XP = 100L

        // Stat increase per level
        private const val STATS_PER_LEVEL = 1

        // Title unlocks
        private val TITLE_UNLOCKS = mapOf(
            5 to "Apprentice",
            10 to "Adept",
            15 to "Expert",
            20 to "Master",
            25 to "Grandmaster",
            30 to "Legend",
            40 to "Mythic",
            50 to "Immortal"
        )
    }

    /**
     * Calculate XP required for a specific level using polynomial formula.
     * Formula: BASE_XP * level^3
     */
    fun calculateXpForLevel(level: Int): Long {
        return (BASE_XP * level.toDouble().pow(3.0)).toLong()
    }

    /**
     * Handle level up and return level up information.
     */
    operator fun invoke(hero: Hero): LevelUpInfo {
        val newLevel = hero.level + 1

        // Calculate stat increases
        // Every level gives 1 point to each stat
        val statIncreases = HeroStats(
            strength = STATS_PER_LEVEL,
            dexterity = STATS_PER_LEVEL,
            constitution = STATS_PER_LEVEL,
            intelligence = STATS_PER_LEVEL,
            wisdom = STATS_PER_LEVEL,
            charisma = STATS_PER_LEVEL
        )

        // Check for title unlock
        val newTitle = TITLE_UNLOCKS[newLevel]

        // Calculate XP needed for next level
        val xpForNextLevel = calculateXpForLevel(newLevel + 1)

        return LevelUpInfo(
            oldLevel = hero.level,
            newLevel = newLevel,
            xpForNextLevel = xpForNextLevel,
            statIncreases = statIncreases,
            newTitle = newTitle
        )
    }
}
