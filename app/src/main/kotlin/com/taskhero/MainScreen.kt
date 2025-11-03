package com.taskhero

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.taskhero.navigation.Screen
import com.taskhero.navigation.TaskHeroNavGraph

/**
 * Data class representing a bottom navigation item
 */
data class NavigationItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

/**
 * Main screen composable that contains the navigation structure
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Get window size class to determine navigation style
    val activity = LocalContext.current as? android.app.Activity
    val windowSizeClass = if (activity != null) {
        calculateWindowSizeClass(activity)
    } else {
        null
    }

    // Determine if we should use NavigationRail (for larger screens)
    val useNavigationRail = windowSizeClass?.widthSizeClass == WindowWidthSizeClass.Expanded

    // Define navigation items
    val navigationItems = listOf(
        NavigationItem(
            screen = Screen.TaskList,
            icon = Icons.Default.List,
            label = "Tasks"
        ),
        NavigationItem(
            screen = Screen.Reports,
            icon = Icons.Default.Star,
            label = "Reports"
        ),
        NavigationItem(
            screen = Screen.Hero,
            icon = Icons.Default.Person,
            label = "Hero"
        ),
        NavigationItem(
            screen = Screen.Settings,
            icon = Icons.Default.Settings,
            label = "Settings"
        )
    )

    // Determine if current screen should show navigation
    val showNavigation = currentDestination?.route in navigationItems.map { it.screen.route }

    if (useNavigationRail && showNavigation) {
        // Use NavigationRail for larger screens
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(
                modifier = Modifier,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                navigationItems.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any {
                        it.route == item.screen.route
                    } == true

                    NavigationRailItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                TaskHeroNavGraph(
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    } else {
        // Use BottomNavigation for smaller screens
        Scaffold(
            bottomBar = {
                if (showNavigation) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        navigationItems.forEach { item ->
                            val isSelected = currentDestination?.hierarchy?.any {
                                it.route == item.screen.route
                            } == true

                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) },
                                selected = isSelected,
                                onClick = {
                                    navController.navigate(item.screen.route) {
                                        // Pop up to the start destination of the graph to
                                        // avoid building up a large stack of destinations
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        // Avoid multiple copies of the same destination
                                        launchSingleTop = true
                                        // Restore state when reselecting a previously selected item
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            TaskHeroNavGraph(
                navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}
