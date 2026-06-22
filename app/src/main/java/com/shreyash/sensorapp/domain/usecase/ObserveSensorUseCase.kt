package com.shreyash.sensorapp.domain.usecase

import com.shreyash.sensorapp.domain.model.SensorReading
import com.shreyash.sensorapp.domain.model.SensorType
import com.shreyash.sensorapp.domain.repository.SensorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSensorUseCase @Inject constructor(
    private val repository: SensorRepository
) {
    operator fun invoke(sensorType: SensorType): Flow<SensorReading> =
        repository.observeSensor(sensorType)
}
