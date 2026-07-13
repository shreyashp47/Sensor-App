package com.shreyash.sensorapp.presentation.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shreyash.sensorapp.domain.model.SensorReading
import com.shreyash.sensorapp.domain.model.SensorType
import com.shreyash.sensorapp.presentation.theme.SensorAppTheme

@Composable
fun StepCounterScreen(
    onBack: () -> Unit,
    sensorType: SensorType = SensorType.STEP_COUNTER
) {
    SensorDetailScaffold(sensorType = sensorType, onBack = onBack) { currentReading, chartReadings ->
        StepCounterContent(currentReading = currentReading, chartReadings = chartReadings)
    }
}

@Composable
private fun StepCounterContent(
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
            sensorType = SensorType.STEP_COUNTER
        )

        Spacer(Modifier.height(24.dp))

        LiveLineChart(
            readings = chartReadings,
            sensorType = SensorType.STEP_COUNTER,
            modifier = Modifier.fillMaxWidth().height(260.dp)
        )

        Spacer(Modifier.height(16.dp))

        SensorUsageHint(sensorType = SensorType.STEP_COUNTER)

        Spacer(Modifier.height(16.dp))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1C1E)
@Composable
private fun PreviewStepCounterScreen() {
    SensorAppTheme {
        StepCounterContent(
            currentReading = SensorReading(
                sensorType = SensorType.STEP_COUNTER,
                values = listOf(1247f),
                accuracy = 3,
                timestampMs = System.currentTimeMillis()
            ),
            chartReadings = emptyList()
        )
    }
}
