package com.caloriecam.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.caloriecam.app.data.FoodLogEntry
import java.io.File
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val entries by viewModel.entries.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val profile by viewModel.profile.collectAsState()
    var showSheet by remember { mutableStateOf(false) }

    val totalKcal = entries.sumOf { it.kcal }
    val goalKcal = profile?.dailyCalorieGoal ?: 2000

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CalorieCam", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.PersonOutline, contentDescription = "Профіль")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Додати їжу")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            DateSelector(
                date = selectedDate,
                onPrevious = viewModel::previousDay,
                onNext = viewModel::nextDay
            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CalorieRing(consumed = totalKcal, goal = goalKcal, modifier = Modifier.padding(vertical = 12.dp))
                    if (profile == null) {
                        TextButton(onClick = onProfileClick) {
                            Text("Налаштувати профіль для точної норми")
                        }
                    }
                }
            }

            if (entries.isNotEmpty()) {
                MacroRow(
                    protein = entries.sumOf { it.protein }.roundToInt(),
                    fat = entries.sumOf { it.fat }.roundToInt(),
                    carbs = entries.sumOf { it.carbs }.roundToInt()
                )
            }

            if (entries.isEmpty()) {
                EmptyState()
            } else {
                Text(
                    text = "Записи",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 4.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        FoodEntryCard(entry = entry, onDelete = { viewModel.delete(entry) })
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    item { Spacer(modifier = Modifier.height(88.dp)) }
                }
            }
        }
    }

    if (showSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(onDismissRequest = { showSheet = false }, sheetState = sheetState) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                ListItem(
                    headlineContent = { Text("Зробити фото") },
                    leadingContent = { Icon(Icons.Default.PhotoCamera, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showSheet = false
                        onCameraClick()
                    }
                )
                ListItem(
                    headlineContent = { Text("Обрати з галереї") },
                    leadingContent = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showSheet = false
                        onGalleryClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun DateSelector(date: LocalDate, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Попередній день")
        }
        Text(text = date.formatUk(), fontWeight = FontWeight.SemiBold)
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ArrowForwardIos, contentDescription = "Наступний день")
        }
    }
}

private fun LocalDate.formatUk(): String {
    val today = LocalDate.now()
    return when (this) {
        today -> "Сьогодні"
        today.minusDays(1) -> "Вчора"
        today.plusDays(1) -> "Завтра"
        else -> {
            val month = month.getDisplayName(TextStyle.FULL, Locale("uk"))
            "$dayOfMonth $month"
        }
    }
}

@Composable
private fun MacroRow(protein: Int, fat: Int, carbs: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MacroStat(label = "Білки", value = protein, color = MaterialTheme.colorScheme.primary)
        MacroStat(label = "Жири", value = fat, color = MaterialTheme.colorScheme.tertiary)
        MacroStat(label = "Вуглеводи", value = carbs, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun MacroStat(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("${value} г", fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.RestaurantMenu,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Ще немає записів за цей день", color = Color.Gray)
            Text("Натисніть + та сфотографуйте їжу", color = Color.Gray)
        }
    }
}

@Composable
private fun FoodEntryCard(entry: FoodLogEntry, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val path = entry.photoPath
            if (path != null) {
                AsyncImage(
                    model = File(path),
                    contentDescription = entry.foodName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.foodName, fontWeight = FontWeight.SemiBold)
                Text(
                    "${entry.grams} г",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("${entry.kcal} ккал", fontWeight = FontWeight.Bold)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Видалити")
            }
        }
    }
}
