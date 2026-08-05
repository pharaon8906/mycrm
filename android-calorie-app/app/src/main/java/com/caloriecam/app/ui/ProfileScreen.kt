package com.caloriecam.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.caloriecam.app.data.ActivityLevel
import com.caloriecam.app.data.BackupManager
import com.caloriecam.app.data.BmiCategory
import com.caloriecam.app.data.Gender
import com.caloriecam.app.data.Goal
import com.caloriecam.app.data.UserProfile
import com.caloriecam.app.data.UserProfileRepository
import com.caloriecam.app.health.HealthConnectAvailability
import com.caloriecam.app.health.HealthConnectManager
import com.caloriecam.app.notify.ReminderScheduler
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    initial: UserProfile?,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var gender by remember { mutableStateOf(initial?.gender ?: Gender.MALE) }
    var ageText by remember { mutableStateOf((initial?.age ?: 25).toString()) }
    var heightText by remember { mutableStateOf((initial?.heightCm ?: 175).toString()) }
    var weightText by remember { mutableStateOf((initial?.weightKg ?: 70.0).toString()) }
    var activity by remember { mutableStateOf(initial?.activityLevel ?: ActivityLevel.MODERATE) }
    var goal by remember { mutableStateOf(initial?.goal ?: Goal.MAINTAIN) }
    var saving by remember { mutableStateOf(false) }

    val age = ageText.toIntOrNull()
    val height = heightText.toIntOrNull()
    val weight = weightText.toDoubleOrNull()
    val valid = age != null && age in 10..100 &&
        height != null && height in 100..250 &&
        weight != null && weight in 30.0..300.0

    val previewProfile = if (valid) {
        UserProfile(gender, age!!, height!!, weight!!, activity, goal)
    } else null
    val previewGoal = previewProfile?.dailyCalorieGoal

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Мій профіль") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Стать", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GenderOption(
                    icon = Icons.Default.Male,
                    label = Gender.MALE.label,
                    selected = gender == Gender.MALE,
                    onClick = { gender = Gender.MALE },
                    modifier = Modifier.weight(1f)
                )
                GenderOption(
                    icon = Icons.Default.Female,
                    label = Gender.FEMALE.label,
                    selected = gender == Gender.FEMALE,
                    onClick = { gender = Gender.FEMALE },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = ageText,
                onValueChange = { ageText = it },
                label = { Text("Вік (років)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = heightText,
                onValueChange = { heightText = it },
                label = { Text("Зріст (см)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it },
                label = { Text("Вага (кг)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text("Рівень активності", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                ActivityLevel.entries.forEach { level ->
                    ActivityRow(
                        label = level.label,
                        selected = activity == level,
                        onClick = { activity = level }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Ціль", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Goal.entries.forEach { g ->
                    FilterChip(
                        selected = goal == g,
                        onClick = { goal = g },
                        label = { Text(g.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("ВАША ДЕННА НОРМА", color = Color.White.copy(alpha = 0.75f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = previewGoal?.let { "$it ккал" } ?: "Заповніть поля коректно",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            if (previewProfile != null) {
                Spacer(modifier = Modifier.height(12.dp))
                BmiCard(previewProfile)
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                enabled = valid && !saving,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val profile = UserProfile(gender, age!!, height!!, weight!!, activity, goal)
                    saving = true
                    scope.launch {
                        UserProfileRepository.save(context, profile)
                        saving = false
                        onDone()
                    }
                }
            ) {
                Text(if (saving) "Збереження…" else "Зберегти")
            }

            Spacer(modifier = Modifier.height(28.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(20.dp))
            RemindersSection()

            Spacer(modifier = Modifier.height(28.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(20.dp))
            BackupSection()

            Spacer(modifier = Modifier.height(28.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(20.dp))
            HealthConnectSection()
        }
    }
}

@Composable
private fun HealthConnectSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val availability = remember { HealthConnectManager.availability(context) }
    var granted by remember { mutableStateOf(false) }

    LaunchedEffect(availability) {
        if (availability == HealthConnectAvailability.AVAILABLE) {
            granted = HealthConnectManager.hasAllPermissions(context)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        HealthConnectManager.permissionRequestContract()
    ) {
        scope.launch { granted = HealthConnectManager.hasAllPermissions(context) }
    }

    Text("Health Connect", fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        "Синхронізація кроків, спалених калорій і ваги з фітнес-трекерами (Google Fit, Samsung Health та іншими) через стандартний агрегатор Android.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(10.dp))
    when (availability) {
        HealthConnectAvailability.AVAILABLE -> {
            if (granted) {
                Text(
                    "✓ Підключено — кроки й спалені калорії показуються на головному екрані.",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                OutlinedButton(onClick = { permissionLauncher.launch(HealthConnectManager.permissions) }) {
                    Text("Підключити Health Connect")
                }
            }
        }
        HealthConnectAvailability.NOT_INSTALLED -> {
            Text(
                "Встановіть застосунок Health Connect з Google Play, щоб увімкнути синхронізацію.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HealthConnectAvailability.UNSUPPORTED -> {
            Text(
                "Health Connect недоступний на цьому пристрої.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BackupSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showImportConfirm by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val ok = BackupManager.export(context, uri)
                Toast.makeText(
                    context,
                    if (ok) "Дані експортовано" else "Не вдалося експортувати",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val importPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
            showImportConfirm = true
        }
    }

    Text("Резервне копіювання", fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        "Збережіть дані перед зміною телефону або відновіть їх на новому пристрої. Фото у файл бекапу не входять.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = { exportLauncher.launch("caloriecam_backup_${System.currentTimeMillis()}.json") }
        ) { Text("Експортувати") }
        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = { importPickerLauncher.launch(arrayOf("application/json")) }
        ) { Text("Імпортувати") }
    }

    if (showImportConfirm) {
        AlertDialog(
            onDismissRequest = { showImportConfirm = false },
            title = { Text("Імпортувати дані?") },
            text = {
                Text("Це замінить усі поточні записи їжі, ваги, води та профіль даними з обраного файлу. Дію не можна скасувати.")
            },
            confirmButton = {
                TextButton(onClick = {
                    val uri = pendingImportUri
                    showImportConfirm = false
                    if (uri != null) {
                        scope.launch {
                            val ok = BackupManager.import(context, uri)
                            Toast.makeText(
                                context,
                                if (ok) "Дані відновлено" else "Не вдалося імпортувати",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }) { Text("Замінити") }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirm = false }) { Text("Скасувати") }
            }
        )
    }
}

@Composable
private fun RemindersSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val remindersEnabled by UserProfileRepository.remindersEnabledFlow(context).collectAsState(initial = false)

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        scope.launch { UserProfileRepository.setRemindersEnabled(context, granted) }
        if (granted) ReminderScheduler.scheduleAll(context)
    }

    fun enableReminders() {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            scope.launch { UserProfileRepository.setRemindersEnabled(context, true) }
            ReminderScheduler.scheduleAll(context)
        }
    }

    Text("Нагадування", fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Нагадувати про прийоми їжі")
            Text(
                "Сніданок 8:00 · обід 13:00 · вечеря 19:00",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = remindersEnabled,
            onCheckedChange = { enabled ->
                if (enabled) {
                    enableReminders()
                } else {
                    scope.launch { UserProfileRepository.setRemindersEnabled(context, false) }
                    ReminderScheduler.cancelAll(context)
                }
            }
        )
    }
}

@Composable
private fun GenderOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
        ),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(label, color = contentColor, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
        }
    }
}

@Composable
private fun BmiCard(profile: UserProfile) {
    val category = profile.bmiCategory
    val accentColor = when (category) {
        BmiCategory.UNDERWEIGHT -> MaterialTheme.colorScheme.secondary
        BmiCategory.NORMAL -> MaterialTheme.colorScheme.primary
        BmiCategory.OVERWEIGHT -> MaterialTheme.colorScheme.tertiary
        BmiCategory.OBESE -> MaterialTheme.colorScheme.error
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "ІНДЕКС МАСИ ТІЛА (ІМТ)",
                    style = MaterialTheme.typography.labelMedium,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(category.label, fontWeight = FontWeight.Bold, color = accentColor)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    category.advice,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                String.format(Locale.US, "%.1f", profile.bmi),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium,
                color = accentColor
            )
        }
    }
}

@Composable
private fun ActivityRow(label: String, selected: Boolean, onClick: () -> Unit) {
    val container = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
        }
    }
}
