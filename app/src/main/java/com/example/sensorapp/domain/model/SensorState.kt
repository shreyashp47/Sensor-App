package com.example.sensorapp.domain.model

data class SensorState(
    val type: SensorType,
    val availability: SensorAvailability,
    val latestReading: SensorReading? = null
)
