package com.taskhero.domain.hero.model

import kotlinx.serialization.Serializable

@Serializable
data class XpHistoryItem(
    val id: Long = 0,
    val taskUuid: String,
    val xpEarned: Long,
    val timestamp: Long,
    val reason: String
)
