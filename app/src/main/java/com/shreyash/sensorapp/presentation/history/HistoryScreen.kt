package com.shreyash.sensorapp.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shreyash.sensorapp.domain.model.LogSession
import com.shreyash.sensorapp.domain.model.SensorType
import com.shreyash.sensorapp.presentation.detail.sensorDisplayName
import com.shreyash.sensorapp.presentation.sensorIcon
import com.shreyash.sensorapp.presentation.theme.SensorAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val isClearing by viewModel.isClearing.collectAsStateWithLifecycle()
    val showClearConfirmation by viewModel.showClearConfirmation.collectAsStateWithLifecycle()

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissClearConfirmation() },
            title = { Text("Clear all sessions?") },
            text = { Text("This will permanently delete all logged sessions. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmClearAll() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissClearConfirmation() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.requestClearAll() },
                        enabled = !isClearing && sessions.isNotEmpty()
                    ) {
                        Icon(
                            Icons.Default.DeleteSweep,
                            contentDescription = "Clear all",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Clear all")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        HistoryScreenContent(
            sessions = sessions,
            searchQuery = searchQuery,
            sortOption = sortOption,
            onSearchQueryChanged = { viewModel.setSearchQuery(it) },
            onSortOptionChanged = { viewModel.setSortOption(it) },
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
private fun HistoryScreenContent(
    sessions: List<LogSession>,
    searchQuery: String,
    sortOption: SortOption,
    onSearchQueryChanged: (String) -> Unit,
    onSortOptionChanged: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            placeholder = { Text("Search by sensor name...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            var sortExpanded by remember { mutableStateOf(false) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Sort:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(4.dp))
                TextButton(onClick = { sortExpanded = true }) {
                    Text(sortOption.displayName)
                }
                DropdownMenu(
                    expanded = sortExpanded,
                    onDismissRequest = { sortExpanded = false }
                ) {
                    SortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.displayName) },
                            onClick = {
                                onSortOptionChanged(option)
                                sortExpanded = false
                            }
                        )
                    }
                }
            }
        }

        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No logging sessions yet.\nStart logging from a sensor to see it here.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions, key = { it.id }) { session ->
                    SessionItem(session = session)
                }
            }
        }
    }
}

@Composable
private fun SessionItem(session: LogSession) {
    val duration = session.endTimeMs?.let { end ->
        val diff = end - session.startTimeMs
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        when {
            hours > 0 -> "${hours}h ${minutes % 60}m"
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    } ?: "In progress..."

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = sensorIcon(session.sensorType),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sensorDisplayName(session.sensorType),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Duration: $duration",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = formatTimestamp(session.startTimeMs),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTimestamp(ms: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM dd HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(ms))
}

private val mockSessions = listOf(
    LogSession(
        id = 1, sensorType = SensorType.ACCELEROMETER,
        startTimeMs = System.currentTimeMillis() - 154000,
        endTimeMs = System.currentTimeMillis(), readingCount = 120, summary = "2m 34s log"
    ),
    LogSession(
        id = 2, sensorType = SensorType.GYROSCOPE,
        startTimeMs = System.currentTimeMillis() - 3600000,
        endTimeMs = System.currentTimeMillis() - 3000000, readingCount = 1800, summary = "10m log"
    ),
    LogSession(
        id = 3, sensorType = SensorType.LIGHT,
        startTimeMs = System.currentTimeMillis() - 86400000,
        endTimeMs = System.currentTimeMillis() - 85000000, readingCount = 500, summary = "23m log"
    ),
    LogSession(
        id = 4, sensorType = SensorType.MAGNETOMETER,
        startTimeMs = System.currentTimeMillis() - 180000,
        endTimeMs = null, readingCount = 60, summary = "In progress"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF1A1C1E)
@Composable
private fun PreviewHistoryScreen() {
    SensorAppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("History") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        TextButton(enabled = true, onClick = {}) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Clear all")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { padding ->
            HistoryScreenContent(
                sessions = mockSessions,
                searchQuery = "",
                sortOption = SortOption.DATE_NEWEST,
                onSearchQueryChanged = {},
                onSortOptionChanged = {},
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF1A1C1E)
@Composable
private fun PreviewHistoryScreenEmpty() {
    SensorAppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("History") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        TextButton(enabled = false, onClick = {}) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Clear all")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { padding ->
            HistoryScreenContent(
                sessions = emptyList(),
                searchQuery = "",
                sortOption = SortOption.DATE_NEWEST,
                onSearchQueryChanged = {},
                onSortOptionChanged = {},
                modifier = Modifier.padding(padding)
            )
        }
    }
}
