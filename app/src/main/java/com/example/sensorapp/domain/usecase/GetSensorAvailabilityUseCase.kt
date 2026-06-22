package com.example.sensorapp.domain.usecase

import com.example.sensorapp.domain.model.SensorAvailability
import com.example.sensorapp.domain.model.SensorType
import com.example.sensorapp.domain.repository.SensorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSensorAvailabilityUseCase @Inject constructor(
    private val repository: SensorRepository
) {
    operator fun invoke(): Flow<Map<SensorType, SensorAvailability>> =
        repository.getSensorAvailabilityFlow()
}
