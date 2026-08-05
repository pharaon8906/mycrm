package com.caloriecam.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {

    @Insert
    suspend fun insert(entry: WaterEntry): Long

    @Delete
    suspend fun delete(entry: WaterEntry)

    @Query("SELECT * FROM water_log WHERE timestamp >= :dayStart AND timestamp < :dayEnd ORDER BY timestamp DESC")
    fun observeForDay(dayStart: Long, dayEnd: Long): Flow<List<WaterEntry>>

    @Query("SELECT * FROM water_log ORDER BY timestamp DESC")
    suspend fun getAll(): List<WaterEntry>

    @Insert
    suspend fun insertAll(entries: List<WaterEntry>)

    @Query("DELETE FROM water_log")
    suspend fun clearAll()
}
