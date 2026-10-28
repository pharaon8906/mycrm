package com.caloriecam.app.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.caloriecam.app.data.DailyTotal
import com.caloriecam.app.data.ProgressPhoto
import com.caloriecam.app.data.WeightEntry
import com.caloriecam.app.util.PhotoStore
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = viewModel()
) {
    val rangeDays by viewModel.rangeDays.collectAsState()
    val dailyTotals by viewModel.dailyTotals.collectAsState()
    val weightEntries by viewModel.weightEntries.collectAsState()
    var showAddWeight by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Прогрес") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Калорії", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = rangeDays == 7, onClick = { viewModel.setRange(7) }, label = { Text("Тиждень") })
                FilterChip(selected = rangeDays == 30, onClick = { viewModel.setRange(30) }, label = { Text("Місяць") })
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (dailyTotals.isEmpty()) {
                Text("Ще немає даних за цей період — почніть додавати записи їжі", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                CalorieBarChart(dailyTotals, modifier = Modifier.fillMaxWidth().height(180.dp))
                Spacer(modifier = Modifier.height(6.dp))
                val avg = dailyTotals.map { it.total }.average().toInt()
                Text(
                    "Середньо: $avg ккал/день",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Вага тіла", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = { showAddWeight = true }) {
                    Text("+ Додати")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (weightEntries.isEmpty()) {
                Text("Ще немає записів ваги", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                WeightLineChart(weightEntries, modifier = Modifier.fillMaxWidth().height(180.dp))
                Spacer(modifier = Modifier.height(6.dp))
                val first = weightEntries.first().weightKg
                val last = weightEntries.last().weightKg
                val diff = last - first
                val sign = if (diff >= 0) "+" else ""
                Text(
                    "Поточна вага: ${last} кг · зміна за період: $sign${String.format(Locale.US, "%.1f", diff)} кг",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text("Фото прогресу", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Фотографуйте себе раз на тиждень в однакових умовах — так найкраще видно зміни.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            ProgressPhotosSection(viewModel)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showAddWeight) {
        var text by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddWeight = false },
            title = { Text("Нова вага") },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Вага (кг)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    text.toDoubleOrNull()?.let { viewModel.addWeight(it) }
                    showAddWeight = false
                }) { Text("Зберегти") }
            },
            dismissButton = {
                TextButton(onClick = { showAddWeight = false }) { Text("Скасувати") }
            }
        )
    }
}

@Composable
private fun CalorieBarChart(data: List<DailyTotal>, modifier: Modifier = Modifier) {
    val maxVal = (data.maxOfOrNull { it.total } ?: 0).coerceAtLeast(1)
    val barColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val barCount = data.size
        val spacing = 6.dp.toPx()
        val barWidth = ((size.width - spacing * (barCount + 1)) / barCount).coerceAtLeast(2f)
        data.forEachIndexed { index, entry ->
            val barHeight = (entry.total.toFloat() / maxVal) * size.height
            val x = spacing + index * (barWidth + spacing)
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}

@Composable
private fun WeightLineChart(data: List<WeightEntry>, modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.primary
    val minW = data.minOf { it.weightKg }
    val maxW = data.maxOf { it.weightKg }
    val range = (maxW - minW).takeIf { it > 0.01 } ?: 1.0

    Canvas(modifier = modifier) {
        if (data.size < 2) {
            drawCircle(color = lineColor, radius = 6.dp.toPx(), center = Offset(size.width / 2f, size.height / 2f))
            return@Canvas
        }
        val stepX = size.width / (data.size - 1)
        val points = data.mapIndexed { index, entry ->
            val x = index * stepX
            val normalized = ((entry.weightKg - minW) / range).toFloat()
            val y = size.height - normalized * size.height
            Offset(x, y)
        }
        for (i in 0 until points.size - 1) {
            drawLine(
                color = lineColor,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        points.forEach { p -> drawCircle(color = lineColor, radius = 5.dp.toPx(), center = p) }
    }
}

// ---------- Progress photos ----------

private val PHOTO_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM")

private fun formatPhotoDate(timestamp: Long): String =
    Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).format(PHOTO_DATE_FORMAT)

@Composable
private fun ProgressPhotosSection(viewModel: StatsViewModel) {
    val context = LocalContext.current
    val photos by viewModel.progressPhotos.collectAsState()
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }
    var previewPhoto by remember { mutableStateOf<ProgressPhoto?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val file = pendingCameraFile
        if (success && file != null) {
            viewModel.addProgressPhoto(file)
        } else {
            file?.let { PhotoStore.delete(it) }
        }
        pendingCameraFile = null
    }

    fun launchCamera() {
        val file = PhotoStore.newCameraFile(context, prefix = "PROGRESS")
        pendingCameraFile = file
        takePictureLauncher.launch(PhotoStore.contentUriFor(context, file))
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) launchCamera() }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val file = PhotoStore.copyToInternal(context, uri, prefix = "PROGRESS")
            viewModel.addProgressPhoto(file)
        }
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            AddPhotoTile(
                onCameraClick = {
                    val granted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (granted) launchCamera() else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
                onGalleryClick = {
                    pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            )
        }
        items(photos, key = { it.id }) { photo ->
            ProgressPhotoTile(photo, onClick = { previewPhoto = photo })
        }
    }

    val photo = previewPhoto
    if (photo != null) {
        Dialog(onDismissRequest = { previewPhoto = null }) {
            Card {
                Column {
                    AsyncImage(
                        model = File(photo.photoPath),
                        contentDescription = "Фото прогресу",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            formatPhotoDate(photo.timestamp),
                            modifier = Modifier.padding(start = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row {
                            TextButton(onClick = { previewPhoto = null }) { Text("Закрити") }
                            TextButton(onClick = {
                                viewModel.deleteProgressPhoto(photo)
                                previewPhoto = null
                            }) { Text("Видалити", color = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddPhotoTile(onCameraClick: () -> Unit, onGalleryClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Card(
            modifier = Modifier.size(90.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            onClick = { expanded = true }
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.AddAPhoto, contentDescription = "Додати фото прогресу")
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Камера") }, onClick = { expanded = false; onCameraClick() })
            DropdownMenuItem(text = { Text("Галерея") }, onClick = { expanded = false; onGalleryClick() })
        }
    }
}

@Composable
private fun ProgressPhotoTile(photo: ProgressPhoto, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(
            model = File(photo.photoPath),
            contentDescription = "Фото прогресу",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            formatPhotoDate(photo.timestamp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
