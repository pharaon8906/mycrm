package com.caloriecam.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Not included in JSON backups — photoPath is a local file path that
 * wouldn't resolve on another device, same limitation as FoodLogEntry.
 */
@Entity(tableName = "progress_photos")
data class ProgressPhoto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val photoPath: String,
    val timestamp: Long
)
