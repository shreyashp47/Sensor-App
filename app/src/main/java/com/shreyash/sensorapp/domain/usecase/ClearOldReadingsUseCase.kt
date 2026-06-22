package com.shreyash.sensorapp.domain.usecase

import com.shreyash.sensorapp.domain.repository.SensorRepository
import javax.inject.Inject

class ClearOldReadingsUseCase @Inject constructor(
    private val repository: SensorRepository
) {
    suspend operator fun invoke(olderThanMs: Long) =
        repository.clearOldReadings(olderThanMs)
}
