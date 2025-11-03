package com.taskhero.domain.hero.model

import kotlinx.serialization.Serializable

@Serializable
data class HeroStats(
    val strength: Int = 10,
    val dexterity: Int = 10,
    val constitution: Int = 10,
    val intelligence: Int = 10,
    val wisdom: Int = 10,
    val charisma: Int = 10
) {
    /**
     * Get the total sum of all stats
     */
    fun getTotalStats(): Int {
        return strength + dexterity + constitution + intelligence + wisdom + charisma
    }

    /**
     * Calculate ability modifier from a stat value
     * D&D standard: modifier = (stat - 10) / 2 (rounded down)
     */
    fun getModifier(stat: Int): Int {
        return (stat - 10) / 2
    }

    /**
     * Get modifier for a specific stat type
     */
    fun getModifierForStat(statType: StatType): Int {
        val stat = when (statType) {
            StatType.STRENGTH -> strength
            StatType.DEXTERITY -> dexterity
            StatType.CONSTITUTION -> constitution
            StatType.INTELLIGENCE -> intelligence
            StatType.WISDOM -> wisdom
            StatType.CHARISMA -> charisma
        }
        return getModifier(stat)
    }

    /**
     * Get the stat value by type
     */
    fun getStatValue(statType: StatType): Int {
        return when (statType) {
            StatType.STRENGTH -> strength
            StatType.DEXTERITY -> dexterity
            StatType.CONSTITUTION -> constitution
            StatType.INTELLIGENCE -> intelligence
            StatType.WISDOM -> wisdom
            StatType.CHARISMA -> charisma
        }
    }
}
