package com.taskhero.domain.hero.model

import kotlinx.serialization.Serializable

/**
 * Information about a level up event.
 */
@Serializable
data class LevelUpInfo(
    val oldLevel: Int,
    val newLevel: Int,
    val xpForNextLevel: Long,
    val statIncreases: HeroStats,
    val newTitle: String? = null
)
