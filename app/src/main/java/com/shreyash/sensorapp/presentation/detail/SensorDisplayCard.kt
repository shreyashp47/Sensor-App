package com.shreyash.sensorapp.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shreyash.sensorapp.domain.model.SensorReading
import com.shreyash.sensorapp.domain.model.SensorType
import com.shreyash.sensorapp.presentation.theme.SensorGreen

@Composable
fun LiveValueDisplay(
    reading: SensorReading?,
    sensorType: SensorType
) {
    if (reading == null) {
        Text(
            text = "Waiting for sensor data...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    if (sensorType == SensorType.PRESSURE) {
        PressureDisplay(reading)
    } else if (sensorType == SensorType.LIGHT) {
        LightDisplay(reading)
    } else if (sensorType == SensorType.PROXIMITY) {
        ProximityDisplay(reading)
    } else if (sensorType.axisCount == 1) {
        SingleAxisDisplay(reading, sensorType)
    } else {
        MultiAxisDisplay(reading, sensorType)
    }
}

@Composable
private fun PressureDisplay(reading: SensorReading) {
    val value = reading.values.firstOrNull() ?: 0f
    val condition = when {
        value < 980f -> "STORM"
        value < 1010f -> "RAIN"
        value < 1025f -> "NORMAL"
        else -> "HIGH"
    }
    val conditionColor = when (condition) {
        "STORM" -> Color(0xFFE53935)
        "RAIN" -> Color(0xFF42A5F5)
        "NORMAL" -> SensorGreen
        else -> Color(0xFFFFB300)
    }

    SensorCard {
        Text(
            text = condition,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = conditionColor
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = when (condition) {
                "STORM" -> "Very low pressure — stormy weather likely"
                "RAIN" -> "Low pressure — rain or unsettled weather"
                "NORMAL" -> "Standard atmospheric pressure"
                else -> "High pressure — fair and stable weather"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "${formatLargeValue(value)} hPa",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Sea level: 1013.25 hPa",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun LightDisplay(reading: SensorReading) {
    val value = reading.values.firstOrNull() ?: 0f
    val level = when {
        value <= 1f -> "DARK"
        value <= 50f -> "DIM"
        value <= 500f -> "INDOOR"
        value <= 10000f -> "OUTDOOR"
        else -> "SUNLIGHT"
    }
    val levelColor = when (level) {
        "DARK" -> Color(0xFF6B6B6B)
        "DIM" -> Color(0xFF9E9E9E)
        "INDOOR" -> Color(0xFFFFD54F)
        "OUTDOOR" -> Color(0xFFFFB300)
        else -> Color(0xFFFF6F00)
    }
    val fraction = (value / 50000f).coerceIn(0f, 1f)

    SensorCard {
        Text(
            text = level,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = levelColor
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = when (level) {
                "DARK" -> "Very little or no light detected"
                "DIM" -> "Low light conditions, like a dimly lit room"
                "INDOOR" -> "Typical indoor lighting level"
                "OUTDOOR" -> "Outdoor conditions, cloudy or shaded"
                else -> "Bright direct sunlight"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(listOf(
                            Color(0xFF6B6B6B),
                            Color(0xFFFFD54F),
                            Color(0xFFFF6F00)
                        ))
                    )
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "${formatLargeValue(value)} lx",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ProximityDisplay(reading: SensorReading) {
    val value = reading.values.firstOrNull() ?: 0f
    val isObstructed = value < 1f

    SensorCard {
        Text(
            text = if (isObstructed) "OBSTRUCTED" else "CLEAR",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = if (isObstructed) Color(0xFFFF6B6B) else SensorGreen
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = if (isObstructed) "An object is covering the sensor"
            else "No object detected near the sensor",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SingleAxisDisplay(reading: SensorReading, sensorType: SensorType) {
    val value = reading.values.firstOrNull() ?: 0f
    Text(
        text = formatLargeValue(value),
        style = MaterialTheme.typography.displayLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        maxLines = 1
    )
    if (sensorType.unitSingle != null && sensorType.unitSingle.isNotEmpty()) {
        Text(
            text = sensorType.unitSingle,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun MultiAxisDisplay(reading: SensorReading, sensorType: SensorType) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AxisValue(
            label = "X",
            value = reading.values.getOrNull(0) ?: 0f,
            unit = sensorType.unitX
        )
        if (sensorType.axisCount >= 2) {
            AxisValue(
                label = "Y",
                value = reading.values.getOrNull(1) ?: 0f,
                unit = sensorType.unitY
            )
        }
        if (sensorType.axisCount >= 3) {
            AxisValue(
                label = "Z",
                value = reading.values.getOrNull(2) ?: 0f,
                unit = sensorType.unitZ
            )
        }
    }
}

@Composable
fun AxisValue(
    label: String,
    value: Float,
    unit: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.widthIn(min = 96.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = formatLargeValue(value),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        if (unit.isNotEmpty()) {
            Text(
                text = unit,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SensorCard(
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}
