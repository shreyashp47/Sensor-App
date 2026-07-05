package com.shreyash.sensorapp.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shreyash.sensorapp.domain.model.SensorAvailability
import com.shreyash.sensorapp.domain.model.SensorState
import com.shreyash.sensorapp.domain.model.SensorType
import com.shreyash.sensorapp.domain.usecase.GetSensorAvailabilityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getSensorAvailabilityUseCase: GetSensorAvailabilityUseCase
) : ViewModel() {

    private val _sensorStates = MutableStateFlow<List<SensorState>>(emptyList())
    val sensorStates: StateFlow<List<SensorState>> = _sensorStates.asStateFlow()

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
        }
    }
}
