package com.taskhero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.taskhero.core.ui.theme.TaskHeroTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the TaskHero application
 *
 * This activity serves as the entry point for the app and hosts the main navigation structure.
 * It uses edge-to-edge display for a modern, immersive experience.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskHeroTheme {
                MainScreen()
            }
        }
    }
}
