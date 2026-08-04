package com.caloriecam.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.caloriecam.app.data.ActivityLevel
import com.caloriecam.app.data.Gender
import com.caloriecam.app.data.Goal
import com.caloriecam.app.data.UserProfile
import com.caloriecam.app.data.UserProfileRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    initial: UserProfile?,
    onDone: () -> Unit,
    onCancel: () -> Unit
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

    val previewGoal = if (valid) {
        UserProfile(gender, age!!, height!!, weight!!, activity, goal).dailyCalorieGoal
    } else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мій профіль") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
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
            Text("Стать", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Gender.entries.forEach { g ->
                    FilterChip(
                        selected = gender == g,
                        onClick = { gender = g },
                        label = { Text(g.label) }
                    )
                }
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
