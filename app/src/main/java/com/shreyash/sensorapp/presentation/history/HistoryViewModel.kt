package com.shreyash.sensorapp.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shreyash.sensorapp.domain.model.LogSession
import com.shreyash.sensorapp.domain.model.SensorType
import com.shreyash.sensorapp.domain.usecase.DeleteAllSessionsUseCase
import com.shreyash.sensorapp.domain.usecase.GetSessionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOption(val displayName: String) {
    DATE_NEWEST("Newest first"),
    DATE_OLDEST("Oldest first"),
    DURATION_LONGEST("Longest first"),
    DURATION_SHORTEST("Shortest first")
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getSessionsUseCase: GetSessionsUseCase,
    private val deleteAllSessionsUseCase: DeleteAllSessionsUseCase
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow<SensorType?>(null)
    val selectedFilter: StateFlow<SensorType?> = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.DATE_NEWEST)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val rawSessions = MutableStateFlow<List<LogSession>>(emptyList())

    val sessions: StateFlow<List<LogSession>> = combine(
        rawSessions, _selectedFilter, _searchQuery, _sortOption
    ) { all, filter, query, sort ->
        var list = all

        if (filter != null) {
            list = list.filter { it.sensorType == filter }
        }

        if (query.isNotBlank()) {
            val q = query.lowercase()
            list = list.filter {
                it.sensorType.displayName.lowercase().contains(q)
            }
        }

        when (sort) {
            SortOption.DATE_NEWEST -> list.sortedByDescending { it.startTimeMs }
            SortOption.DATE_OLDEST -> list.sortedBy { it.startTimeMs }
            SortOption.DURATION_LONGEST -> list.sortedByDescending {
                it.endTimeMs?.let { e -> e - it.startTimeMs } ?: 0L
            }
            SortOption.DURATION_SHORTEST -> list.sortedBy {
                it.endTimeMs?.let { e -> e - it.startTimeMs } ?: Long.MAX_VALUE
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isClearing = MutableStateFlow(false)
    val isClearing: StateFlow<Boolean> = _isClearing.asStateFlow()

    private val _showClearConfirmation = MutableStateFlow(false)
    val showClearConfirmation: StateFlow<Boolean> = _showClearConfirmation.asStateFlow()

    init {
        viewModelScope.launch {
            getSessionsUseCase(null).collect { list ->
                rawSessions.value = list
            }
        }
    }

    fun setFilter(sensorType: SensorType?) {
        _selectedFilter.value = sensorType
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun requestClearAll() {
        _showClearConfirmation.value = true
    }

    fun dismissClearConfirmation() {
        _showClearConfirmation.value = false
    }

    fun confirmClearAll() {
        _showClearConfirmation.value = false
        viewModelScope.launch {
            _isClearing.value = true
            deleteAllSessionsUseCase()
            _isClearing.value = false
        }
    }
}
