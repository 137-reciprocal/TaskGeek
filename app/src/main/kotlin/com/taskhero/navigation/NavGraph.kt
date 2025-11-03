package com.taskhero.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

/**
 * Sealed class representing all possible screens in the app
 */
sealed class Screen(val route: String) {
    data object TaskList : Screen("task_list")
    data object TaskDetail : Screen("task_detail/{taskUuid}") {
        fun createRoute(taskUuid: String) = "task_detail/$taskUuid"
    }
    data object AddTask : Screen("add_task")
    data object Reports : Screen("reports")
    data object Settings : Screen("settings")
    data object Hero : Screen("hero")
}

/**
 * Main navigation graph for TaskHero app
 *
 * @param navController Navigation controller for handling navigation
 * @param modifier Modifier for the NavHost
 * @param startDestination Initial screen to display
 */
@Composable
fun TaskHeroNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.TaskList.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Task List Screen
        composable(Screen.TaskList.route) {
            TaskListScreenPlaceholder(
                onNavigateToTaskDetail = { taskUuid ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskUuid))
                },
                onNavigateToAddTask = {
                    navController.navigate(Screen.AddTask.route)
                }
            )
        }

        // Task Detail Screen
        composable(
            route = Screen.TaskDetail.route,
            arguments = listOf(
                navArgument("taskUuid") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val taskUuid = backStackEntry.arguments?.getString("taskUuid") ?: ""
            TaskDetailScreenPlaceholder(
                taskUuid = taskUuid,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // Add Task Screen
        composable(Screen.AddTask.route) {
            AddTaskScreenPlaceholder(
                onNavigateBack = { navController.navigateUp() },
                onTaskAdded = { navController.navigateUp() }
            )
        }

        // Reports Screen
        composable(Screen.Reports.route) {
            ReportsScreenPlaceholder()
        }

        // Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreenPlaceholder()
        }

        // Hero Screen
        composable(Screen.Hero.route) {
            HeroScreenPlaceholder()
        }
    }
}

// Placeholder composables - to be replaced with actual implementations
@Composable
private fun TaskListScreenPlaceholder(
    onNavigateToTaskDetail: (String) -> Unit,
    onNavigateToAddTask: () -> Unit
) {
    androidx.compose.material3.Text("Task List Screen - Coming Soon!")
}

@Composable
private fun TaskDetailScreenPlaceholder(
    taskUuid: String,
    onNavigateBack: () -> Unit
) {
    androidx.compose.material3.Text("Task Detail Screen - UUID: $taskUuid")
}

@Composable
private fun AddTaskScreenPlaceholder(
    onNavigateBack: () -> Unit,
    onTaskAdded: () -> Unit
) {
    androidx.compose.material3.Text("Add Task Screen - Coming Soon!")
}

@Composable
private fun ReportsScreenPlaceholder() {
    androidx.compose.material3.Text("Reports Screen - Coming Soon!")
}

@Composable
private fun SettingsScreenPlaceholder() {
    androidx.compose.material3.Text("Settings Screen - Coming Soon!")
}

@Composable
private fun HeroScreenPlaceholder() {
    androidx.compose.material3.Text("Hero Screen - Coming Soon!")
}
