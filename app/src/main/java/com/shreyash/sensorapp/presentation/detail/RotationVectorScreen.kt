package com.shreyash.sensorapp.presentation.detail

import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shreyash.sensorapp.domain.model.SensorReading
import com.shreyash.sensorapp.domain.model.SensorType
import com.shreyash.sensorapp.presentation.theme.SensorAppTheme
import kotlin.math.abs
import kotlin.math.roundToInt

private val CardBg = Color(0xFFCECBF6)
private val CardStroke = Color(0xFF534AB7)
private val CardHomeBtn = Color(0xFF3C3489)
private val CardScreen = Color(0xFFF1EFE8)

@Composable
fun RotationVectorScreen(
    onBack: () -> Unit,
    sensorType: SensorType = SensorType.ROTATION_VECTOR
) {
    SensorDetailScaffold(sensorType = sensorType, onBack = onBack) { currentReading, chartReadings ->
        RotationVectorContent(currentReading = currentReading, chartReadings = chartReadings)
    }
}

@Composable
private fun RotationVectorContent(
    currentReading: SensorReading?,
    chartReadings: List<SensorReading>
) {
    val values = currentReading?.values
    val azimuth = values?.let { computeAzimuth(it) } ?: 0f
    val pitch = values?.let { computePitch(it) } ?: 0f
    val roll = values?.let { computeRoll(it) } ?: 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Orientation",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = sensorDisplayName(SensorType.ROTATION_VECTOR),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

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
                PhoneOrientationIndicator(
                    azimuth = azimuth,
                    pitch = pitch,
                    roll = roll,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(8.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EulerCard(
                label = "Azimuth",
                value = azimuth.roundToInt().toString() + "°",
                modifier = Modifier.weight(1f)
            )
            EulerCard(
                label = "Pitch",
                value = pitch.roundToInt().toString() + "°",
                modifier = Modifier.weight(1f)
            )
            EulerCard(
                label = "Roll",
                value = roll.roundToInt().toString() + "°",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Quaternion (x, y, z, w)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(4.dp))
                val qx = formatDetailValue(values?.getOrNull(0))
                val qy = formatDetailValue(values?.getOrNull(1))
                val qz = formatDetailValue(values?.getOrNull(2))
                val qw = formatDetailValue(values?.getOrNull(3)) { v -> String.format("%.2f", v) }
                Text(
                    text = "$qx, $qy, $qz, $qw",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
                    sensorType = SensorType.ROTATION_VECTOR,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        SensorUsageHint(sensorType = SensorType.ROTATION_VECTOR)

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PhoneOrientationIndicator(
    azimuth: Float,
    pitch: Float,
    roll: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val outerR = size.minDimension / 2f - 4.dp.toPx()
        val outerColor = Color(0xFFB4B2A9)
        val innerColor = Color(0xFFD3D1C7)

        drawCircle(color = outerColor, radius = outerR, center = c, style = Stroke(1.5.dp.toPx()))
        drawCircle(color = innerColor, radius = outerR * 0.63f, center = c, style = Stroke(1.dp.toPx()))
        drawLine(color = innerColor, start = Offset(c.x - outerR, c.y), end = Offset(c.x + outerR, c.y), strokeWidth = 0.8.dp.toPx())
        drawLine(color = innerColor, start = Offset(c.x, c.y - outerR), end = Offset(c.x, c.y + outerR), strokeWidth = 0.8.dp.toPx())

        val pw = outerR * 0.53f
        val ph = outerR * 0.95f
        val cr = pw * 0.18f
        val scaleY = (1f - abs(pitch) * 0.003f).coerceIn(0.7f, 1f)

        withTransform({
            translate(left = c.x, top = c.y)
            rotate(degrees = azimuth + roll * 0.3f)
            if (scaleY < 1f) {
                scale(scaleX = 1f, scaleY = scaleY)
            }
        }) {
            val path = Path().apply {
                addRoundRect(roundRect = androidx.compose.ui.geometry.RoundRect(
                    left = -pw / 2f, top = -ph / 2f, right = pw / 2f, bottom = ph / 2f,
                    cornerRadius = CornerRadius(cr, cr)
                ))
            }
            drawPath(path, color = CardBg, style = Stroke(2.dp.toPx()))
            drawPath(path, color = CardStroke, style = Stroke(1.5.dp.toPx()))
            drawCircle(color = CardHomeBtn, radius = pw * 0.07f, center = Offset(0f, ph * 0.31f))
            val screenPath = Path().apply {
                addRoundRect(roundRect = androidx.compose.ui.geometry.RoundRect(
                    left = -pw * 0.28f, top = -ph * 0.38f, right = pw * 0.28f, bottom = ph * 0.1f,
                    cornerRadius = CornerRadius(cr * 0.5f, cr * 0.5f)
                ))
            }
            drawPath(screenPath, color = CardScreen)
            drawPath(screenPath, color = CardStroke.copy(alpha = 0.3f), style = Stroke(0.5.dp.toPx()))
        }
    }
}

@Composable
private fun EulerCard(
    label: String,
    value: String,
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
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun computeAzimuth(values: List<Float>): Float {
    if (values.size < 4) return 0f
    val r = FloatArray(9)
    SensorManager.getRotationMatrixFromVector(r, values.toFloatArray())
    val orientation = FloatArray(3)
    SensorManager.getOrientation(r, orientation)
    return orientation[0] * 180f / Math.PI.toFloat()
}

private fun computePitch(values: List<Float>): Float {
    if (values.size < 4) return 0f
    val r = FloatArray(9)
    SensorManager.getRotationMatrixFromVector(r, values.toFloatArray())
    val orientation = FloatArray(3)
    SensorManager.getOrientation(r, orientation)
    return orientation[1] * 180f / Math.PI.toFloat()
}

private fun computeRoll(values: List<Float>): Float {
    if (values.size < 4) return 0f
    val r = FloatArray(9)
    SensorManager.getRotationMatrixFromVector(r, values.toFloatArray())
    val orientation = FloatArray(3)
    SensorManager.getOrientation(r, orientation)
    return orientation[2] * 180f / Math.PI.toFloat()
}

private fun formatDetailValue(value: Float?, formatter: (Float) -> String = { v -> String.format("%.2f", v) }): String {
    if (value == null) return "--"
    return formatter(value)
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1C1E)
@Composable
private fun PreviewRotationVectorScreen() {
    SensorAppTheme {
        RotationVectorContent(
            currentReading = SensorReading(
                sensorType = SensorType.ROTATION_VECTOR,
                values = listOf(0.08f, 0.16f, 0.36f, 0.92f),
                accuracy = 3,
                timestampMs = System.currentTimeMillis()
            ),
            chartReadings = emptyList()
        )
    }
}
