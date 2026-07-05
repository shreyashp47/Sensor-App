package com.shreyash.sensorapp.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shreyash.sensorapp.data.sensor.HapticManager
import com.shreyash.sensorapp.domain.model.SensorReading
import com.shreyash.sensorapp.domain.model.SensorType
import com.shreyash.sensorapp.domain.usecase.LogSensorReadingUseCase
import com.shreyash.sensorapp.domain.usecase.ObserveSensorUseCase
import com.shreyash.sensorapp.domain.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.sqrt

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val observeSensorUseCase: ObserveSensorUseCase,
    private val logSensorReadingUseCase: LogSensorReadingUseCase,
    private val repository: SensorRepository,
    private val hapticManager: HapticManager
) : ViewModel() {

    private var _sensorType: SensorType = SensorType.ACCELEROMETER
    val sensorType: SensorType get() = _sensorType

    private val _currentReading = MutableStateFlow<SensorReading?>(null)
    val currentReading: StateFlow<SensorReading?> = _currentReading.asStateFlow()

    private val _isLogging = MutableStateFlow(false)
    val isLogging: StateFlow<Boolean> = _isLogging.asStateFlow()

    private val _chartReadings = MutableStateFlow<List<SensorReading>>(emptyList())
    val chartReadings: StateFlow<List<SensorReading>> = _chartReadings.asStateFlow()

    private var sensorJob: Job? = null
    private var currentSessionId: Long? = null
    private var previousReading: SensorReading? = null
    private var initialized = false

    fun initialize(sensorType: SensorType) {
        if (initialized) return
        initialized = true
        _sensorType = sensorType
        startObserving()
    }

    fun startObserving() {
        sensorJob?.cancel()
        sensorJob = viewModelScope.launch {
            observeSensorUseCase(sensorType).collect { reading ->
                val prev = previousReading
                previousReading = reading

                _currentReading.value = reading
                _chartReadings.update { buffer ->
                    (buffer + reading).takeLast(60)
                }
                if (_isLogging.value) {
                    logSensorReadingUseCase(reading)
                }

                if (repository.isHapticEnabled()) {
                    checkHapticTriggers(reading, prev)
                }
            }
        }
    }

    private fun checkHapticTriggers(reading: SensorReading, prev: SensorReading?) {
        when (sensorType) {
            SensorType.PROXIMITY -> {
                val currVal = reading.values.getOrNull(0) ?: return
                val prevVal = prev?.values?.getOrNull(0) ?: return
                val currObstructed = currVal < 1f
                val prevObstructed = prevVal < 1f
                if (prevObstructed != currObstructed) {
                    hapticManager.doubleTick()
                }
            }
            SensorType.STEP_COUNTER -> {
                val currVal = reading.values.getOrNull(0) ?: 0f
                val prevVal = prev?.values?.getOrNull(0) ?: currVal
                if (currVal > prevVal) {
                    hapticManager.tick()
                }
            }
            SensorType.GYROSCOPE -> {
                val gx = reading.values.getOrNull(0) ?: 0f
                val gy = reading.values.getOrNull(1) ?: 0f
                val gz = reading.values.getOrNull(2) ?: 0f
                val magnitude = sqrt((gx * gx + gy * gy + gz * gz).toDouble()).toFloat()
                if (magnitude > 5f) {
                    hapticManager.tick()
                }
            }
            else -> {}
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
