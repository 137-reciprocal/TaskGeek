package com.taskhero

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TaskHeroApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Application initialization
    }
}
