package com.taskhero.data.preferences.di

import com.taskhero.data.preferences.PreferencesRepository
import com.taskhero.data.preferences.PreferencesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides preferences repository implementation.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesModule {

    /**
     * Binds PreferencesRepositoryImpl to PreferencesRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(
        preferencesRepositoryImpl: PreferencesRepositoryImpl
    ): PreferencesRepository
}
