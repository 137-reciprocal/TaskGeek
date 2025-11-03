package com.taskhero.widget

import com.taskhero.domain.hero.repository.HeroRepository
import com.taskhero.domain.task.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for widget dependencies.
 * Provides widget-specific components.
 */
@Module
@InstallIn(SingletonComponent::class)
object WidgetModule {

    @Provides
    @Singleton
    fun provideWidgetRepository(
        taskRepository: TaskRepository,
        heroRepository: HeroRepository
    ): WidgetRepository {
        return WidgetRepository(taskRepository, heroRepository)
    }
}
