package com.shreyash.sensorapp.presentation.compass

import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shreyash.sensorapp.domain.model.SensorAvailability
import com.shreyash.sensorapp.domain.model.SensorReading
import com.shreyash.sensorapp.domain.model.SensorType
import com.shreyash.sensorapp.domain.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompassViewModel @Inject constructor(
    private val repository: SensorRepository
) : ViewModel() {

    private val _heading = MutableStateFlow(0f)
    val heading: StateFlow<Float> = _heading.asStateFlow()

    private val _isAvailable = MutableStateFlow(true)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    private var sensorJob: Job? = null

    init {
        checkAvailability()
    }

    private fun checkAvailability() {
        viewModelScope.launch {
            val accelAvail = repository.getSensorAvailability(SensorType.ACCELEROMETER)
            val magAvail = repository.getSensorAvailability(SensorType.MAGNETOMETER)
            _isAvailable.value = accelAvail is SensorAvailability.Available &&
                    magAvail is SensorAvailability.Available
        }
    }

    fun startObserving() {
        sensorJob?.cancel()
        sensorJob = viewModelScope.launch {
            combine(
                repository.observeSensor(SensorType.ACCELEROMETER),
                repository.observeSensor(SensorType.MAGNETOMETER),
                ::computeHeading
            ).collect { heading ->
                _heading.value = heading
            }
        }
    }

    fun stopObserving() {
        sensorJob?.cancel()
        sensorJob = null
    }

    private fun computeHeading(accel: SensorReading, mag: SensorReading): Float {
        val rotationMatrix = FloatArray(9)
        val success = SensorManager.getRotationMatrix(
            rotationMatrix, null,
            accel.values.toFloatArray(),
            mag.values.toFloatArray()
        )
        if (success) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
            return (azimuthDeg + 360) % 360
        }
        return _heading.value
    }

    override fun onCleared() {
        sensorJob?.cancel()
        super.onCleared()
    }
}
