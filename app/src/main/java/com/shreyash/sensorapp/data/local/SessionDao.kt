package com.shreyash.sensorapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert
    suspend fun insert(session: LogSessionEntity): Long

    @Update
    suspend fun update(session: LogSessionEntity)

    @Query("SELECT * FROM log_sessions WHERE id = :id")
    suspend fun getById(id: Long): LogSessionEntity?

    @Query("SELECT * FROM log_sessions ORDER BY start_time_ms DESC")
    fun getAll(): Flow<List<LogSessionEntity>>

    @Query("SELECT * FROM log_sessions WHERE sensor_type = :sensorType ORDER BY start_time_ms DESC")
    fun getByType(sensorType: String): Flow<List<LogSessionEntity>>

    @Query("DELETE FROM log_sessions")
    suspend fun deleteAll()
}
