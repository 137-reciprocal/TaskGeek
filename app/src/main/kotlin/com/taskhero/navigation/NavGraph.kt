package com.taskhero.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.taskhero.feature.tasklist.TaskListScreen
import com.taskhero.feature.taskdetail.TaskDetailScreen
import com.taskhero.feature.hero.HeroScreen
import com.taskhero.feature.reports.ReportsScreen
import com.taskhero.feature.settings.SettingsScreen
import com.taskhero.feature.filter.FilterScreen

/**
 * Sealed class representing all possible screens in the app
 */
sealed class Screen(val route: String) {
    data object TaskList : Screen("task_list")
    data object TaskDetail : Screen("task_detail/{taskUuid}") {
        fun createRoute(taskUuid: String) = "task_detail/$taskUuid"
    }
    data object AddTask : Screen("add_task")
    data object Filter : Screen("filter")
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
            TaskListScreen(
                onNavigateToDetail = { taskUuid ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskUuid))
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
            TaskDetailScreen(
                taskUuid = taskUuid,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // Add Task Screen (reuse TaskDetail with new task)
        composable(Screen.AddTask.route) {
            TaskDetailScreen(
                taskUuid = "", // Empty UUID means new task
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // Filter Screen
        composable(Screen.Filter.route) {
            FilterScreen(
                onNavigateBack = { navController.navigateUp() },
                onFilterApplied = { filter ->
                    // Navigate back to task list with filter applied
                    // The filter will be handled by the TaskListScreen
                    navController.navigateUp()
                }
            )
        }

        // Reports Screen
        composable(Screen.Reports.route) {
            ReportsScreen()
        }

        // Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // Hero Screen
        composable(Screen.Hero.route) {
            HeroScreen()
        }
    }
}
