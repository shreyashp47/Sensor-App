package com.shreyash.sensorapp.domain.usecase

import com.shreyash.sensorapp.domain.model.SensorAvailability
import com.shreyash.sensorapp.domain.model.SensorType
import com.shreyash.sensorapp.domain.repository.SensorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSensorAvailabilityUseCase @Inject constructor(
    private val repository: SensorRepository
) {
    operator fun invoke(): Flow<Map<SensorType, SensorAvailability>> =
        repository.getSensorAvailabilityFlow()
}
