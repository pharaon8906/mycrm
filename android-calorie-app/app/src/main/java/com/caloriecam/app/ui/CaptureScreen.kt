package com.caloriecam.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.caloriecam.app.data.AppDatabase
import com.caloriecam.app.data.FoodLogEntry
import com.caloriecam.app.data.NutritionItem
import com.caloriecam.app.data.NutritionRepository
import com.caloriecam.app.ml.FoodLabeler
import com.caloriecam.app.util.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    photoFile: File,
    onDone: () -> Unit,
    onDiscard: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var recognizing by remember { mutableStateOf(true) }
    var suggestions by remember { mutableStateOf<List<NutritionItem>>(emptyList()) }
    var selectedItem by remember { mutableStateOf<NutritionItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var gramsText by remember { mutableStateOf("150") }
    var manualMode by remember { mutableStateOf(false) }
    var manualName by remember { mutableStateOf("") }
    var manualKcal100Text by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(photoFile) {
        recognizing = true
        val bitmap = withContext(Dispatchers.IO) { BitmapUtils.decodeSampled(photoFile) }
        if (bitmap != null) {
            val labels = FoodLabeler().label(bitmap)
            val labelTexts = labels.sortedByDescending { it.confidence }.map { it.text }
            val matches = NutritionRepository.matchLabels(context, labelTexts)
            suggestions = matches.take(4)
            val best = matches.firstOrNull()
            if (best != null) {
                selectedItem = best
                gramsText = best.defaultGrams.toString()
            }
        }
        recognizing = false
    }

    val grams = gramsText.toIntOrNull() ?: 0
    val manualKcal100 = manualKcal100Text.toDoubleOrNull()
    val kcal100 = if (manualMode) manualKcal100 else selectedItem?.kcal100
    val foodName = if (manualMode) manualName else (selectedItem?.nameUk ?: "")
    val computedKcal = if (kcal100 != null && grams > 0) (kcal100 * grams / 100.0).roundToInt() else null
    val canSave = foodName.isNotBlank() && kcal100 != null && kcal100 > 0 && grams > 0 && !saving

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новий запис") },
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
                contentDescription = "Фото їжі",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (recognizing) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Розпізнаю страву на фото…")
                }
            } else if (suggestions.isNotEmpty()) {
                Text("Схоже на:", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    suggestions.forEach { item ->
                        FilterChip(
                            selected = selectedItem?.id == item.id && !manualMode,
                            onClick = {
                                selectedItem = item
                                manualMode = false
                                gramsText = item.defaultGrams.toString()
                            },
                            label = { Text(item.nameUk) }
                        )
                    }
                }
            } else {
                Text("Не вдалося розпізнати автоматично — оберіть страву нижче")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Пошук страви", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Наприклад: борщ, банан, піца…") },
                singleLine = true
            )

            if (searchQuery.isNotBlank()) {
                val results = NutritionRepository.search(context, searchQuery)
                LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                    items(results, key = { it.id }) { item ->
                        ListItem(
                            headlineContent = { Text(item.nameUk) },
                            supportingContent = { Text("${item.kcal100.roundToInt()} ккал / 100 г") },
                            modifier = Modifier.clickable {
                                selectedItem = item
                                manualMode = false
                                gramsText = item.defaultGrams.toString()
                                searchQuery = ""
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = { manualMode = !manualMode }) {
                Text(if (manualMode) "Скасувати ручне введення" else "Ввести вручну")
            }

            if (manualMode) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = manualName,
                    onValueChange = { manualName = it },
                    label = { Text("Назва страви") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = manualKcal100Text,
                    onValueChange = { manualKcal100Text = it },
                    label = { Text("Калорій на 100 г") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Обрана страва: ${foodName.ifBlank { "—" }}", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Порція: $grams г")
            Slider(
                value = grams.coerceIn(10, 1000).toFloat(),
                onValueChange = { gramsText = it.roundToInt().toString() },
                valueRange = 10f..1000f
            )
            OutlinedTextField(
                value = gramsText,
                onValueChange = { gramsText = it },
                label = { Text("Грамів") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = computedKcal?.let { "≈ $it ккал" } ?: "Оберіть страву та порцію",
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDiscard) { Text("Скасувати") }
                Button(
                    enabled = canSave,
                    onClick = {
                        val kcal = computedKcal ?: return@Button
                        val ratio = grams / 100.0
                        val protein = if (manualMode) 0.0 else (selectedItem?.protein100 ?: 0.0) * ratio
                        val fat = if (manualMode) 0.0 else (selectedItem?.fat100 ?: 0.0) * ratio
                        val carbs = if (manualMode) 0.0 else (selectedItem?.carbs100 ?: 0.0) * ratio
                        saving = true
                        scope.launch {
                            val dao = AppDatabase.getInstance(context).foodLogDao()
                            dao.insert(
                                FoodLogEntry(
                                    photoPath = photoFile.absolutePath,
                                    foodName = foodName,
                                    grams = grams,
                                    kcal = kcal,
                                    protein = protein,
                                    fat = fat,
                                    carbs = carbs,
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                            saving = false
                            onDone()
                        }
                    }
                ) {
                    Text(if (saving) "Збереження…" else "Зберегти")
                }
            }
        }
    }
}
