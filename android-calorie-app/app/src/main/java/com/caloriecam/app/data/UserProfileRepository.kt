package com.caloriecam.app.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userProfileStore by preferencesDataStore(name = "user_profile")

object UserProfileRepository {

    private object Keys {
        val GENDER = stringPreferencesKey("gender")
        val AGE = intPreferencesKey("age")
        val HEIGHT = intPreferencesKey("height_cm")
        val WEIGHT = doublePreferencesKey("weight_kg")
        val ACTIVITY = stringPreferencesKey("activity_level")
        val GOAL = stringPreferencesKey("goal")
    }

    /** Null until the user saves a profile for the first time. */
    fun profileFlow(context: Context): Flow<UserProfile?> =
        context.userProfileStore.data.map { prefs ->
            val genderName = prefs[Keys.GENDER] ?: return@map null
            UserProfile(
                gender = Gender.valueOf(genderName),
                age = prefs[Keys.AGE] ?: 25,
                heightCm = prefs[Keys.HEIGHT] ?: 175,
                weightKg = prefs[Keys.WEIGHT] ?: 70.0,
                activityLevel = prefs[Keys.ACTIVITY]?.let { ActivityLevel.valueOf(it) }
                    ?: ActivityLevel.MODERATE,
                goal = prefs[Keys.GOAL]?.let { Goal.valueOf(it) } ?: Goal.MAINTAIN
            )
        }

    suspend fun save(context: Context, profile: UserProfile) {
        context.userProfileStore.edit { prefs ->
            prefs[Keys.GENDER] = profile.gender.name
            prefs[Keys.AGE] = profile.age
            prefs[Keys.HEIGHT] = profile.heightCm
            prefs[Keys.WEIGHT] = profile.weightKg
            prefs[Keys.ACTIVITY] = profile.activityLevel.name
            prefs[Keys.GOAL] = profile.goal.name
        }
    }
}
