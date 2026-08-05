package com.caloriecam.app.notify

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val hour = inputData.getInt(KEY_HOUR, -1)
        val label = inputData.getString(KEY_LABEL) ?: "прийом їжі"
        if (hour < 0) return Result.failure()

        NotificationHelper.showMealReminder(applicationContext, notificationId = hour, mealLabel = label)
        // Re-arm for the same time tomorrow — keeps the reminder recurring
        // without relying on exact-alarm permissions.
        ReminderScheduler.scheduleDaily(applicationContext, hour, label)
        return Result.success()
    }

    companion object {
        const val KEY_HOUR = "hour"
        const val KEY_LABEL = "label"
    }
}
