package com.caloriecam.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.caloriecam.app.data.OffProduct
import com.caloriecam.app.data.OpenFoodFactsApi
import com.caloriecam.app.data.ProductVerdictLevel
import com.caloriecam.app.data.novaDescription
import com.caloriecam.app.data.verdict
import com.caloriecam.app.ml.BarcodeReader
import com.caloriecam.app.util.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private sealed class ScanState {
    object Scanning : ScanState()
    object LookingUp : ScanState()
    object NoBarcode : ScanState()
    data class NotFound(val code: String) : ScanState()
    data class Error(val message: String) : ScanState()
    data class Found(val code: String, val product: OffProduct) : ScanState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScanScreen(
    photoFile: File,
    onDone: () -> Unit,
    onDiscard: () -> Unit
) {
    var state by remember { mutableStateOf<ScanState>(ScanState.Scanning) }

    LaunchedEffect(photoFile) {
        state = ScanState.Scanning
        val bitmap = withContext(Dispatchers.IO) { BitmapUtils.decodeSampled(photoFile) }
        if (bitmap == null) {
            state = ScanState.Error("Не вдалося обробити фото")
            return@LaunchedEffect
        }
        val barcodes = BarcodeReader().read(bitmap)
        val code = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue
        if (code == null) {
            state = ScanState.NoBarcode
            return@LaunchedEffect
        }
        state = ScanState.LookingUp
        state = when (val result = OpenFoodFactsApi.lookupByBarcode(code)) {
            is OpenFoodFactsApi.Result.Found -> ScanState.Found(code, result.product)
            is OpenFoodFactsApi.Result.NotFound -> ScanState.NotFound(code)
            is OpenFoodFactsApi.Result.InvalidBarcode -> ScanState.Error("Розпізнаний код не схожий на штрихкод товару")
            is OpenFoodFactsApi.Result.NetworkError -> ScanState.Error(
                "Немає з'єднання або сервіс недоступний (${result.message}). Перевірте інтернет і спробуйте ще раз."
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Сканування штрихкоду") },
                navigationIcon = {
                    IconButton(onClick = onDiscard) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Скасувати")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            AsyncImage(
                model = photoFile,
                contentDescription = "Фото штрихкоду",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(modifier = Modifier.height(20.dp))

            when (val s = state) {
                is ScanState.Scanning -> LoadingRow("Шукаю штрихкод на фото…")
                is ScanState.LookingUp -> LoadingRow("Шукаю товар у базі Open Food Facts…")
                is ScanState.NoBarcode -> {
                    Text("Штрихкод не знайдено на фото.", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Спробуйте зняти ще раз: тримайте камеру рівно над штрихкодом, ближче й при доброму освітленні.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(onClick = onDiscard) { Text("Спробувати ще раз") }
                }
                is ScanState.Error -> {
                    Text("Помилка", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(s.message, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(onClick = onDiscard) { Text("Закрити") }
                }
                is ScanState.NotFound -> {
                    Text("Товар не знайдено", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Штрихкод ${s.code} відсутній у базі Open Food Facts. Це трапляється з локальними товарами, яких там ще немає.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(onClick = onDiscard) { Text("Закрити") }
                }
                is ScanState.Found -> {
                    ProductResultCard(s.product)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                        Text("Готово")
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text)
    }
}

@Composable
private fun ProductResultCard(product: OffProduct) {
    val verdict = product.verdict()
    val verdictColor = when (verdict.level) {
        ProductVerdictLevel.GOOD -> MaterialTheme.colorScheme.primary
        ProductVerdictLevel.NEUTRAL -> MaterialTheme.colorScheme.tertiary
        ProductVerdictLevel.CAUTION -> MaterialTheme.colorScheme.error
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (product.imageUrl != null) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.productName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column {
                Text(
                    product.productName?.takeIf { it.isNotBlank() } ?: "Без назви",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                if (!product.brands.isNullOrBlank()) {
                    Text(product.brands, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            product.nutriscoreGrade?.let { grade ->
                NutriScoreBadge(grade)
            }
            product.novaGroup?.let { nova ->
                NovaBadge(nova)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Card(colors = CardDefaults.cardColors(containerColor = verdictColor.copy(alpha = 0.12f))) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(verdict.label, fontWeight = FontWeight.Bold, color = verdictColor)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    verdict.explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        product.nutriments?.energyKcal100g?.let { kcal ->
            Spacer(modifier = Modifier.height(16.dp))
            Text("${kcal.toInt()} ккал / 100 г", fontWeight = FontWeight.Bold)
        }

        if (!product.ingredientsText.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Склад", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(product.ingredientsText, style = MaterialTheme.typography.bodySmall)
        }

        if (product.additiveCodes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Харчові добавки", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                product.additiveCodes.joinToString(", "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NutriScoreBadge(grade: String) {
    val letter = grade.uppercase()
    val color = when (letter) {
        "A" -> Color(0xFF1E8F4E)
        "B" -> Color(0xFF7AC142)
        "C" -> Color(0xFFF4C10F)
        "D" -> Color(0xFFEE8100)
        "E" -> Color(0xFFE63E11)
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(letter, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("Nutri-Score", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun NovaBadge(group: Int) {
    val color = when (group) {
        1 -> Color(0xFF1E8F4E)
        2 -> Color(0xFF7AC142)
        3 -> Color(0xFFEE8100)
        else -> Color(0xFFE63E11)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("$group", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("NOVA", style = MaterialTheme.typography.labelSmall)
    }
    // Description available via novaDescription(group) — surfaced as a tooltip
    // could be a future improvement; kept simple here.
}
