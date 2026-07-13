package com.shreyash.sensorapp.presentation.detail

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.shreyash.sensorapp.R
import com.shreyash.sensorapp.domain.model.SensorCategory
import com.shreyash.sensorapp.domain.model.SensorType

@Composable
fun sensorDisplayName(type: SensorType): String = stringResource(sensorDisplayNameRes(type))

@StringRes
fun sensorDisplayNameRes(type: SensorType): Int = when (type) {
    SensorType.ACCELEROMETER -> R.string.sensor_accelerometer
    SensorType.GYROSCOPE -> R.string.sensor_gyroscope
    SensorType.LINEAR_ACCELERATION -> R.string.sensor_linear_acceleration
    SensorType.MAGNETOMETER -> R.string.sensor_magnetometer
    SensorType.GRAVITY -> R.string.sensor_gravity
    SensorType.ROTATION_VECTOR -> R.string.sensor_rotation_vector
    SensorType.LIGHT -> R.string.sensor_light
    SensorType.PROXIMITY -> R.string.sensor_proximity
    SensorType.PRESSURE -> R.string.sensor_barometer
    SensorType.STEP_COUNTER -> R.string.sensor_step_counter
}

@Composable
fun sensorCategoryName(category: SensorCategory): String = stringResource(sensorCategoryRes(category))

@StringRes
fun sensorCategoryRes(category: SensorCategory): Int = when (category) {
    SensorCategory.MOTION -> R.string.category_motion
    SensorCategory.ENVIRONMENTAL -> R.string.category_environmental
    SensorCategory.POSITION -> R.string.category_position
    SensorCategory.ACTIVITY -> R.string.category_activity
}

@Composable
fun sensorDescription(type: SensorType): String = stringResource(sensorDescriptionRes(type))

@StringRes
fun sensorDescriptionRes(type: SensorType): Int = when (type) {
    SensorType.ACCELEROMETER -> R.string.sensor_accelerometer_desc
    SensorType.GYROSCOPE -> R.string.sensor_gyroscope_desc
    SensorType.LINEAR_ACCELERATION -> R.string.sensor_linear_acceleration_desc
    SensorType.MAGNETOMETER -> R.string.sensor_magnetometer_desc
    SensorType.GRAVITY -> R.string.sensor_gravity_desc
    SensorType.ROTATION_VECTOR -> R.string.sensor_rotation_vector_desc
    SensorType.LIGHT -> R.string.sensor_light_desc
    SensorType.PROXIMITY -> R.string.sensor_proximity_desc
    SensorType.PRESSURE -> R.string.sensor_barometer_desc
    SensorType.STEP_COUNTER -> R.string.sensor_step_counter_desc
}

@Composable
fun sensorUsageHint(type: SensorType): String = stringResource(sensorUsageHintRes(type))

@StringRes
fun sensorUsageHintRes(type: SensorType): Int = when (type) {
    SensorType.ACCELEROMETER -> R.string.hint_accelerometer
    SensorType.GYROSCOPE -> R.string.hint_gyroscope
    SensorType.LINEAR_ACCELERATION -> R.string.hint_linear_acceleration
    SensorType.MAGNETOMETER -> R.string.hint_magnetometer
    SensorType.GRAVITY -> R.string.hint_gravity
    SensorType.ROTATION_VECTOR -> R.string.hint_rotation_vector
    SensorType.LIGHT -> R.string.hint_light
    SensorType.PROXIMITY -> R.string.hint_proximity
    SensorType.PRESSURE -> R.string.hint_pressure
    SensorType.STEP_COUNTER -> R.string.hint_step_counter
}

@Composable
fun sensorUnitText(unit: String): String = stringResource(sensorUnitRes(unit))

@StringRes
fun sensorUnitRes(unit: String): Int = when (unit) {
    "m/s²" -> R.string.unit_mps2
    "°/s" -> R.string.unit_dps
    "µT" -> R.string.unit_microtesla
    "lx" -> R.string.unit_lux
    "cm" -> R.string.unit_cm
    "hPa" -> R.string.unit_hpa
    "steps" -> R.string.unit_steps
    else -> throw IllegalArgumentException("Unknown unit: $unit")
}
