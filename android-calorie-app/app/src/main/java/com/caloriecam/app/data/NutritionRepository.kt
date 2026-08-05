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

    /**
     * ML Kit's on-device model is a *generic* image classifier — for real
     * photos it very often returns broad categories ("Fruit", "Produce",
     * "Baked goods") rather than the exact dish. When no item's specific
     * mlLabels match, we fall back to a curated set of common items for
     * that category so the user still gets useful quick-pick suggestions
     * instead of nothing.
     */
    private val categoryFallback: List<Pair<String, List<String>>> = listOf(
        "citrus" to listOf("orange", "lemon", "grapefruit"),
        "fruit" to listOf("banana", "apple", "orange", "grape", "strawberry", "mango", "pear", "kiwi", "watermelon"),
        "vegetable" to listOf("tomato", "cucumber", "potato_boiled", "carrot", "broccoli", "cabbage", "onion", "bell_pepper"),
        "produce" to listOf("banana", "apple", "tomato", "cucumber", "carrot"),
        "natural foods" to listOf("apple", "banana", "tomato", "cucumber", "carrot"),
        "whole food" to listOf("apple", "banana", "carrot", "broccoli"),
        "superfood" to listOf("blueberry", "avocado", "spinach", "walnuts"),
        "baked goods" to listOf("bread_white", "bread_rye", "croissant", "bagel", "muffin"),
        "bread" to listOf("bread_white", "bread_rye", "bagel"),
        "pastry" to listOf("croissant", "eclair", "profiteroles", "strudel", "cake_napoleon"),
        "dessert" to listOf("cake", "cheesecake", "ice_cream", "donut", "tiramisu", "cake_chocolate"),
        "confectionery" to listOf("chocolate_bar", "candy", "cookie"),
        "sweetness" to listOf("chocolate_bar", "cake", "candy", "donut"),
        "junk food" to listOf("chips", "popcorn", "candy", "chocolate_bar"),
        "fast food" to listOf("hamburger", "pizza", "hot_dog", "french_fries", "chicken_nuggets"),
        "finger food" to listOf("chicken_wings", "french_fries", "sandwich"),
        "comfort food" to listOf("pizza", "hamburger", "pasta_boiled", "borscht"),
        "meat" to listOf("chicken_breast", "beef_steak", "pork_chop"),
        "seafood" to listOf("salmon", "sushi"),
        "cheese" to listOf("cheese_hard", "cottage_cheese", "cheesecake"),
        "dairy" to listOf("milk", "yogurt", "kefir", "cottage_cheese"),
        "salad" to listOf("salad_vegetable", "olivier_salad"),
        "soup" to listOf("borscht", "chicken_soup", "tom_yum", "kapusnyak"),
        "noodle" to listOf("pasta_boiled", "spaghetti_bolognese", "pho"),
        "pasta" to listOf("pasta_boiled", "pasta_carbonara", "spaghetti_bolognese", "lasagna"),
        "rice" to listOf("rice_boiled", "pilaf", "sushi"),
        "breakfast" to listOf("omelet", "oatmeal", "pancake", "granola"),
        "vegan nutrition" to listOf("salad_vegetable", "tomato", "cucumber", "hummus", "falafel"),
        // last-resort, very generic labels — only used if nothing else matched at all
        "food" to listOf("pizza", "borscht", "salad_vegetable", "chicken_breast"),
        "dish" to listOf("pizza", "borscht", "salad_vegetable", "chicken_breast"),
        "cuisine" to listOf("pizza", "borscht", "salad_vegetable", "chicken_breast")
    )

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
     * database's specific mlLabels aliases only. Returns matched items in the
     * same order as the input labels, de-duplicated, best guesses first.
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

    /**
     * Best-effort suggestions for a photo: specific matches first, then
     * (if there aren't enough) broad category fallbacks so the user always
     * has quick-pick chips instead of an empty screen.
     */
    fun suggestForLabels(context: Context, labels: List<String>, limit: Int = 6): List<NutritionItem> {
        val all = items(context)
        val result = LinkedHashSet<NutritionItem>()
        result.addAll(matchLabels(context, labels))

        if (result.size < limit) {
            for (label in labels) {
                val lbl = label.trim().lowercase()
                for ((keyword, ids) in categoryFallback) {
                    if (lbl.contains(keyword)) {
                        ids.forEach { id ->
                            all.firstOrNull { it.id == id }?.let { result.add(it) }
                        }
                    }
                    if (result.size >= limit) break
                }
                if (result.size >= limit) break
            }
        }
        return result.take(limit)
    }
}
