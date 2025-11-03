package com.taskhero.domain.hero.usecase

import com.taskhero.domain.hero.model.Hero
import com.taskhero.domain.hero.repository.HeroRepository
import javax.inject.Inject

/**
 * Use case to update hero information.
 */
class UpdateHeroUseCase @Inject constructor(
    private val repository: HeroRepository
) {
    suspend operator fun invoke(hero: Hero): Result<Unit> {
        return repository.updateHero(hero)
    }
}
