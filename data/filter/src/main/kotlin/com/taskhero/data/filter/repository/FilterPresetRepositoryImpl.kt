package com.taskhero.data.filter.repository

import com.taskhero.domain.filter.model.FilterPreset
import com.taskhero.domain.filter.repository.FilterPresetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory implementation of FilterPresetRepository.
 * Stores filter presets in memory for the current session.
 * In a production app, this would persist to a database or data store.
 */
@Singleton
class FilterPresetRepositoryImpl @Inject constructor() : FilterPresetRepository {

    private val presets = MutableStateFlow<Map<String, FilterPreset>>(emptyMap())

    override fun getAllPresets(): Flow<List<FilterPreset>> {
        return presets.map { it.values.sortedByDescending { preset -> preset.createdAt } }
    }

    override suspend fun getPresetById(id: String): FilterPreset? {
        return presets.value[id]
    }

    override suspend fun savePreset(preset: FilterPreset): Result<Unit> {
        return try {
            val currentPresets = presets.value.toMutableMap()
            currentPresets[preset.id] = preset
            presets.value = currentPresets
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePreset(id: String): Result<Unit> {
        return try {
            val currentPresets = presets.value.toMutableMap()
            currentPresets.remove(id)
            presets.value = currentPresets
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePreset(preset: FilterPreset): Result<Unit> {
        return try {
            if (!presets.value.containsKey(preset.id)) {
                Result.failure(IllegalArgumentException("Preset with id ${preset.id} not found"))
            } else {
                savePreset(preset)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
