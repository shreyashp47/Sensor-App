package com.example.sensorapp.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sensorapp.domain.model.SensorReading
import com.example.sensorapp.domain.model.SensorType
import com.example.sensorapp.domain.usecase.ClearOldReadingsUseCase
import com.example.sensorapp.domain.usecase.GetSensorHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getSensorHistoryUseCase: GetSensorHistoryUseCase,
    private val clearOldReadingsUseCase: ClearOldReadingsUseCase
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow<SensorType?>(null)
    val selectedFilter: StateFlow<SensorType?> = _selectedFilter.asStateFlow()

    private val _readings = MutableStateFlow<List<SensorReading>>(emptyList())
    val readings: StateFlow<List<SensorReading>> = _readings.asStateFlow()

    private val _isClearing = MutableStateFlow(false)
    val isClearing: StateFlow<Boolean> = _isClearing.asStateFlow()

    private var collectionJob: Job? = null

    init {
        collectReadings()
    }

    private fun collectReadings() {
        collectionJob?.cancel()
        collectionJob = viewModelScope.launch {
            getSensorHistoryUseCase(_selectedFilter.value, 200).collect { list ->
                _readings.value = list
            }
        }
    }

    fun setFilter(sensorType: SensorType?) {
        _selectedFilter.value = sensorType
        collectReadings()
    }

    fun clearOldReadings() {
        viewModelScope.launch {
            _isClearing.value = true
            clearOldReadingsUseCase(System.currentTimeMillis() - 24 * 60 * 60 * 1000L)
            _isClearing.value = false
        }
    }
}
