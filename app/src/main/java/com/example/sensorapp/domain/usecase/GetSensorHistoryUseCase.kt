package com.example.sensorapp.domain.usecase

import com.example.sensorapp.domain.model.SensorReading
import com.example.sensorapp.domain.model.SensorType
import com.example.sensorapp.domain.repository.SensorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSensorHistoryUseCase @Inject constructor(
    private val repository: SensorRepository
) {
    operator fun invoke(type: SensorType?, limit: Int = 100): Flow<List<SensorReading>> =
        repository.getHistory(type, limit)
}
