package com.shreyash.sensorapp.domain.usecase

import com.shreyash.sensorapp.domain.repository.SensorRepository
import javax.inject.Inject

class DeleteAllSessionsUseCase @Inject constructor(
    private val repository: SensorRepository
) {
    suspend operator fun invoke() = repository.deleteAllSessions()
}
