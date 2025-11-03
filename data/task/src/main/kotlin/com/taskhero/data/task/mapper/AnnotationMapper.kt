package com.taskhero.data.task.mapper

import com.taskhero.core.database.entity.AnnotationEntity
import com.taskhero.domain.task.model.Annotation

/**
 * Mapper object for converting between AnnotationEntity and Annotation domain model.
 */
object AnnotationMapper {
    /**
     * Convert AnnotationEntity to Annotation domain model.
     *
     * @return Annotation domain model
     */
    fun AnnotationEntity.toDomain(): Annotation {
        return Annotation(
            id = id,
            taskUuid = taskUuid,
            timestamp = timestamp,
            description = description
        )
    }

    /**
     * Convert Annotation domain model to AnnotationEntity.
     *
     * @return AnnotationEntity for database storage
     */
    fun Annotation.toEntity(): AnnotationEntity {
        return AnnotationEntity(
            id = id,
            taskUuid = taskUuid,
            timestamp = timestamp,
            description = description
        )
    }
}
