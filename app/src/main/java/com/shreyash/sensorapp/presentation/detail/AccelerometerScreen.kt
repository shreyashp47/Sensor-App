package com.shreyash.sensorapp.presentation.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shreyash.sensorapp.domain.model.SensorReading
import com.shreyash.sensorapp.domain.model.SensorType

@Composable
fun AccelerometerScreen(
    onBack: () -> Unit,
    sensorType: SensorType = SensorType.ACCELEROMETER
) {
    SensorDetailScaffold(sensorType = sensorType, onBack = onBack) { currentReading, chartReadings ->
        AccelerometerContent(currentReading = currentReading, chartReadings = chartReadings)
    }
}

@Composable
private fun AccelerometerContent(
    currentReading: SensorReading?,
    chartReadings: List<SensorReading>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LiveIndicator()

        Spacer(Modifier.height(16.dp))

        LiveValueDisplay(
            reading = currentReading,
            sensorType = SensorType.ACCELEROMETER
        )

        Spacer(Modifier.height(24.dp))

        LiveLineChart(
            readings = chartReadings,
            sensorType = SensorType.ACCELEROMETER,
            modifier = Modifier.fillMaxWidth().height(260.dp)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "X: ${formatDetailValue(currentReading?.values?.getOrNull(0))} ${SensorType.ACCELEROMETER.unitX}" +
                    "  Y: ${formatDetailValue(currentReading?.values?.getOrNull(1))} ${SensorType.ACCELEROMETER.unitY}" +
                    "  Z: ${formatDetailValue(currentReading?.values?.getOrNull(2))} ${SensorType.ACCELEROMETER.unitZ}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        SensorUsageHint(sensorType = SensorType.ACCELEROMETER)

        Spacer(Modifier.height(16.dp))
    }
}
