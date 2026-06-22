package com.example.sensorapp.presentation.dashboard

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sensorapp.domain.model.SensorAvailability
import com.example.sensorapp.domain.model.SensorState
import com.example.sensorapp.domain.model.SensorType
import com.example.sensorapp.presentation.permission.PermissionDialog
import com.example.sensorapp.presentation.permission.rememberPermissionHandler
import com.example.sensorapp.presentation.theme.SensorGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToDetail: (SensorType) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val sensorStates by viewModel.sensorStates.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val permissionHandler = rememberPermissionHandler(snackbarHostState)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("SensorApp") },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(sensorStates, key = { it.type }) { state ->
                SensorListItem(
                    state = state,
                    onClick = {
                        if (state.availability is SensorAvailability.Available) {
                            permissionHandler.checkAndRequest(state.type) {
                                onNavigateToDetail(state.type)
                            }
                        }
                    }
                )
            }
        }
    }

    if (permissionHandler.showDialog) {
        PermissionDialog(
            sensorType = permissionHandler.pendingSensorType,
            onDismiss = permissionHandler.onDismissDialog,
            onConfirm = permissionHandler.onConfirmDialog
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorListItem(
    state: SensorState,
    onClick: () -> Unit
) {
    val isAvailable = state.availability is SensorAvailability.Available
    var showBottomSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (isAvailable) onClick()
                else showBottomSheet = true
            }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Sensors,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isAvailable) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.type.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isAvailable) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                if (!isAvailable) {
                    Text(
                        text = "Not available on this device",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                } else if (state.latestReading != null) {
                    val value = state.latestReading.values.firstOrNull() ?: 0f
                    val unit = state.type.unitSingle ?: state.type.unitX
                    Text(
                        text = "${formatValue(value)} ${unit}".trimEnd(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (isAvailable) {
                LiveDot()
                Spacer(Modifier.width(8.dp))
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = state.type.displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Your device does not have a ${state.type.displayName}. " +
                            state.type.description,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun LiveDot() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        )
    )
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(SensorGreen.copy(alpha = alpha))
    )
}

private fun formatValue(value: Float): String {
    return if (value >= 1000) String.format("%.1f", value)
    else if (value >= 100) String.format("%.1f", value)
    else if (value >= 10) String.format("%.2f", value)
    else String.format("%.3f", value)
}
