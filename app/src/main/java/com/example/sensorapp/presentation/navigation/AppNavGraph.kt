package com.example.sensorapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.sensorapp.domain.model.SensorType
import com.example.sensorapp.presentation.dashboard.DashboardScreen
import com.example.sensorapp.presentation.detail.SensorDetailScreen
import com.example.sensorapp.presentation.history.HistoryScreen
import com.example.sensorapp.presentation.settings.SettingsScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Route.Dashboard.route
    ) {
        composable(Route.Dashboard.route) {
            DashboardScreen(
                onNavigateToDetail = { sensorType ->
                    navController.navigate(Route.SensorDetail(sensorType).route)
                },
                onNavigateToHistory = { navController.navigate(Route.History.route) },
                onNavigateToSettings = { navController.navigate(Route.Settings.route) }
            )
        }

        composable(
            route = Route.SensorDetail.ROUTE_PATTERN,
            arguments = listOf(
                navArgument("sensorType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sensorTypeName = backStackEntry.arguments?.getString("sensorType")
            val sensorType = SensorType.valueOf(sensorTypeName ?: "ACCELEROMETER")
            SensorDetailScreen(
                sensorType = sensorType,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Route.History.route) {
            HistoryScreen(onBack = { navController.popBackStack() })
        }

        composable(Route.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
