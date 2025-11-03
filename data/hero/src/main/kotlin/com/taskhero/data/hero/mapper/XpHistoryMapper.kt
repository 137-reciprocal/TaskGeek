package com.taskhero.data.hero.mapper

import com.taskhero.core.database.entity.XpHistoryEntity
import com.taskhero.domain.hero.model.XpHistoryItem

/**
 * Mapper for converting between XpHistoryEntity and XpHistoryItem
 */
fun XpHistoryEntity.toDomain(): XpHistoryItem {
    return XpHistoryItem(
        id = id,
        taskUuid = taskUuid,
        xpEarned = xpEarned,
        timestamp = timestamp,
        reason = reason
    )
}

fun XpHistoryItem.toEntity(): XpHistoryEntity {
    return XpHistoryEntity(
        id = id,
        taskUuid = taskUuid,
        xpEarned = xpEarned,
        timestamp = timestamp,
        reason = reason
    )
}
