package com.shreyash.sensorapp.domain.usecase

import com.shreyash.sensorapp.domain.model.LogSession
import com.shreyash.sensorapp.domain.model.SensorType
import com.shreyash.sensorapp.domain.repository.SensorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSessionsUseCase @Inject constructor(
    private val repository: SensorRepository
) {
    operator fun invoke(sensorType: SensorType?): Flow<List<LogSession>> =
        repository.getSessions(sensorType)
}
