package com.example.sensorapp.domain.repository

import com.example.sensorapp.domain.model.SensorAvailability
import com.example.sensorapp.domain.model.SensorReading
import com.example.sensorapp.domain.model.SensorType
import kotlinx.coroutines.flow.Flow

interface SensorRepository {
    fun observeSensor(sensorType: SensorType): Flow<SensorReading>

    fun getSensorAvailabilityFlow(): Flow<Map<SensorType, SensorAvailability>>

    fun getAllSensors(): List<SensorType>

    suspend fun getSensorAvailability(sensorType: SensorType): SensorAvailability

    suspend fun logReading(reading: SensorReading)

    fun getHistory(sensorType: SensorType?, limit: Int = 100): Flow<List<SensorReading>>

    suspend fun clearOldReadings(olderThanMs: Long)

    suspend fun getTotalRowCount(): Int

    suspend fun getDelay(): Int

    suspend fun setDelay(delay: Int)

    suspend fun isLoggingEnabled(sensorType: SensorType): Boolean

    suspend fun setLoggingEnabled(sensorType: SensorType, enabled: Boolean)
}
