package com.example.sensorapp.presentation.navigation

import com.example.sensorapp.domain.model.SensorType

sealed class Route(val route: String) {
    data object Dashboard : Route("dashboard")
    data object History : Route("history")
    data object Settings : Route("settings")

    data class SensorDetail(val sensorType: SensorType) : Route("sensor_detail/${sensorType.name}") {
        companion object {
            const val ROUTE_PATTERN = "sensor_detail/{sensorType}"
        }
    }
}
