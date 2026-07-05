package com.shreyash.sensorapp.presentation.detail

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shreyash.sensorapp.domain.model.SensorType
import com.shreyash.sensorapp.presentation.theme.SensorGreen

@Composable
fun LiveIndicator() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        )
    )
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SensorGreen.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(SensorGreen.copy(alpha = alpha))
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "LIVE",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = SensorGreen
            )
        }
    }
}

@Composable
fun SensorUsageHint(sensorType: SensorType) {
    val hint = when (sensorType) {
        SensorType.ACCELEROMETER -> "Tilt or shake the device to see acceleration forces change across X, Y, and Z axes."
        SensorType.GYROSCOPE -> "Rotate the device to measure the rate of rotation around each axis."
        SensorType.LINEAR_ACCELERATION -> "Move the device quickly to measure acceleration excluding gravity."
        SensorType.MAGNETOMETER -> "Move the device near a metal object or wave it in a figure-8 pattern to test."
        SensorType.GRAVITY -> "Tilt the device to see how gravity distributes across each axis."
        SensorType.ROTATION_VECTOR -> "Rotate the device to observe orientation changes relative to the world."
        SensorType.LIGHT -> "Cover and uncover the light sensor (usually near the front camera) to see lux level changes."
        SensorType.PROXIMITY -> "Cover the top of the device to trigger the sensor. Used to detect when the phone is held to the ear."
        SensorType.PRESSURE -> "Ambient air pressure changes with altitude. Try moving to a different floor or elevation."
        SensorType.STEP_COUNTER -> "Walk or simulate steps to see the step count increment in real time."
    }

    Text(
        text = hint,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        textAlign = TextAlign.Center
    )
}

fun formatLargeValue(value: Float): String {
    return String.format("%.1f", value)
}

fun formatDetailValue(value: Float?): String {
    if (value == null) return "--"
    return formatLargeValue(value)
}
