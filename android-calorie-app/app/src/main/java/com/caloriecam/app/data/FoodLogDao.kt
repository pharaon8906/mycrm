package com.caloriecam.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodLogDao {

    @Insert
    suspend fun insert(entry: FoodLogEntry): Long

    @Delete
    suspend fun delete(entry: FoodLogEntry)

    @Query("SELECT * FROM food_log WHERE timestamp >= :dayStart AND timestamp < :dayEnd ORDER BY timestamp DESC")
    fun observeForDay(dayStart: Long, dayEnd: Long): Flow<List<FoodLogEntry>>

    @Query(
        "SELECT strftime('%Y-%m-%d', timestamp / 1000, 'unixepoch', 'localtime') AS day, SUM(kcal) AS total " +
            "FROM food_log WHERE timestamp >= :since GROUP BY day ORDER BY day ASC"
    )
    fun observeDailyTotals(since: Long): Flow<List<DailyTotal>>

    @Query("SELECT * FROM food_log ORDER BY timestamp DESC")
    suspend fun getAll(): List<FoodLogEntry>

    @Insert
    suspend fun insertAll(entries: List<FoodLogEntry>)

    @Query("DELETE FROM food_log")
    suspend fun clearAll()
}

data class DailyTotal(val day: String, val total: Int)
