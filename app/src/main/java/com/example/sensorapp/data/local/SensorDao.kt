package com.example.sensorapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reading: SensorReadingEntity)

    @Query(
        """
        SELECT * FROM (
            SELECT * FROM sensor_readings
            WHERE sensor_type = :sensorType
            ORDER BY timestamp_ms DESC
            LIMIT :limit
        ) ORDER BY timestamp_ms ASC
        """
    )
    fun getRecentByType(sensorType: String, limit: Int): Flow<List<SensorReadingEntity>>

    @Query("SELECT * FROM sensor_readings WHERE sensor_type = :sensorType ORDER BY timestamp_ms DESC")
    fun getByType(sensorType: String): Flow<List<SensorReadingEntity>>

    @Query("SELECT * FROM sensor_readings WHERE sensor_type = :sensorType ORDER BY timestamp_ms DESC LIMIT :limit")
    fun getByTypeLimited(sensorType: String, limit: Int): Flow<List<SensorReadingEntity>>

    @Query("SELECT * FROM sensor_readings ORDER BY timestamp_ms DESC")
    fun getAll(): Flow<List<SensorReadingEntity>>

    @Query("DELETE FROM sensor_readings WHERE timestamp_ms < :cutoffMs")
    suspend fun deleteOlderThan(cutoffMs: Long)

    @Query("SELECT COUNT(*) FROM sensor_readings")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM sensor_readings WHERE sensor_type = :sensorType")
    suspend fun countByType(sensorType: String): Int
}
