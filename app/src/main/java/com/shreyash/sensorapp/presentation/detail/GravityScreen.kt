package com.shreyash.sensorapp.presentation.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shreyash.sensorapp.domain.model.SensorReading
import com.shreyash.sensorapp.domain.model.SensorType
import com.shreyash.sensorapp.presentation.theme.SensorAppTheme
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.math.PI

private val AxisRed = Color(0xFFE24B4A)
private val AxisGreen = Color(0xFF639922)
private val AxisBlue = Color(0xFF378ADD)

@Composable
fun GravityScreen(
    onBack: () -> Unit,
    sensorType: SensorType = SensorType.GRAVITY
) {
    SensorDetailScaffold(sensorType = sensorType, onBack = onBack) { currentReading, chartReadings ->
        GravityContent(currentReading = currentReading, chartReadings = chartReadings)
    }
}

@Composable
private fun GravityContent(
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
            text = "Tilt",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = sensorDisplayName(SensorType.GRAVITY),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        val gx = currentReading?.values?.getOrNull(0) ?: 0f
        val gy = currentReading?.values?.getOrNull(1) ?: 0f
        val gz = currentReading?.values?.getOrNull(2) ?: 9.81f
        val magnitude = sqrt(gx * gx + gy * gy + gz * gz)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BubbleLevel(
                    gx = gx,
                    gy = gy,
                    magnitude = magnitude,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(8.dp)
                )

                Spacer(Modifier.height(8.dp))

                val tiltRight = asin((gx / magnitude).coerceIn(-1f, 1f)) * 180f / PI
                val tiltForward = asin((gy / magnitude).coerceIn(-1f, 1f)) * 180f / PI

                val dirX = when {
                    abs(tiltRight) < 2f -> ""
                    tiltRight > 0 -> "right"
                    else -> "left"
                }
                val dirY = when {
                    abs(tiltForward) < 2f -> ""
                    tiltForward > 0 -> "forward"
                    else -> "backward"
                }
                val tiltText = buildString {
                    val parts = mutableListOf<String>()
                    if (dirX.isNotEmpty()) parts.add("${abs(tiltRight).roundToInt()}° $dirX")
                    if (dirY.isNotEmpty()) parts.add("${abs(tiltForward).roundToInt()}° $dirY")
                    if (parts.isEmpty()) {
                        append("Level")
                    } else {
                        append("Tilted ${parts.joinToString(", ")}")
                    }
                }
                Text(
                    text = tiltText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AxisValueCard(
                label = "X",
                value = currentReading?.values?.getOrNull(0),
                unit = if (SensorType.GRAVITY.unitX.isNotEmpty()) sensorUnitText(SensorType.GRAVITY.unitX) else "",
                labelColor = AxisRed,
                modifier = Modifier.weight(1f)
            )
            AxisValueCard(
                label = "Y",
                value = currentReading?.values?.getOrNull(1),
                unit = if (SensorType.GRAVITY.unitY.isNotEmpty()) sensorUnitText(SensorType.GRAVITY.unitY) else "",
                labelColor = AxisGreen,
                modifier = Modifier.weight(1f)
            )
            AxisValueCard(
                label = "Z",
                value = currentReading?.values?.getOrNull(2),
                unit = if (SensorType.GRAVITY.unitZ.isNotEmpty()) sensorUnitText(SensorType.GRAVITY.unitZ) else "",
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
                LiveLineChart(
                    readings = chartReadings,
                    sensorType = SensorType.GRAVITY,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        SensorUsageHint(sensorType = SensorType.GRAVITY)

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun BubbleLevel(
    gx: Float,
    gy: Float,
    magnitude: Float,
    modifier: Modifier = Modifier
) {
    val bubbleColor = Color(0xFF1D9E75)
    val bubbleDotColor = Color(0xFF04342C)
    val outerRingColor = Color(0xFFB4B2A9)
    val innerRingColor = Color(0xFFD3D1C7)

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = size.minDimension / 2f - 4.dp.toPx()
        val innerRadius = outerRadius * 0.63f
        val maxBubbleRadius = outerRadius * 0.75f

        drawCircle(
            color = outerRingColor,
            radius = outerRadius,
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )
        drawCircle(
            color = innerRingColor,
            radius = innerRadius,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )

        drawLine(
            color = innerRingColor,
            start = Offset(center.x - outerRadius, center.y),
            end = Offset(center.x + outerRadius, center.y),
            strokeWidth = 0.8.dp.toPx()
        )
        drawLine(
            color = innerRingColor,
            start = Offset(center.x, center.y - outerRadius),
            end = Offset(center.x, center.y + outerRadius),
            strokeWidth = 0.8.dp.toPx()
        )

        val normX = (gx / magnitude).coerceIn(-1f, 1f)
        val normY = (gy / magnitude).coerceIn(-1f, 1f)
        val bubbleOffsetX = normX * maxBubbleRadius
        val bubbleOffsetY = -normY * maxBubbleRadius
        val bubblePos = Offset(center.x + bubbleOffsetX, center.y + bubbleOffsetY)
        val bubbleRadius = outerRadius * 0.19f

        drawCircle(
            color = bubbleColor.copy(alpha = 0.85f),
            radius = bubbleRadius,
            center = bubblePos
        )
        drawCircle(
            color = bubbleDotColor,
            radius = bubbleRadius * 0.17f,
            center = bubblePos
        )
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
private fun PreviewGravityScreen() {
    SensorAppTheme {
        GravityContent(
            currentReading = SensorReading(
                sensorType = SensorType.GRAVITY,
                values = listOf(1.36f, 1.03f, 9.65f),
                accuracy = 3,
                timestampMs = System.currentTimeMillis()
            ),
            chartReadings = emptyList()
        )
    }
}
