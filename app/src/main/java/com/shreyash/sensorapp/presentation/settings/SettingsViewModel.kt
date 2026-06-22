package com.shreyash.sensorapp.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shreyash.sensorapp.domain.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SensorRepository
) : ViewModel() {

    private val _selectedDelay = MutableStateFlow(2)
    val selectedDelay: StateFlow<Int> = _selectedDelay.asStateFlow()

    private val _totalRows = MutableStateFlow(0)
    val totalRows: StateFlow<Int> = _totalRows.asStateFlow()

    init {
        viewModelScope.launch {
            _selectedDelay.value = repository.getDelay()
            _totalRows.value = repository.getTotalRowCount()
            refreshDelay()
        }
    }

    private suspend fun refreshDelay() {
        _selectedDelay.value = repository.getDelay()
    }

    fun setDelay(delay: Int) {
        viewModelScope.launch {
            repository.setDelay(delay)
            _selectedDelay.value = delay
        }
    }

    fun refreshStats() {
        viewModelScope.launch {
            _totalRows.value = repository.getTotalRowCount()
        }
    }

    companion object {
        val delayOptions = listOf(
            0 to "FASTEST",
            1 to "GAME",
            2 to "UI",
            3 to "NORMAL"
        )
    }
}
