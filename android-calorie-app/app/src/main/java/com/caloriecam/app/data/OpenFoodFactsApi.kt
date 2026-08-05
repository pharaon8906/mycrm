package com.caloriecam.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

/**
 * Thin client for the free, open Open Food Facts product database
 * (https://openfoodfacts.org) — no API key needed. Strong coverage for
 * European/international branded products; weaker for local Ukrainian
 * goods without a barcode entry yet.
 */
object OpenFoodFactsApi {

    private val json = Json { ignoreUnknownKeys = true }
    private val barcodePattern = Regex("^[0-9]{6,14}$")

    sealed class Result {
        data class Found(val product: OffProduct) : Result()
        object NotFound : Result()
        object InvalidBarcode : Result()
        data class NetworkError(val message: String) : Result()
    }

    suspend fun lookupByBarcode(barcode: String): Result {
        if (!barcodePattern.matches(barcode)) {
            return Result.InvalidBarcode
        }
        return withContext(Dispatchers.IO) {
            fetch(barcode)
        }
    }

    private fun fetch(barcode: String): Result {
        val outcome = runCatching {
            val url = URL("https://world.openfoodfacts.org/api/v2/product/$barcode.json")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("User-Agent", "CalorieCam-Android - Open Food Facts client")
            }
            val responseCode: Int
            val body: String
            try {
                responseCode = connection.responseCode
                body = if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    ""
                }
            } finally {
                connection.disconnect()
            }
            responseCode to body
        }

        val (responseCode, body) = outcome.getOrElse { e ->
            return Result.NetworkError(e.message ?: "Невідома помилка мережі")
        }

        if (responseCode != HttpURLConnection.HTTP_OK) {
            return Result.NetworkError("HTTP $responseCode")
        }

        val response = runCatching { json.decodeFromString<OffResponse>(body) }
            .getOrElse { e -> return Result.NetworkError(e.message ?: "Не вдалося розібрати відповідь") }

        return if (response.status == 1 && response.product != null) {
            Result.Found(response.product)
        } else {
            Result.NotFound
        }
    }
}
