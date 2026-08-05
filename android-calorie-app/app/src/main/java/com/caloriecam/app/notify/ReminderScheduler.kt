package com.caloriecam.app.notify

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * Daily meal reminders via a self-rescheduling WorkManager chain (each
 * firing re-enqueues itself ~24h later). Avoids needing the
 * SCHEDULE_EXACT_ALARM permission — timing can drift by a few minutes,
 * which is fine for a "don't forget to log lunch" nudge.
 */
object ReminderScheduler {

    private val slots = listOf(8 to "сніданок", 13 to "обід", 19 to "вечерю")

    fun scheduleAll(context: Context) {
        slots.forEach { (hour, label) -> scheduleDaily(context, hour, label) }
    }

    fun cancelAll(context: Context) {
        val workManager = WorkManager.getInstance(context)
        slots.forEach { (hour, _) -> workManager.cancelUniqueWork(uniqueName(hour)) }
    }

    fun scheduleDaily(context: Context, hour: Int, label: String) {
        val now = LocalDateTime.now()
        var target = now.withHour(hour).withMinute(0).withSecond(0).withNano(0)
        if (!target.isAfter(now)) target = target.plusDays(1)
        val delayMillis = Duration.between(now, target).toMillis()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    ReminderWorker.KEY_HOUR to hour,
                    ReminderWorker.KEY_LABEL to label
                )
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(uniqueName(hour), ExistingWorkPolicy.REPLACE, request)
    }

    private fun uniqueName(hour: Int) = "meal_reminder_$hour"
}
