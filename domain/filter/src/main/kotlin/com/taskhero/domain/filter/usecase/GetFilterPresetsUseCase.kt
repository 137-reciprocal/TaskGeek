package com.taskhero.domain.filter.usecase

import com.taskhero.domain.filter.model.FilterPreset
import com.taskhero.domain.filter.repository.FilterPresetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving all saved filter presets.
 */
class GetFilterPresetsUseCase @Inject constructor(
    private val filterPresetRepository: FilterPresetRepository
) {

    /**
     * Get all saved filter presets.
     *
     * @return Flow emitting list of filter presets
     */
    operator fun invoke(): Flow<List<FilterPreset>> {
        return filterPresetRepository.getAllPresets()
    }
}
