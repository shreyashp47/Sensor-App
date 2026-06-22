package com.shreyash.sensorapp.domain.model

data class SensorReading(
    val sensorType: SensorType,
    val values: List<Float>,
    val accuracy: Int,
    val timestampMs: Long
)
