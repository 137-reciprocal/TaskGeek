package com.taskhero.navigation

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for navigation-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object NavigationModule {

    /**
     * Provides the start destination for the app
     */
    @Provides
    @Singleton
    fun provideStartDestination(): String = Screen.TaskList.route
}
