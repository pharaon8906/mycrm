package com.caloriecam.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressPhotoDao {

    @Insert
    suspend fun insert(photo: ProgressPhoto): Long

    @Delete
    suspend fun delete(photo: ProgressPhoto)

    @Query("SELECT * FROM progress_photos ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<ProgressPhoto>>
}
