package com.shreyash.sensorapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.shreyash.sensorapp.presentation.compass.CompassScreen
import com.shreyash.sensorapp.presentation.dashboard.DashboardScreen
import com.shreyash.sensorapp.presentation.detail.AccelerometerScreen
import com.shreyash.sensorapp.presentation.detail.GravityScreen
import com.shreyash.sensorapp.presentation.detail.GyroscopeScreen
import com.shreyash.sensorapp.presentation.detail.LightScreen
import com.shreyash.sensorapp.presentation.detail.LinearAccelerationScreen
import com.shreyash.sensorapp.presentation.detail.MagnetometerScreen
import com.shreyash.sensorapp.presentation.detail.PressureScreen
import com.shreyash.sensorapp.presentation.detail.ProximityScreen
import com.shreyash.sensorapp.presentation.detail.RotationVectorScreen
import com.shreyash.sensorapp.presentation.detail.StepCounterScreen
import com.shreyash.sensorapp.presentation.history.HistoryScreen
import com.shreyash.sensorapp.presentation.settings.SettingsScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Route.Dashboard.route
    ) {
        composable(Route.Dashboard.route) {
            DashboardScreen(
                onNavigateToDetail = { sensorType ->
                    navController.navigate(sensorType.toRoute())
                },
                onNavigateToCompass = { navController.navigate(Route.Compass.route) },
                onNavigateToHistory = { navController.navigate(Route.History.route) },
                onNavigateToSettings = { navController.navigate(Route.Settings.route) }
            )
        }

        composable(Route.Accelerometer.route) {
            AccelerometerScreen(onBack = { navController.popBackStack() })
        }

        composable(Route.Gyroscope.route) {
            GyroscopeScreen(onBack = { navController.popBackStack() })
        }

        composable(Route.LinearAcceleration.route) {
            LinearAccelerationScreen(onBack = { navController.popBackStack() })
        }

        composable(Route.Magnetometer.route) {
            MagnetometerScreen(onBack = { navController.popBackStack() })
        }

        composable(Route.Gravity.route) {
            GravityScreen(onBack = { navController.popBackStack() })
        }

        composable(Route.RotationVector.route) {
            RotationVectorScreen(onBack = { navController.popBackStack() })
        }

        composable(Route.Light.route) {
            LightScreen(onBack = { navController.popBackStack() })
        }

        composable(Route.Proximity.route) {
            ProximityScreen(onBack = { navController.popBackStack() })
        }

        composable(Route.Pressure.route) {
            PressureScreen(onBack = { navController.popBackStack() })
        }

        composable(Route.StepCounter.route) {
            StepCounterScreen(onBack = { navController.popBackStack() })
        }

        composable(Route.History.route) {
            HistoryScreen(onBack = { navController.popBackStack() })
        }

        composable(Route.Compass.route) {
            CompassScreen(onBack = { navController.popBackStack() })
        }

        composable(Route.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
