package com.caloriecam.app.data

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val exportedAt: Long,
    val profile: UserProfile? = null,
    val foodLog: List<FoodLogEntry> = emptyList(),
    val weightLog: List<WeightEntry> = emptyList(),
    val waterLog: List<WaterEntry> = emptyList()
)
