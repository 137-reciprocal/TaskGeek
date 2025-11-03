package com.taskhero.domain.hero.usecase

import com.taskhero.domain.hero.model.Hero
import com.taskhero.domain.hero.repository.HeroRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get the current hero.
 */
class GetHeroUseCase @Inject constructor(
    private val repository: HeroRepository
) {
    operator fun invoke(): Flow<Hero?> {
        return repository.getHero()
    }
}
