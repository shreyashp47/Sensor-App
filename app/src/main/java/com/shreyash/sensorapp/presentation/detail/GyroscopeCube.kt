package com.shreyash.sensorapp.presentation.detail

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.shreyash.sensorapp.domain.model.SensorReading
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GyroscopeCube(
    reading: SensorReading,
    modifier: Modifier = Modifier
) {
    val gx = reading.values.getOrNull(0) ?: 0f
    val gy = reading.values.getOrNull(1) ?: 0f
    val gz = reading.values.getOrNull(2) ?: 0f

    val rotationX = remember { Animatable(0f) }
    val rotationY = remember { Animatable(0f) }
    val rotationZ = remember { Animatable(0f) }

    LaunchedEffect(gx, gy, gz) {
        rotationX.snapTo(rotationX.value + gx * 0.08f)
        rotationY.snapTo(rotationY.value + gy * 0.08f)
        rotationZ.snapTo(rotationZ.value + gz * 0.08f)
    }

    val primary = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "3D Visualization",
                style = MaterialTheme.typography.labelLarge,
                color = onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val s = minOf(cx, cy) * 0.55f

                val rx = rotationX.value
                val ry = rotationY.value
                val rz = rotationZ.value

                val vertices = listOf(
                    floatArrayOf(-1f, -1f, -1f), floatArrayOf(1f, -1f, -1f),
                    floatArrayOf(1f, -1f, 1f), floatArrayOf(-1f, -1f, 1f),
                    floatArrayOf(-1f, 1f, -1f), floatArrayOf(1f, 1f, -1f),
                    floatArrayOf(1f, 1f, 1f), floatArrayOf(-1f, 1f, 1f)
                )

                val rotated = vertices.map { v ->
                    var x = v[0]; var y = v[1]; var z = v[2]

                    val rxRad = Math.toRadians(rx.toDouble()).toFloat()
                    val ryRad = Math.toRadians(ry.toDouble()).toFloat()
                    val rzRad = Math.toRadians(rz.toDouble()).toFloat()

                    val cosX = cos(rxRad); val sinX = sin(rxRad)
                    val cosY = cos(ryRad); val sinY = sin(ryRad)
                    val cosZ = cos(rzRad); val sinZ = sin(rzRad)

                    val y1 = y * cosX - z * sinX
                    val z1 = y * sinX + z * cosX
                    y = y1; z = z1

                    val x1 = x * cosY + z * sinY
                    z = -x * sinY + z * cosY
                    x = x1

                    val xRot = x * cosZ - y * sinZ
                    val yRot = x * sinZ + y * cosZ
                    x = xRot; y = yRot

                    Triple(x, y, z)
                }

                val projected = rotated.map { (x, y, _) ->
                    Offset(cx + x * s, cy + y * s)
                }

                val edges = listOf(
                    0 to 1, 1 to 2, 2 to 3, 3 to 0,
                    4 to 5, 5 to 6, 6 to 7, 7 to 4,
                    0 to 4, 1 to 5, 2 to 6, 3 to 7
                )

                for ((i, j) in edges) {
                    val zAvg = (rotated[i].third + rotated[j].third) / 2f
                    val alpha = ((zAvg + 1.5f) / 3f).coerceIn(0.25f, 1f)
                    drawLine(
                        color = primary.copy(alpha = alpha),
                        start = projected[i],
                        end = projected[j],
                        strokeWidth = 2.5.dp.toPx()
                    )
                }

                for (v in projected) {
                    drawCircle(color = primary, radius = 3.5.dp.toPx(), center = v)
                }
            }
        }
    }
}
