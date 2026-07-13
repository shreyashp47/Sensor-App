package com.shreyash.sensorapp.presentation.detail

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shreyash.sensorapp.domain.model.SensorReading
import com.shreyash.sensorapp.domain.model.SensorType
import com.shreyash.sensorapp.presentation.detail.sensorDisplayName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.OutputStreamWriter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorDetailScaffold(
    sensorType: SensorType,
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
    content: @Composable (currentReading: SensorReading?, chartReadings: List<SensorReading>) -> Unit
) {
    val currentReading by viewModel.currentReading.collectAsStateWithLifecycle()
    val isLogging by viewModel.isLogging.collectAsStateWithLifecycle()
    val chartReadings by viewModel.chartReadings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(sensorType) {
        viewModel.initialize(sensorType)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.startObserving()
                Lifecycle.Event.ON_STOP -> viewModel.stopObserving()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text(sensorDisplayName(sensorType)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.toggleLogging() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLogging) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (isLogging) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (isLogging) "Stop Logging" else "Start Logging")
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val uri = exportToCsv(context, chartReadings, sensorType)
                            if (uri != null) {
                                snackbarHostState.showSnackbar("CSV exported to Downloads")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    enabled = chartReadings.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Export CSV")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            content(currentReading, chartReadings)
        }
    }
}

private suspend fun exportToCsv(
    context: Context,
    readings: List<SensorReading>,
    sensorType: SensorType
): android.net.Uri? = withContext(Dispatchers.IO) {
    try {
        val fileName = "${sensorType.name}_${System.currentTimeMillis()}.csv"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues
            ) ?: return@withContext null

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                writeCsv(outputStream, readings)
            }

            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            context.contentResolver.update(uri, contentValues, null, null)
            uri
        } else {
            val dir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            dir.mkdirs()
            val file = java.io.File(dir, fileName)
            file.outputStream().use { outputStream ->
                writeCsv(outputStream, readings)
            }
            android.net.Uri.fromFile(file)
        }
    } catch (e: Exception) {
        null
    }
}

private fun writeCsv(outputStream: java.io.OutputStream, readings: List<SensorReading>) {
    val writer = BufferedWriter(OutputStreamWriter(outputStream))
    writer.write("Timestamp_ms,Value1,Value2,Value3\n")
    readings.forEach { reading ->
        writer.write("${reading.timestampMs},${reading.values.joinToString(",")}\n")
    }
    writer.flush()
}
