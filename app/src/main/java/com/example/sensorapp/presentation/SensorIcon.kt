package com.example.sensorapp.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ThreeDRotation
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.sensorapp.domain.model.SensorType

fun sensorIcon(type: SensorType): ImageVector = when (type) {
    SensorType.ACCELEROMETER -> Icons.Default.AutoGraph
    SensorType.GYROSCOPE -> Icons.Default.RotateRight
    SensorType.MAGNETOMETER -> Icons.Default.Explore
    SensorType.LIGHT -> Icons.Default.WbSunny
    SensorType.PROXIMITY -> Icons.Default.Sensors
    SensorType.PRESSURE -> Icons.Default.Speed
    SensorType.STEP_COUNTER -> Icons.Default.DirectionsWalk
    SensorType.GRAVITY -> Icons.Default.ArrowDownward
    SensorType.LINEAR_ACCELERATION -> Icons.Default.OpenWith
    SensorType.ROTATION_VECTOR -> Icons.Default.ThreeDRotation
}
