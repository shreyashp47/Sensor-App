package com.example.sensorapp.domain.model

sealed interface SensorAvailability {
    data object Available : SensorAvailability
    data object Unavailable : SensorAvailability
}
