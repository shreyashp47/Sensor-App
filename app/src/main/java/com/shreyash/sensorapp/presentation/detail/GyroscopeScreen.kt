package com.shreyash.sensorapp.presentation.detail

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shreyash.sensorapp.domain.model.SensorReading
import com.shreyash.sensorapp.domain.model.SensorType
import com.shreyash.sensorapp.presentation.theme.SensorAppTheme
import com.shreyash.sensorapp.presentation.theme.SensorGreen
import com.shreyash.sensorapp.presentation.detail.sensorUnitText
import kotlin.math.sqrt

@Composable
fun GyroscopeScreen(
    onBack: () -> Unit,
    sensorType: SensorType = SensorType.GYROSCOPE
) {
    SensorDetailScaffold(sensorType = sensorType, onBack = onBack) { currentReading, chartReadings ->
        GyroscopeContent(currentReading = currentReading, chartReadings = chartReadings)
    }
}

@Composable
private fun GyroscopeContent(
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
        Spacer(Modifier.height(16.dp))

        if (currentReading != null) {
            RotationMagnitudeGauge(reading = currentReading)

            Spacer(Modifier.height(16.dp))

            AxisValuesCard(reading = currentReading)
        } else {
            Text(
                text = "Waiting for sensor data...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(20.dp))

        LiveLineChart(
            readings = chartReadings,
            sensorType = SensorType.GYROSCOPE,
            modifier = Modifier.fillMaxWidth().height(240.dp)
        )

        Spacer(Modifier.height(16.dp))

        if (currentReading != null) {
            GyroscopeCube(
                reading = currentReading,
                modifier = Modifier.fillMaxWidth().height(260.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        SensorUsageHint(sensorType = SensorType.GYROSCOPE)

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun RotationMagnitudeGauge(reading: SensorReading) {
    val gx = reading.values.getOrNull(0) ?: 0f
    val gy = reading.values.getOrNull(1) ?: 0f
    val gz = reading.values.getOrNull(2) ?: 0f
    val magnitude = sqrt(gx * gx + gy * gy + gz * gz)

    val maxSpeed = 15f
    val fraction = (magnitude / maxSpeed).coerceIn(0f, 1f)

    val arcColor = when {
        fraction < 0.33f -> SensorGreen
        fraction < 0.66f -> Color(0xFFFFC107)
        else -> Color(0xFFE53935)
    }
    val arcBgColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ROTATION SPEED",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                letterSpacing = 0.5.sp
            )

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                    drawArc(
                        color = arcBgColor,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )

                    drawArc(
                        color = arcColor,
                        startAngle = 135f,
                        sweepAngle = 270f * fraction,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%.1f", magnitude),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = arcColor
                    )
                    Text(
                        text = "°/s",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SpeedLabel("Slow", SensorGreen)
                SpeedLabel("Moderate", Color(0xFFFFC107))
                SpeedLabel("Fast", Color(0xFFE53935))
            }
        }
    }
}

@Composable
private fun SpeedLabel(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun AxisValuesCard(reading: SensorReading) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AXIS VALUES",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                letterSpacing = 0.5.sp
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AxisValueCard(
                    label = "X",
                    value = reading.values.getOrNull(0) ?: 0f,
                    unit = sensorUnitText(SensorType.GYROSCOPE.unitX),
                    color = Color(0xFF42A5F5)
                )
                AxisValueCard(
                    label = "Y",
                    value = reading.values.getOrNull(1) ?: 0f,
                    unit = sensorUnitText(SensorType.GYROSCOPE.unitY),
                    color = SensorGreen
                )
                AxisValueCard(
                    label = "Z",
                    value = reading.values.getOrNull(2) ?: 0f,
                    unit = sensorUnitText(SensorType.GYROSCOPE.unitZ),
                    color = Color(0xFFFF7043)
                )
            }
        }
    }
}

@Composable
private fun AxisValueCard(
    label: String,
    value: Float,
    unit: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = String.format("%.1f", value),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1C1E)
@Composable
private fun PreviewGyroscopeScreen() {
    SensorAppTheme {
        GyroscopeContent(
            currentReading = SensorReading(
                sensorType = SensorType.GYROSCOPE,
                values = listOf(1.2f, -0.8f, 0.5f),
                accuracy = 3,
                timestampMs = System.currentTimeMillis()
            ),
            chartReadings = emptyList()
        )
    }
}
