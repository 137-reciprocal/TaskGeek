package com.taskhero.domain.hero.model

import kotlinx.serialization.Serializable

@Serializable
data class XpReward(
    val baseXp: Long,
    val urgencyMultiplier: Double = 1.0,
    val completionBonus: Long = 0L
) {
    /**
     * Calculate total XP earned from base XP, urgency multiplier, and completion bonus
     */
    val totalXp: Long
        get() = (baseXp * urgencyMultiplier).toLong() + completionBonus

    /**
     * Get formatted XP string for display
     */
    fun getFormattedXp(): String {
        return totalXp.toString()
    }

    /**
     * Check if there's a bonus applied
     */
    fun hasBonus(): Boolean {
        return urgencyMultiplier > 1.0 || completionBonus > 0L
    }
}
