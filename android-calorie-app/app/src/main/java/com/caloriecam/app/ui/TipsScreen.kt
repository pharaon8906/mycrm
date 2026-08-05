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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caloriecam.app.data.DailyMenu
import com.caloriecam.app.data.FoodTip
import com.caloriecam.app.data.Goal
import com.caloriecam.app.data.MealSlot
import com.caloriecam.app.data.TipSection
import com.caloriecam.app.data.TipsContent
import com.caloriecam.app.data.UserProfile
import com.caloriecam.app.data.WorkoutPlan

private val TAB_TITLES = listOf("Поради", "Меню", "Вправи")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipsScreen(
    profile: UserProfile?,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Поради та вправи") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(TAB_TITLES[0]) },
                    icon = { Icon(Icons.Default.Lightbulb, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(TAB_TITLES[1]) },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(TAB_TITLES[2]) },
                    icon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) }
                )
            }

            when (selectedTab) {
                0 -> AdviceTab(profile)
                1 -> MenuTab(profile)
                else -> WorkoutsTab()
            }
        }
    }
}

@Composable
private fun EmojiAvatar(emoji: String, background: Color, size: androidx.compose.ui.unit.Dp = 44.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(background, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = (size.value * 0.5f).sp)
    }
}

// ---------- Advice tab ----------

@Composable
private fun AdviceTab(profile: UserProfile?) {
    val sections = TipsContent.allTipSections
    val defaultIndex = when (profile?.goal) {
        Goal.LOSE -> 0
        Goal.GAIN -> 1
        else -> 2
    }
    var selectedIndex by remember { mutableStateOf(defaultIndex) }
    val chipLabels = listOf("Схуднення", "Набір маси", "Загальні")

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            sections.forEachIndexed { index, section ->
                FilterChip(
                    selected = selectedIndex == index,
                    onClick = { selectedIndex = index },
                    label = { Text("${section.emoji} ${chipLabels[index]}") }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        AdviceCard(sections[selectedIndex])

        if (selectedIndex == 0) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Рекомендовані продукти для схуднення", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            FoodTipGrid(TipsContent.weightLossFoods)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AdviceCard(section: TipSection) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiAvatar(section.emoji, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.width(12.dp))
                Text(section.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(14.dp))
            section.tips.forEach { tip ->
                Row(modifier = Modifier.padding(bottom = 10.dp)) {
                    Text("•  ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(tip, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun FoodTipGrid(foods: List<FoodTip>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.height(((foods.size + 1) / 2 * 96).dp)
    ) {
        items(foods) { food ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    EmojiAvatar(food.emoji, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), size = 40.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(food.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            food.reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ---------- Menu tab ----------

@Composable
private fun MenuTab(profile: UserProfile?) {
    var selectedGoal by remember { mutableStateOf(profile?.goal ?: Goal.MAINTAIN) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Goal.entries.forEach { g ->
                FilterChip(
                    selected = selectedGoal == g,
                    onClick = { selectedGoal = g },
                    label = { Text(g.label) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        val menu = TipsContent.dailyMenus.first { it.goal == selectedGoal }
        MenuTotalCard(menu)
        Spacer(modifier = Modifier.height(16.dp))
        menu.meals.forEach { slot ->
            MealCard(slot)
            Spacer(modifier = Modifier.height(10.dp))
        }
        Text(
            "Орієнтовне меню — коригуйте порції під свою денну норму з профілю.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun MenuTotalCard(menu: DailyMenu) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("ПРИБЛИЗНО ЗА ДЕНЬ", color = Color.White.copy(alpha = 0.75f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "≈ ${menu.totalKcal} ккал",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
private fun MealCard(slot: MealSlot) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(slot.title, fontWeight = FontWeight.Bold)
                Text(
                    slot.time,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            slot.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EmojiAvatar(item.emoji, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f), size = 34.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(item.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${item.kcal} ккал",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// ---------- Workouts tab ----------

@Composable
private fun WorkoutsTab() {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        TipsContent.workoutPlans.forEach { plan -> WorkoutCard(plan) }
    }
}

@Composable
private fun WorkoutCard(plan: WorkoutPlan) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiAvatar(plan.emoji, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(plan.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        plan.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            plan.exercises.forEach { exercise ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(exercise.name, fontWeight = FontWeight.Medium)
                    Text(
                        exercise.detail,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
