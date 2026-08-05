package com.caloriecam.app.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Exports/imports all local data (profile, food/weight/water log) as a single
 * JSON file the user picks a destination for via the system file picker —
 * useful before switching phones. Photos referenced by food log entries stay
 * on the original device (only local file paths are stored) and won't be
 * restored; the food entries themselves (name, grams, kcal, macros) are.
 */
object BackupManager {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    suspend fun export(context: Context, destination: Uri): Boolean {
        val db = AppDatabase.getInstance(context)
        val profile = UserProfileRepository.profileFlow(context).first()
        val data = BackupData(
            exportedAt = System.currentTimeMillis(),
            profile = profile,
            foodLog = db.foodLogDao().getAll(),
            weightLog = db.weightDao().getAll(),
            waterLog = db.waterDao().getAll()
        )
        val text = json.encodeToString(data)
        return runCatching {
            val out = context.contentResolver.openOutputStream(destination) ?: return false
            out.use { it.write(text.toByteArray(Charsets.UTF_8)) }
            true
        }.getOrDefault(false)
    }

    suspend fun import(context: Context, source: Uri): Boolean {
        val text = runCatching {
            context.contentResolver.openInputStream(source)?.use { it.readBytes().toString(Charsets.UTF_8) }
        }.getOrNull() ?: return false

        val data = runCatching { json.decodeFromString<BackupData>(text) }.getOrNull() ?: return false

        val db = AppDatabase.getInstance(context)
        db.foodLogDao().clearAll()
        db.foodLogDao().insertAll(data.foodLog)
        db.weightDao().clearAll()
        db.weightDao().insertAll(data.weightLog)
        db.waterDao().clearAll()
        db.waterDao().insertAll(data.waterLog)
        data.profile?.let { UserProfileRepository.save(context, it) }
        return true
    }
}
