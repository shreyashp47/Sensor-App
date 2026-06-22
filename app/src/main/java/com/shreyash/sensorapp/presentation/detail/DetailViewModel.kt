package com.shreyash.sensorapp.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shreyash.sensorapp.domain.model.SensorReading
import com.shreyash.sensorapp.domain.model.SensorType
import com.shreyash.sensorapp.domain.usecase.LogSensorReadingUseCase
import com.shreyash.sensorapp.domain.usecase.ObserveSensorUseCase
import com.shreyash.sensorapp.domain.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val observeSensorUseCase: ObserveSensorUseCase,
    private val logSensorReadingUseCase: LogSensorReadingUseCase,
    private val repository: SensorRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val sensorType: SensorType = SensorType.valueOf(
        savedStateHandle.get<String>("sensorType") ?: "ACCELEROMETER"
    )

    private val _currentReading = MutableStateFlow<SensorReading?>(null)
    val currentReading: StateFlow<SensorReading?> = _currentReading.asStateFlow()

    private val _isLogging = MutableStateFlow(false)
    val isLogging: StateFlow<Boolean> = _isLogging.asStateFlow()

    private val _chartReadings = MutableStateFlow<List<SensorReading>>(emptyList())
    val chartReadings: StateFlow<List<SensorReading>> = _chartReadings.asStateFlow()

    private var sensorJob: Job? = null
    private var currentSessionId: Long? = null

    init {
        startObserving()
    }

    fun startObserving() {
        sensorJob?.cancel()
        sensorJob = viewModelScope.launch {
            observeSensorUseCase(sensorType).collect { reading ->
                _currentReading.value = reading
                _chartReadings.update { buffer ->
                    (buffer + reading).takeLast(60)
                }
                if (_isLogging.value) {
                    logSensorReadingUseCase(reading)
                }
            }
        }
    }

    fun stopObserving() {
        sensorJob?.cancel()
        sensorJob = null
    }

    fun toggleLogging() {
        viewModelScope.launch {
            if (_isLogging.value) {
                val sessionId = currentSessionId
                if (sessionId != null) {
                    repository.endSession(sessionId, System.currentTimeMillis())
                }
                currentSessionId = null
                _isLogging.value = false
            } else {
                val sessionId = repository.startSession(sensorType)
                currentSessionId = sessionId
                _isLogging.value = true
            }
        }
    }

    override fun onCleared() {
        sensorJob?.cancel()
        viewModelScope.launch {
            val sessionId = currentSessionId
            if (sessionId != null) {
                repository.endSession(sessionId, System.currentTimeMillis())
            }
        }
        super.onCleared()
    }
}
