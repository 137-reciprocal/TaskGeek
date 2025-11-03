package com.taskhero.domain.hero.model

import kotlinx.serialization.Serializable

@Serializable
enum class StatType {
    STRENGTH,
    DEXTERITY,
    CONSTITUTION,
    INTELLIGENCE,
    WISDOM,
    CHARISMA
}
