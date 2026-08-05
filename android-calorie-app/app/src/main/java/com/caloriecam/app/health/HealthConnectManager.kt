package com.caloriecam.app.health

import android.content.Context
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Mass
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class HealthConnectAvailability { AVAILABLE, NOT_INSTALLED, UNSUPPORTED }

data class TodayActivity(val steps: Long, val activeCaloriesBurned: Int)

/**
 * Wraps Health Connect (androidx.health.connect) — the Android-standard
 * aggregator that Google Fit, Samsung Health and most fitness trackers
 * write into. Reads today's steps/active calories and writes logged body
 * weight so other health apps can see it. All calls are best-effort:
 * Health Connect may not be installed, or the user may not have granted
 * permission, and every function degrades gracefully in that case.
 */
object HealthConnectManager {

    val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getWritePermission(WeightRecord::class)
    )

    fun permissionRequestContract(): ActivityResultContract<Set<String>, Set<String>> =
        PermissionController.createRequestPermissionResultContract()

    fun availability(context: Context): HealthConnectAvailability =
        when (HealthConnectClient.getSdkStatus(context)) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.AVAILABLE
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HealthConnectAvailability.NOT_INSTALLED
            else -> HealthConnectAvailability.UNSUPPORTED
        }

    private fun client(context: Context): HealthConnectClient = HealthConnectClient.getOrCreate(context)

    suspend fun hasAllPermissions(context: Context): Boolean = runCatching {
        val granted = client(context).permissionController.getGrantedPermissions()
        granted.containsAll(permissions)
    }.getOrDefault(false)

    suspend fun readTodayActivity(context: Context): TodayActivity? = runCatching {
        val zone = ZoneId.systemDefault()
        val range = TimeRangeFilter.between(
            LocalDate.now().atStartOfDay(zone).toInstant(),
            Instant.now()
        )
        val healthConnect = client(context)

        val steps = healthConnect.aggregate(
            AggregateRequest(metrics = setOf(StepsRecord.COUNT_TOTAL), timeRangeFilter = range)
        )[StepsRecord.COUNT_TOTAL] ?: 0L

        val kcal = healthConnect.aggregate(
            AggregateRequest(metrics = setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL), timeRangeFilter = range)
        )[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories?.toInt() ?: 0

        TodayActivity(steps, kcal)
    }.getOrNull()

    suspend fun writeWeight(context: Context, weightKg: Double, time: Instant = Instant.now()) {
        runCatching {
            val record = WeightRecord(
                weight = Mass.kilograms(weightKg),
                time = time,
                zoneOffset = ZoneId.systemDefault().rules.getOffset(time)
            )
            client(context).insertRecords(listOf(record))
        }
    }
}
