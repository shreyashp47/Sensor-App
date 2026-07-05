package com.shreyash.sensorapp.domain.repository

import com.shreyash.sensorapp.domain.model.LogSession
import com.shreyash.sensorapp.domain.model.SensorAvailability
import com.shreyash.sensorapp.domain.model.SensorReading
import com.shreyash.sensorapp.domain.model.SensorType
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

    suspend fun startSession(sensorType: SensorType): Long

    suspend fun endSession(sessionId: Long, endTimeMs: Long)

    fun getSessions(sensorType: SensorType?): Flow<List<LogSession>>

    suspend fun deleteAllSessions()

    suspend fun getDelay(): Int

    suspend fun setDelay(delay: Int)

    fun isHapticEnabled(): Boolean

    fun setHapticEnabled(enabled: Boolean)
}
