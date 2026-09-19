package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_intake_logs WHERE dateString = :dateString ORDER BY timestamp DESC")
    fun getLogsForDate(dateString: String): Flow<List<WaterIntakeLog>>

    @Query("SELECT * FROM water_intake_logs WHERE dateString >= :startDate AND dateString <= :endDate ORDER BY timestamp ASC")
    fun getLogsBetweenDates(startDate: String, endDate: String): Flow<List<WaterIntakeLog>>

    @Query("SELECT * FROM water_intake_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<WaterIntakeLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WaterIntakeLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<WaterIntakeLog>)

    @Query("DELETE FROM water_intake_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("SELECT * FROM hydration_config WHERE id = 1")
    fun getConfig(): Flow<HydrationConfig?>

    @Query("SELECT * FROM hydration_config WHERE id = 1")
    suspend fun getConfigDirect(): HydrationConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: HydrationConfig)

    @Query("SELECT COUNT(*) FROM water_intake_logs")
    suspend fun countLogs(): Int
}
