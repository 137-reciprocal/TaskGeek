package com.taskhero.domain.hero.model

import kotlinx.serialization.Serializable

@Serializable
data class Hero(
    val id: Int = HERO_ID,
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
) {
    companion object {
        const val HERO_ID = 1
    }

    /**
     * Get the hero's stats as a HeroStats object
     */
    fun getStats(): HeroStats {
        return HeroStats(
            strength = strength,
            dexterity = dexterity,
            constitution = constitution,
            intelligence = intelligence,
            wisdom = wisdom,
            charisma = charisma
        )
    }

    /**
     * Calculate the XP needed to reach the next level
     * Formula: baseXp * (level + 1)
     */
    fun getXpToNextLevel(): Long {
        val baseXpPerLevel = 1000L
        return baseXpPerLevel * (level + 1)
    }

    /**
     * Get the remaining XP needed to level up
     */
    fun getRemainingXpToNextLevel(): Long {
        val xpNeeded = getXpToNextLevel()
        return maxOf(0L, xpNeeded - currentXp)
    }

    /**
     * Get the progress to the next level as a percentage (0-100)
     */
    fun getXpProgress(): Float {
        val xpNeeded = getXpToNextLevel()
        return if (xpNeeded > 0) {
            ((currentXp.toFloat() / xpNeeded) * 100).coerceIn(0f, 100f)
        } else {
            0f
        }
    }

    /**
     * Check if hero is at max stats (all stats at 20)
     */
    fun isAtMaxStats(): Boolean {
        return strength >= 20 && dexterity >= 20 && constitution >= 20 &&
                intelligence >= 20 && wisdom >= 20 && charisma >= 20
    }

    /**
     * Get total stats sum
     */
    fun getTotalStats(): Int {
        return strength + dexterity + constitution + intelligence + wisdom + charisma
    }

    /**
     * Create a copy with updated XP and level
     */
    fun addXp(xpAmount: Long): Hero {
        val newTotalXp = totalXpEarned + xpAmount
        val newCurrentXp = currentXp + xpAmount
        var newLevel = level
        var newLevelUpXp = newCurrentXp

        // Check for level up
        while (newLevelUpXp >= getXpToNextLevel()) {
            newLevelUpXp -= getXpToNextLevel()
            newLevel++
        }

        return this.copy(
            totalXpEarned = newTotalXp,
            currentXp = newLevelUpXp,
            level = newLevel
        )
    }
}
