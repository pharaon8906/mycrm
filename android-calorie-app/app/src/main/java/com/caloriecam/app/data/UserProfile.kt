package com.caloriecam.app.data

import kotlin.math.roundToInt

enum class Gender(val label: String) {
    MALE("Чоловіча"),
    FEMALE("Жіноча")
}

enum class ActivityLevel(val multiplier: Double, val label: String) {
    SEDENTARY(1.2, "Малорухливий спосіб життя"),
    LIGHT(1.375, "Легка активність (1–3 р/тиж)"),
    MODERATE(1.55, "Помірна активність (3–5 р/тиж)"),
    ACTIVE(1.725, "Висока активність (6–7 р/тиж)"),
    VERY_ACTIVE(1.9, "Дуже висока активність (спорт щодня)")
}

enum class Goal(val factor: Double, val label: String) {
    LOSE(0.8, "Схуднення"),
    MAINTAIN(1.0, "Підтримка ваги"),
    GAIN(1.15, "Набір маси")
}

/**
 * Daily kcal norm via Mifflin-St Jeor BMR, scaled by activity level and goal.
 */
data class UserProfile(
    val gender: Gender = Gender.MALE,
    val age: Int = 25,
    val heightCm: Int = 175,
    val weightKg: Double = 70.0,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val goal: Goal = Goal.MAINTAIN
) {
    val bmr: Double
        get() = when (gender) {
            Gender.MALE -> 10 * weightKg + 6.25 * heightCm - 5 * age + 5
            Gender.FEMALE -> 10 * weightKg + 6.25 * heightCm - 5 * age - 161
        }

    val dailyCalorieGoal: Int
        get() = (bmr * activityLevel.multiplier * goal.factor).roundToInt()
}
