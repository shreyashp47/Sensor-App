package com.example.sensorapp.domain.usecase

import com.example.sensorapp.domain.model.SensorReading
import com.example.sensorapp.domain.repository.SensorRepository
import javax.inject.Inject

class LogSensorReadingUseCase @Inject constructor(
    private val repository: SensorRepository
) {
    suspend operator fun invoke(reading: SensorReading) =
        repository.logReading(reading)
}
