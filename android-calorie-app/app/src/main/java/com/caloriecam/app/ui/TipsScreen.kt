package com.caloriecam.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caloriecam.app.data.DayMenu
import com.caloriecam.app.data.FoodTip
import com.caloriecam.app.data.Goal
import com.caloriecam.app.data.MealSlot
import com.caloriecam.app.data.TipSection
import com.caloriecam.app.data.TipsContent
import com.caloriecam.app.data.UserProfile
import com.caloriecam.app.data.WorkoutPlan
import java.time.LocalDate

private val TAB_TITLES = listOf("Поради", "Меню", "Вправи")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipsScreen(
    profile: UserProfile?
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Поради та вправи") })
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

// ---------- Shared building blocks ----------

@Composable
private fun EmojiAvatar(emoji: String, background: Color, size: Dp = 44.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(background, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = (size.value * 0.5f).sp)
    }
}

/** Card with a colored accent bar on the left — gives sections a clear visual identity. */
@Composable
private fun AccentCard(
    accentColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(5.dp)
                    .background(accentColor)
            )
            Column(modifier = Modifier.padding(16.dp)) { content() }
        }
    }
}

@Composable
private fun ScrollableChipRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) { content() }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary
    )
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
    val accentColors = listOf(
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SectionLabel("Оберіть категорію")
        Spacer(modifier = Modifier.height(8.dp))
        ScrollableChipRow {
            sections.forEachIndexed { index, section ->
                FilterChip(
                    selected = selectedIndex == index,
                    onClick = { selectedIndex = index },
                    label = { Text("${section.emoji} ${chipLabels[index]}") }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        AdviceCard(sections[selectedIndex], accentColors[selectedIndex])

        if (selectedIndex == 0) {
            Spacer(modifier = Modifier.height(28.dp))
            SectionLabel("Рекомендовані продукти для схуднення")
            Spacer(modifier = Modifier.height(12.dp))
            FoodTipGrid(TipsContent.weightLossFoods)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AdviceCard(section: TipSection, accentColor: Color) {
    AccentCard(accentColor = accentColor) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EmojiAvatar(section.emoji, accentColor.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.width(12.dp))
            Text(section.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.height(14.dp))
        section.tips.forEach { tip ->
            Row(modifier = Modifier.padding(bottom = 12.dp)) {
                Text("●", fontWeight = FontWeight.Bold, color = accentColor, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(8.dp))
                Text(tip, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
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
        modifier = Modifier.height(((foods.size + 1) / 2 * 132).dp)
    ) {
        items(foods) { food ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    EmojiAvatar(food.emoji, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), size = 40.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(food.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
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

// ---------- Menu tab ----------

private fun todayIndex(): Int = (LocalDate.now().dayOfWeek.value - 1).coerceIn(0, 6)

@Composable
private fun MenuTab(profile: UserProfile?) {
    var selectedGoal by remember { mutableStateOf(profile?.goal ?: Goal.MAINTAIN) }
    var selectedDay by remember { mutableIntStateOf(todayIndex()) }

    val week = TipsContent.weekMenus.first { it.goal == selectedGoal }
    val day = week.days[selectedDay]

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SectionLabel("Ціль")
        Spacer(modifier = Modifier.height(8.dp))
        ScrollableChipRow {
            Goal.entries.forEach { g ->
                FilterChip(
                    selected = selectedGoal == g,
                    onClick = { selectedGoal = g; selectedDay = todayIndex() },
                    label = { Text(g.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        SectionLabel("День тижня")
        Spacer(modifier = Modifier.height(8.dp))
        val shortDayLabels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд")
        ScrollableChipRow {
            week.days.forEachIndexed { index, _ ->
                FilterChip(
                    selected = selectedDay == index,
                    onClick = { selectedDay = index },
                    label = { Text(shortDayLabels[index]) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(day.dayLabel, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(10.dp))
        MenuTotalCard(day)
        Spacer(modifier = Modifier.height(16.dp))
        day.meals.forEach { slot ->
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
private fun MenuTotalCard(day: DayMenu) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("ПРИБЛИЗНО ЗА ДЕНЬ", color = Color.White.copy(alpha = 0.75f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "≈ ${day.totalKcal} ккал",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
private fun MealCard(slot: MealSlot) {
    AccentCard(accentColor = MaterialTheme.colorScheme.tertiary) {
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
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "${item.kcal} ккал",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
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
        TipsContent.workoutPlans.forEach { plan ->
            WorkoutCard(plan)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun WorkoutCard(plan: WorkoutPlan) {
    AccentCard(accentColor = MaterialTheme.colorScheme.primary) {
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
                    .padding(vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(exercise.name, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    exercise.detail,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
