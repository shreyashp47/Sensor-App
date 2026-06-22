package com.shreyash.sensorapp.domain.model

data class LogSession(
    val id: Long,
    val sensorType: SensorType,
    val startTimeMs: Long,
    val endTimeMs: Long?,
    val readingCount: Int,
    val summary: String
)
