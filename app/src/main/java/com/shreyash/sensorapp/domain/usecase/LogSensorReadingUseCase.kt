package com.shreyash.sensorapp.domain.usecase

import com.shreyash.sensorapp.domain.model.SensorReading
import com.shreyash.sensorapp.domain.repository.SensorRepository
import javax.inject.Inject

class LogSensorReadingUseCase @Inject constructor(
    private val repository: SensorRepository
) {
    suspend operator fun invoke(reading: SensorReading) =
        repository.logReading(reading)
}
