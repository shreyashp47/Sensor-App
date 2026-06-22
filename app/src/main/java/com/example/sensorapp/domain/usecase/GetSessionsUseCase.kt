package com.example.sensorapp.domain.usecase

import com.example.sensorapp.domain.model.LogSession
import com.example.sensorapp.domain.model.SensorType
import com.example.sensorapp.domain.repository.SensorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSessionsUseCase @Inject constructor(
    private val repository: SensorRepository
) {
    operator fun invoke(sensorType: SensorType?): Flow<List<LogSession>> =
        repository.getSessions(sensorType)
}
