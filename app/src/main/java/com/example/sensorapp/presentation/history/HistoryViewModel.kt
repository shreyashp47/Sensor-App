package com.example.sensorapp.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sensorapp.domain.model.LogSession
import com.example.sensorapp.domain.model.SensorType
import com.example.sensorapp.domain.usecase.DeleteAllSessionsUseCase
import com.example.sensorapp.domain.usecase.GetSessionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getSessionsUseCase: GetSessionsUseCase,
    private val deleteAllSessionsUseCase: DeleteAllSessionsUseCase
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow<SensorType?>(null)
    val selectedFilter: StateFlow<SensorType?> = _selectedFilter.asStateFlow()

    private val _sessions = MutableStateFlow<List<LogSession>>(emptyList())
    val sessions: StateFlow<List<LogSession>> = _sessions.asStateFlow()

    private val _isClearing = MutableStateFlow(false)
    val isClearing: StateFlow<Boolean> = _isClearing.asStateFlow()

    private var collectionJob: Job? = null

    init {
        collectSessions()
    }

    private fun collectSessions() {
        collectionJob?.cancel()
        collectionJob = viewModelScope.launch {
            getSessionsUseCase(_selectedFilter.value).collect { list ->
                _sessions.value = list
            }
        }
    }

    fun setFilter(sensorType: SensorType?) {
        _selectedFilter.value = sensorType
        collectSessions()
    }

    fun clearAllSessions() {
        viewModelScope.launch {
            _isClearing.value = true
            deleteAllSessionsUseCase()
            _isClearing.value = false
        }
    }
}
