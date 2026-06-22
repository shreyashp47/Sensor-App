package com.example.sensorapp.domain.model

enum class SensorCategory(val displayName: String) {
    MOTION("Motion"),
    ENVIRONMENTAL("Environmental"),
    POSITION("Position"),
    ACTIVITY("Activity")
}

enum class SensorType(
    val displayName: String,
    val description: String,
    val axisCount: Int,
    val category: SensorCategory,
    val unitX: String = "",
    val unitY: String = "",
    val unitZ: String = "",
    val unitSingle: String? = null
) {
    ACCELEROMETER(
        displayName = "Accelerometer",
        description = "Measures acceleration forces applied to the device, including gravity.",
        axisCount = 3, category = SensorCategory.MOTION,
        unitX = "m/s²", unitY = "m/s²", unitZ = "m/s²"
    ),
    GYROSCOPE(
        displayName = "Gyroscope",
        description = "Measures the device's rate of rotation around each axis.",
        axisCount = 3, category = SensorCategory.MOTION,
        unitX = "°/s", unitY = "°/s", unitZ = "°/s"
    ),
    LINEAR_ACCELERATION(
        displayName = "Linear Acceleration",
        description = "Measures the acceleration forces excluding gravity.",
        axisCount = 3, category = SensorCategory.MOTION,
        unitX = "m/s²", unitY = "m/s²", unitZ = "m/s²"
    ),
    MAGNETOMETER(
        displayName = "Magnetometer",
        description = "Measures the ambient magnetic field in the environment.",
        axisCount = 3, category = SensorCategory.POSITION,
        unitX = "µT", unitY = "µT", unitZ = "µT"
    ),
    GRAVITY(
        displayName = "Gravity",
        description = "Measures the direction and magnitude of gravity.",
        axisCount = 3, category = SensorCategory.POSITION,
        unitX = "m/s²", unitY = "m/s²", unitZ = "m/s²"
    ),
    ROTATION_VECTOR(
        displayName = "Rotation Vector",
        description = "Measures the orientation of the device relative to the world coordinate system.",
        axisCount = 3, category = SensorCategory.POSITION,
        unitX = "", unitY = "", unitZ = ""
    ),
    LIGHT(
        displayName = "Light",
        description = "Measures the ambient light level (illuminance).",
        axisCount = 1, category = SensorCategory.ENVIRONMENTAL, unitSingle = "lx"
    ),
    PROXIMITY(
        displayName = "Proximity",
        description = "Measures the distance of an object relative to the device screen.",
        axisCount = 1, category = SensorCategory.ENVIRONMENTAL, unitSingle = "cm"
    ),
    PRESSURE(
        displayName = "Barometer",
        description = "Measures the ambient air pressure.",
        axisCount = 1, category = SensorCategory.ENVIRONMENTAL, unitSingle = "hPa"
    ),
    STEP_COUNTER(
        displayName = "Step Counter",
        description = "Counts the number of steps taken since the last device reboot.",
        axisCount = 1, category = SensorCategory.ACTIVITY, unitSingle = "steps"
    );
}
