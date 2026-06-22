package com.shreyash.sensorapp.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "log_sessions")
data class LogSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "sensor_type") val sensorType: String,
    @ColumnInfo(name = "start_time_ms") val startTimeMs: Long,
    @ColumnInfo(name = "end_time_ms") val endTimeMs: Long? = null,
    @ColumnInfo(name = "reading_count") val readingCount: Int = 0,
    @ColumnInfo(name = "summary") val summary: String = ""
)
