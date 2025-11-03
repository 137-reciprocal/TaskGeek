package com.taskhero.data.task.di

import com.taskhero.data.task.repository.TagRepositoryImpl
import com.taskhero.data.task.repository.TaskRepositoryImpl
import com.taskhero.domain.task.repository.TagRepository
import com.taskhero.domain.task.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides task-related repository implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataTaskModule {

    /**
     * Binds TaskRepositoryImpl to TaskRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        taskRepositoryImpl: TaskRepositoryImpl
    ): TaskRepository

    /**
     * Binds TagRepositoryImpl to TagRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindTagRepository(
        tagRepositoryImpl: TagRepositoryImpl
    ): TagRepository
}
