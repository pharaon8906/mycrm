package com.caloriecam.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.caloriecam.app.data.AppDatabase
import com.caloriecam.app.data.DailyTotal
import com.caloriecam.app.data.ProgressPhoto
import com.caloriecam.app.data.UserProfileRepository
import com.caloriecam.app.data.WeightEntry
import com.caloriecam.app.health.HealthConnectManager
import com.caloriecam.app.util.PhotoStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val foodDao = AppDatabase.getInstance(application).foodLogDao()
    private val weightDao = AppDatabase.getInstance(application).weightDao()
    private val progressPhotoDao = AppDatabase.getInstance(application).progressPhotoDao()

    private val _rangeDays = MutableStateFlow(7)
    val rangeDays: StateFlow<Int> = _rangeDays

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailyTotals: StateFlow<List<DailyTotal>> = _rangeDays
        .flatMapLatest { days ->
            val since = Instant.now().minus(days.toLong(), ChronoUnit.DAYS).toEpochMilli()
            foodDao.observeDailyTotals(since)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weightEntries: StateFlow<List<WeightEntry>> = run {
        val since = Instant.now().minus(180, ChronoUnit.DAYS).toEpochMilli()
        weightDao.observeSince(since)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    val progressPhotos: StateFlow<List<ProgressPhoto>> = progressPhotoDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setRange(days: Int) {
        _rangeDays.value = days
    }

    fun addWeight(weightKg: Double) {
        viewModelScope.launch {
            weightDao.insert(WeightEntry(weightKg = weightKg, timestamp = System.currentTimeMillis()))
            // Keep the profile's weight (used for BMR) in sync with the latest logged weight.
            val app = getApplication<Application>()
            val current = UserProfileRepository.profileFlow(app).first()
            if (current != null) {
                UserProfileRepository.save(app, current.copy(weightKg = weightKg))
            }
            // Best-effort: silently no-ops if Health Connect isn't installed/authorized.
            HealthConnectManager.writeWeight(app, weightKg)
        }
    }

    fun addProgressPhoto(file: File) {
        viewModelScope.launch {
            progressPhotoDao.insert(ProgressPhoto(photoPath = file.absolutePath, timestamp = System.currentTimeMillis()))
        }
    }

    fun deleteProgressPhoto(photo: ProgressPhoto) {
        viewModelScope.launch {
            progressPhotoDao.delete(photo)
            PhotoStore.delete(File(photo.photoPath))
        }
    }
}
