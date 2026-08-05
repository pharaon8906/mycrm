package com.caloriecam.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OffResponse(
    val status: Int = 0,
    val product: OffProduct? = null
)

@Serializable
data class OffProduct(
    @SerialName("product_name") val productName: String? = null,
    val brands: String? = null,
    @SerialName("ingredients_text") val ingredientsText: String? = null,
    @SerialName("nutriscore_grade") val nutriscoreGrade: String? = null,
    @SerialName("nova_group") val novaGroup: Int? = null,
    @SerialName("additives_tags") val additivesTags: List<String> = emptyList(),
    @SerialName("image_front_small_url") val imageUrl: String? = null,
    val nutriments: OffNutriments? = null
) {
    /** "en:e330" -> "E330" — OFF prefixes additive tags with a language code. */
    val additiveCodes: List<String>
        get() = additivesTags.map { it.substringAfter(':').uppercase() }
}

@Serializable
data class OffNutriments(
    @SerialName("energy-kcal_100g") val energyKcal100g: Double? = null,
    @SerialName("proteins_100g") val proteins100g: Double? = null,
    @SerialName("fat_100g") val fat100g: Double? = null,
    @SerialName("carbohydrates_100g") val carbohydrates100g: Double? = null,
    @SerialName("sugars_100g") val sugars100g: Double? = null,
    @SerialName("salt_100g") val salt100g: Double? = null
)

enum class ProductVerdictLevel { GOOD, NEUTRAL, CAUTION }

data class ProductVerdict(val level: ProductVerdictLevel, val label: String, val explanation: String)

/**
 * Best-effort verdict from Nutri-Score (nutritional quality) and NOVA group
 * (degree of processing) — the two standardized signals Open Food Facts
 * provides. Neither field is guaranteed to be present for every product.
 */
fun OffProduct.verdict(): ProductVerdict {
    val nutri = nutriscoreGrade?.lowercase()
    val nova = novaGroup

    val bad = nutri in setOf("d", "e") || nova == 4
    val good = nutri in setOf("a", "b") && (nova == null || nova <= 2)

    return when {
        nutri == null && nova == null -> ProductVerdict(
            ProductVerdictLevel.NEUTRAL,
            "Недостатньо даних",
            "Open Food Facts не має Nutri-Score чи рівня обробки для цього товару."
        )
        bad -> ProductVerdict(
            ProductVerdictLevel.CAUTION,
            "Краще обмежити",
            "Низький Nutri-Score та/або високий ступінь обробки (NOVA 4 — ультра-оброблений продукт)."
        )
        good -> ProductVerdict(
            ProductVerdictLevel.GOOD,
            "Гарний вибір",
            "Високий Nutri-Score і мінімальна обробка."
        )
        else -> ProductVerdict(
            ProductVerdictLevel.NEUTRAL,
            "Помірно корисний",
            "Не найкращий, але і не найгірший варіант — вживайте помірно."
        )
    }
}

fun novaDescription(group: Int?): String = when (group) {
    1 -> "Необроблені / мінімально оброблені продукти"
    2 -> "Оброблені кулінарні інгредієнти"
    3 -> "Оброблені продукти"
    4 -> "Ультра-оброблені продукти"
    else -> "Невідомо"
}
