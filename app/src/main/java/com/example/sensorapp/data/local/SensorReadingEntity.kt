package com.example.sensorapp.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensor_readings")
data class SensorReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "sensor_type") val sensorType: String,
    @ColumnInfo(name = "values") val values: List<Float>,
    @ColumnInfo(name = "accuracy") val accuracy: Int,
    @ColumnInfo(name = "timestamp_ms") val timestampMs: Long
)
