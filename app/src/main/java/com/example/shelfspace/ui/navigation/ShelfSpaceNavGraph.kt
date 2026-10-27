package com.example.shelfspace.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shelfspace.ui.screens.AboutScreen
import com.example.shelfspace.ui.screens.HistoryScreen
import com.example.shelfspace.ui.screens.HomeScreen
import com.example.shelfspace.ui.screens.MultipleUnitsScreen
import com.example.shelfspace.ui.screens.SettingsScreen
import com.example.shelfspace.ui.screens.SplashScreen
import com.example.shelfspace.ui.screens.StorageLayoutScreen
import com.example.shelfspace.viewmodel.ShelfSpaceViewModel

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object StorageLayout : Screen("storage_layout")
    data object MultipleUnits : Screen("multiple_units")
    data object History : Screen("history")
    data object Settings : Screen("settings")
    data object About : Screen("about")
}

@Composable
fun ShelfSpaceNavGraph(
    navController: NavHostController = rememberNavController(),
    viewModel: ShelfSpaceViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToLayout = { navController.navigate(Screen.StorageLayout.route) },
                onNavigateToMultipleUnits = { navController.navigate(Screen.MultipleUnits.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) }
            )
        }

        composable(Screen.StorageLayout.route) {
            StorageLayoutScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MultipleUnits.route) {
            MultipleUnitsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onOpenCalculation = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
