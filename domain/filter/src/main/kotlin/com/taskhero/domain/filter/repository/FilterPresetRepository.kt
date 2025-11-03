package com.taskhero.domain.filter.repository

import com.taskhero.domain.filter.model.FilterPreset
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing filter presets.
 */
interface FilterPresetRepository {
    /**
     * Get all saved filter presets.
     */
    fun getAllPresets(): Flow<List<FilterPreset>>

    /**
     * Get a preset by ID.
     */
    suspend fun getPresetById(id: String): FilterPreset?

    /**
     * Save a new filter preset.
     */
    suspend fun savePreset(preset: FilterPreset): Result<Unit>

    /**
     * Delete a preset by ID.
     */
    suspend fun deletePreset(id: String): Result<Unit>

    /**
     * Update an existing preset.
     */
    suspend fun updatePreset(preset: FilterPreset): Result<Unit>
}
