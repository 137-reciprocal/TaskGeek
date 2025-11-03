package com.taskhero.domain.filter.usecase

import com.taskhero.domain.filter.model.FilterPreset
import com.taskhero.domain.filter.repository.FilterPresetRepository
import com.taskhero.domain.task.model.TaskFilter
import java.util.UUID
import javax.inject.Inject

/**
 * Use case for saving a filter as a reusable preset.
 */
class SaveFilterPresetUseCase @Inject constructor(
    private val filterPresetRepository: FilterPresetRepository
) {

    /**
     * Save a filter as a named preset.
     *
     * @param name The name for the preset
     * @param filter The filter to save
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(name: String, filter: TaskFilter): Result<FilterPreset> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Preset name cannot be empty"))
        }

        val preset = FilterPreset(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            filter = filter,
            createdAt = System.currentTimeMillis()
        )

        return filterPresetRepository.savePreset(preset).fold(
            onSuccess = { Result.success(preset) },
            onFailure = { Result.failure(it) }
        )
    }
}
