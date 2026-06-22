package com.example.sensorapp.domain.usecase

import com.example.sensorapp.domain.repository.SensorRepository
import javax.inject.Inject

class ClearOldReadingsUseCase @Inject constructor(
    private val repository: SensorRepository
) {
    suspend operator fun invoke(olderThanMs: Long) =
        repository.clearOldReadings(olderThanMs)
}
