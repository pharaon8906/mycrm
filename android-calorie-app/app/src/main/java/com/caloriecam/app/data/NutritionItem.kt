package com.caloriecam.app.data

import kotlinx.serialization.Serializable

@Serializable
data class NutritionItem(
    val id: String,
    val nameUk: String,
    val kcal100: Double,
    val protein100: Double,
    val fat100: Double,
    val carbs100: Double,
    val defaultGrams: Int,
    val mlLabels: List<String> = emptyList(),
    /** Short "why it's good for you" blurb. Empty when not curated yet. */
    val benefits: String = "",
    /** Main vitamins/minerals, e.g. "Вітамін C, калій". Empty when not curated yet. */
    val vitamins: String = ""
)
