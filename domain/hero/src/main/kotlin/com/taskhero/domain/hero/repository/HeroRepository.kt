package com.taskhero.domain.hero.repository

import com.taskhero.domain.hero.model.Hero
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for hero-related operations.
 * Manages the hero entity and its state.
 */
interface HeroRepository {
    /**
     * Get the current hero as a Flow stream.
     */
    fun getHero(): Flow<Hero?>

    /**
     * Update the hero's information.
     *
     * @param hero The hero object with updated information
     */
    suspend fun updateHero(hero: Hero): Result<Unit>

    /**
     * Create a new hero.
     *
     * @param hero The hero object to create
     */
    suspend fun createHero(hero: Hero): Result<Unit>
}
