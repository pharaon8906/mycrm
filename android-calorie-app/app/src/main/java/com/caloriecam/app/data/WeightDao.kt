package com.caloriecam.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {

    @Insert
    suspend fun insert(entry: WeightEntry): Long

    @Delete
    suspend fun delete(entry: WeightEntry)

    @Query("SELECT * FROM weight_log ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_log WHERE timestamp >= :since ORDER BY timestamp ASC")
    fun observeSince(since: Long): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_log ORDER BY timestamp DESC LIMIT 1")
    suspend fun latest(): WeightEntry?

    @Query("SELECT * FROM weight_log ORDER BY timestamp DESC")
    suspend fun getAll(): List<WeightEntry>

    @Insert
    suspend fun insertAll(entries: List<WeightEntry>)

    @Query("DELETE FROM weight_log")
    suspend fun clearAll()
}
