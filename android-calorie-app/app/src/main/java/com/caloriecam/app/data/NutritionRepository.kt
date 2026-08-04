package com.caloriecam.app.data

import android.content.Context
import kotlinx.serialization.json.Json

/**
 * Loads the bundled food database (assets/nutrition_uk.json) and provides
 * lookup by search text or by ML Kit image-labeling output.
 */
object NutritionRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private var cache: List<NutritionItem>? = null

    fun items(context: Context): List<NutritionItem> {
        cache?.let { return it }
        val text = context.assets.open("nutrition_uk.json").bufferedReader().use { it.readText() }
        val parsed = json.decodeFromString<List<NutritionItem>>(text)
        cache = parsed
        return parsed
    }

    fun byId(context: Context, id: String): NutritionItem? =
        items(context).firstOrNull { it.id == id }

    fun search(context: Context, query: String): List<NutritionItem> {
        val all = items(context)
        if (query.isBlank()) return all
        val q = query.trim().lowercase()
        return all.filter { it.nameUk.lowercase().contains(q) }
    }

    /**
     * Matches raw ML Kit image labels (English, confidence-ordered) against the
     * database. Returns matched items in the same order as the input labels,
     * de-duplicated, best guesses first.
     */
    fun matchLabels(context: Context, labels: List<String>): List<NutritionItem> {
        val all = items(context)
        val result = LinkedHashSet<NutritionItem>()
        for (label in labels) {
            val lbl = label.trim().lowercase()
            if (lbl.isEmpty()) continue
            val match = all.firstOrNull { item ->
                item.mlLabels.any { alias ->
                    val a = alias.lowercase()
                    a == lbl || lbl.contains(a) || a.contains(lbl)
                }
            }
            if (match != null) result.add(match)
        }
        return result.toList()
    }
}
