package com.taskhero.core.notifications

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides notification-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    /**
     * Provides a singleton instance of NotificationHelper.
     */
    @Provides
    @Singleton
    fun provideNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper {
        return NotificationHelper(context)
    }
}
