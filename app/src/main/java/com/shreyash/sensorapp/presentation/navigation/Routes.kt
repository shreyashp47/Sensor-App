package com.shreyash.sensorapp.presentation.navigation

import com.shreyash.sensorapp.domain.model.SensorType

sealed class Route(val route: String) {
    data object Dashboard : Route("dashboard")
    data object History : Route("history")
    data object Settings : Route("settings")
    data object Compass : Route("compass")

    data object Accelerometer : Route("sensor/accelerometer")
    data object Gyroscope : Route("sensor/gyroscope")
    data object LinearAcceleration : Route("sensor/linear_acceleration")
    data object Magnetometer : Route("sensor/magnetometer")
    data object Gravity : Route("sensor/gravity")
    data object RotationVector : Route("sensor/rotation_vector")
    data object Light : Route("sensor/light")
    data object Proximity : Route("sensor/proximity")
    data object Pressure : Route("sensor/pressure")
    data object StepCounter : Route("sensor/step_counter")
}

fun SensorType.toRoute(): String = when (this) {
    SensorType.ACCELEROMETER -> Route.Accelerometer.route
    SensorType.GYROSCOPE -> Route.Gyroscope.route
    SensorType.LINEAR_ACCELERATION -> Route.LinearAcceleration.route
    SensorType.MAGNETOMETER -> Route.Magnetometer.route
    SensorType.GRAVITY -> Route.Gravity.route
    SensorType.ROTATION_VECTOR -> Route.RotationVector.route
    SensorType.LIGHT -> Route.Light.route
    SensorType.PROXIMITY -> Route.Proximity.route
    SensorType.PRESSURE -> Route.Pressure.route
    SensorType.STEP_COUNTER -> Route.StepCounter.route
}
