package com.shreyash.sensorapp.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shreyash.sensorapp.domain.model.SensorReading
import com.shreyash.sensorapp.domain.model.SensorType
import com.shreyash.sensorapp.presentation.theme.SensorAppTheme

private val AxisRed = Color(0xFFE24B4A)
private val AxisGreen = Color(0xFF639922)
private val AxisBlue = Color(0xFF378ADD)

@Composable
fun LinearAccelerationScreen(
    onBack: () -> Unit,
    sensorType: SensorType = SensorType.LINEAR_ACCELERATION
) {
    SensorDetailScaffold(sensorType = sensorType, onBack = onBack) { currentReading, chartReadings ->
        LinearAccelerationContent(currentReading = currentReading, chartReadings = chartReadings)
    }
}

@Composable
private fun LinearAccelerationContent(
    currentReading: SensorReading?,
    chartReadings: List<SensorReading>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = SensorType.LINEAR_ACCELERATION.category.displayName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = SensorType.LINEAR_ACCELERATION.displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AxisValueCard(
                label = "X",
                value = currentReading?.values?.getOrNull(0),
                unit = SensorType.LINEAR_ACCELERATION.unitX,
                labelColor = AxisRed,
                modifier = Modifier.weight(1f)
            )
            AxisValueCard(
                label = "Y",
                value = currentReading?.values?.getOrNull(1),
                unit = SensorType.LINEAR_ACCELERATION.unitY,
                labelColor = AxisGreen,
                modifier = Modifier.weight(1f)
            )
            AxisValueCard(
                label = "Z",
                value = currentReading?.values?.getOrNull(2),
                unit = SensorType.LINEAR_ACCELERATION.unitZ,
                labelColor = AxisBlue,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Live waveform",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "50 Hz",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                MultiAxisLineChart(
                    readings = chartReadings,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        SensorUsageHint(sensorType = SensorType.LINEAR_ACCELERATION)

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun AxisValueCard(
    label: String,
    value: Float?,
    unit: String,
    labelColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(labelColor.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = labelColor
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = formatDetailValue(value),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1C1E)
@Composable
private fun PreviewLinearAccelerationScreen() {
    SensorAppTheme {
        LinearAccelerationContent(
            currentReading = SensorReading(
                sensorType = SensorType.LINEAR_ACCELERATION,
                values = listOf(0.05f, -0.03f, 0.12f),
                accuracy = 3,
                timestampMs = System.currentTimeMillis()
            ),
            chartReadings = emptyList()
        )
    }
}
