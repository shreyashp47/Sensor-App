package com.shreyash.sensorapp.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shreyash.sensorapp.domain.model.SensorAvailability
import com.shreyash.sensorapp.domain.model.SensorReading
import com.shreyash.sensorapp.domain.model.SensorState
import com.shreyash.sensorapp.domain.model.SensorType
import com.shreyash.sensorapp.domain.usecase.GetSensorAvailabilityUseCase
import com.shreyash.sensorapp.domain.usecase.ObserveSensorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

    override fun onCleared() {
        activeJobs.values.forEach { it.cancel() }
        activeJobs.clear()
        super.onCleared()
    }
}
