package com.taskhero.data.filter.di

import com.taskhero.data.filter.repository.FilterPresetRepositoryImpl
import com.taskhero.domain.filter.repository.FilterPresetRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing filter data dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FilterDataModule {

    @Binds
    @Singleton
    abstract fun bindFilterPresetRepository(
        impl: FilterPresetRepositoryImpl
    ): FilterPresetRepository
}
