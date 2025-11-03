package com.taskhero.domain.hero.model

import kotlinx.serialization.Serializable

@Serializable
data class Title(
    val id: String,
    val name: String,
    val description: String,
    val requiredLevel: Int,
    val isUnlocked: Boolean = false
)
