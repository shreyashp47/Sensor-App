package com.example.sensorapp.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sensorapp.domain.model.SensorReading
import com.example.sensorapp.domain.model.SensorType
import com.example.sensorapp.domain.usecase.GetSensorHistoryUseCase
import com.example.sensorapp.domain.usecase.LogSensorReadingUseCase
import com.example.sensorapp.domain.usecase.ObserveSensorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val observeSensorUseCase: ObserveSensorUseCase,
    private val logSensorReadingUseCase: LogSensorReadingUseCase,
    private val getSensorHistoryUseCase: GetSensorHistoryUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val sensorType: SensorType = SensorType.valueOf(
        savedStateHandle.get<String>("sensorType") ?: "ACCELEROMETER"
    )

    private val _currentReading = MutableStateFlow<SensorReading?>(null)
    val currentReading: StateFlow<SensorReading?> = _currentReading.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    val history: StateFlow<List<SensorReading>> = getSensorHistoryUseCase(sensorType, 60)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var sensorJob: Job? = null

    fun start() {
        if (sensorJob != null) return
        _isRunning.value = true
        sensorJob = viewModelScope.launch {
            observeSensorUseCase(sensorType).collect { reading ->
                _currentReading.value = reading
                logSensorReadingUseCase(reading)
            }
        }
    }

    fun stop() {
        sensorJob?.cancel()
        sensorJob = null
        _isRunning.value = false
    }

    override fun onCleared() {
        sensorJob?.cancel()
        super.onCleared()
    }
}
