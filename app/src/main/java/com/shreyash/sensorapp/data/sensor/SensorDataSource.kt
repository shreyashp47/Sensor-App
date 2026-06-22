package com.shreyash.sensorapp.data.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.shreyash.sensorapp.domain.model.SensorReading
import com.shreyash.sensorapp.domain.model.SensorType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorDataSource @Inject constructor(
    private val sensorManager: SensorManager
) {

    private val sensorTypeMapping: Map<SensorType, Int> = mapOf(
        SensorType.ACCELEROMETER to Sensor.TYPE_ACCELEROMETER,
        SensorType.GYROSCOPE to Sensor.TYPE_GYROSCOPE,
        SensorType.MAGNETOMETER to Sensor.TYPE_MAGNETIC_FIELD,
        SensorType.LIGHT to Sensor.TYPE_LIGHT,
        SensorType.PROXIMITY to Sensor.TYPE_PROXIMITY,
        SensorType.PRESSURE to Sensor.TYPE_PRESSURE,
        SensorType.STEP_COUNTER to Sensor.TYPE_STEP_COUNTER,
        SensorType.GRAVITY to Sensor.TYPE_GRAVITY,
        SensorType.LINEAR_ACCELERATION to Sensor.TYPE_LINEAR_ACCELERATION,
        SensorType.ROTATION_VECTOR to Sensor.TYPE_ROTATION_VECTOR
    )

    fun isSensorAvailable(sensorType: SensorType): Boolean {
        val androidType = sensorTypeMapping[sensorType] ?: return false
        return sensorManager.getDefaultSensor(androidType) != null
    }

    fun observeSensor(
        sensorType: SensorType,
        delay: Int = SensorManager.SENSOR_DELAY_UI
    ): Flow<SensorReading> = callbackFlow {
        val androidType = sensorTypeMapping[sensorType]
            ?: run {
                close(Exception("Unknown sensor type: $sensorType"))
                return@callbackFlow
            }
        val sensor = sensorManager.getDefaultSensor(androidType) ?: run {
            close(Exception("Sensor not available: $sensorType"))
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val reading = SensorReading(
                    sensorType = sensorType,
                    values = event.values.toList(),
                    accuracy = event.accuracy,
                    timestampMs = event.timestamp / 1_000_000L
                )
                trySend(reading)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorManager.registerListener(listener, sensor, delay)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    fun getAllSensorTypes(): List<SensorType> = sensorTypeMapping.keys.toList()
}
