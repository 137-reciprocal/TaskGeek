package com.taskhero.data.hero.di

import com.taskhero.data.hero.repository.HeroRepositoryImpl
import com.taskhero.data.hero.repository.XpHistoryRepositoryImpl
import com.taskhero.domain.hero.repository.HeroRepository
import com.taskhero.domain.hero.repository.XpHistoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides hero-related repository implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataHeroModule {

    /**
     * Binds HeroRepositoryImpl to HeroRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindHeroRepository(
        heroRepositoryImpl: HeroRepositoryImpl
    ): HeroRepository

    /**
     * Binds XpHistoryRepositoryImpl to XpHistoryRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindXpHistoryRepository(
        xpHistoryRepositoryImpl: XpHistoryRepositoryImpl
    ): XpHistoryRepository
}
