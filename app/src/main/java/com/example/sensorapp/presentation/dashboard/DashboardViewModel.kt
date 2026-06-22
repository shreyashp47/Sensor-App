package com.example.sensorapp.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sensorapp.domain.model.SensorAvailability
import com.example.sensorapp.domain.model.SensorReading
import com.example.sensorapp.domain.model.SensorState
import com.example.sensorapp.domain.model.SensorType
import com.example.sensorapp.domain.usecase.GetSensorAvailabilityUseCase
import com.example.sensorapp.domain.usecase.ObserveSensorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getSensorAvailabilityUseCase: GetSensorAvailabilityUseCase,
    private val observeSensorUseCase: ObserveSensorUseCase
) : ViewModel() {

    private val _sensorStates = MutableStateFlow<List<SensorState>>(emptyList())
    val sensorStates: StateFlow<List<SensorState>> = _sensorStates.asStateFlow()

    private val _isLoggingActive = MutableStateFlow(false)
    val isLoggingActive: StateFlow<Boolean> = _isLoggingActive.asStateFlow()

    private val activeJobs = mutableMapOf<SensorType, Job>()

    init {
        viewModelScope.launch {
            val sensorTypes = SensorType.entries
            val availabilityMap = getSensorAvailabilityUseCase().first()
            val states = sensorTypes.map { type ->
                SensorState(
                    type = type,
                    availability = availabilityMap[type] ?: SensorAvailability.Unavailable
                )
            }
            _sensorStates.value = states
            states.filter { it.availability is SensorAvailability.Available }
                .forEach { observeSensor(it.type) }
        }
    }

    private fun observeSensor(sensorType: SensorType) {
        activeJobs[sensorType] = viewModelScope.launch {
            observeSensorUseCase(sensorType).collect { reading: SensorReading ->
                _sensorStates.update { states ->
                    states.map { state ->
                        if (state.type == sensorType) {
                            state.copy(latestReading = reading)
                        } else state
                    }
                }
            }
        }
    }

    fun toggleLogging() {
        _isLoggingActive.update { !it }
    }

    override fun onCleared() {
        activeJobs.values.forEach { it.cancel() }
        activeJobs.clear()
        super.onCleared()
    }
}
