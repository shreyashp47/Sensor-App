package com.example.sensorapp.data.repository

import com.example.sensorapp.data.local.AppDatabase
import com.example.sensorapp.data.local.SensorReadingEntity
import com.example.sensorapp.data.sensor.SensorDataSource
import com.example.sensorapp.domain.model.SensorAvailability
import com.example.sensorapp.domain.model.SensorReading
import com.example.sensorapp.domain.model.SensorType
import com.example.sensorapp.domain.repository.SensorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorRepositoryImpl @Inject constructor(
    private val sensorDataSource: SensorDataSource,
    private val database: AppDatabase
) : SensorRepository {

    private val dao = database.sensorDao()

    @Volatile
    private var currentDelay: Int = android.hardware.SensorManager.SENSOR_DELAY_UI

    private val loggingEnabled = mutableMapOf<SensorType, Boolean>()

    override fun observeSensor(sensorType: SensorType): Flow<SensorReading> {
        return sensorDataSource.observeSensor(sensorType, currentDelay)
    }

    override fun getSensorAvailabilityFlow(): Flow<Map<SensorType, SensorAvailability>> {
        val sensors = sensorDataSource.getAllSensorTypes()
        val map = mutableMapOf<SensorType, SensorAvailability>()
        for (sensor in sensors) {
            map[sensor] = if (sensorDataSource.isSensorAvailable(sensor)) {
                SensorAvailability.Available
            } else {
                SensorAvailability.Unavailable
            }
        }
        return flowOf(map)
    }

    override fun getAllSensors(): List<SensorType> = sensorDataSource.getAllSensorTypes()

    override suspend fun getSensorAvailability(sensorType: SensorType): SensorAvailability {
        return if (sensorDataSource.isSensorAvailable(sensorType)) {
            SensorAvailability.Available
        } else {
            SensorAvailability.Unavailable
        }
    }

    override suspend fun logReading(reading: SensorReading) {
        val entity = SensorReadingEntity(
            sensorType = reading.sensorType.name,
            values = reading.values,
            accuracy = reading.accuracy,
            timestampMs = reading.timestampMs
        )
        dao.insert(entity)
    }

    override fun getHistory(sensorType: SensorType?, limit: Int): Flow<List<SensorReading>> {
        val flow = if (sensorType != null) {
            dao.getByTypeLimited(sensorType.name, limit)
        } else {
            dao.getAll()
        }
        return flow.map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun clearOldReadings(olderThanMs: Long) {
        dao.deleteOlderThan(olderThanMs)
    }

    override suspend fun getTotalRowCount(): Int = dao.count()

    override suspend fun getDelay(): Int = currentDelay

    override suspend fun setDelay(delay: Int) {
        currentDelay = delay
    }

    override suspend fun isLoggingEnabled(sensorType: SensorType): Boolean {
        return loggingEnabled[sensorType] ?: true
    }

    override suspend fun setLoggingEnabled(sensorType: SensorType, enabled: Boolean) {
        loggingEnabled[sensorType] = enabled
    }
}

private fun SensorReadingEntity.toDomainModel() = SensorReading(
    sensorType = SensorType.valueOf(this.sensorType),
    values = this.values,
    accuracy = this.accuracy,
    timestampMs = this.timestampMs
)
