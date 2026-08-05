package com.caloriecam.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.caloriecam.app.data.AppDatabase
import com.caloriecam.app.data.FoodLogEntry
import com.caloriecam.app.data.UserProfile
import com.caloriecam.app.data.UserProfileRepository
import com.caloriecam.app.data.WaterEntry
import com.caloriecam.app.util.PhotoStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.ZoneId

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).foodLogDao()
    private val waterDao = AppDatabase.getInstance(application).waterDao()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val entries: StateFlow<List<FoodLogEntry>> = _selectedDate
        .flatMapLatest { date ->
            val (start, end) = dayRangeMillis(date)
            dao.observeForDay(start, end)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profile: StateFlow<UserProfile?> = UserProfileRepository.profileFlow(application)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Water tracking always reflects today, regardless of which day's food log is being browsed.
    val todayWater: StateFlow<List<WaterEntry>> = run {
        val (start, end) = dayRangeMillis(LocalDate.now())
        waterDao.observeForDay(start, end)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun addWater(amountMl: Int = 250) {
        viewModelScope.launch {
            waterDao.insert(WaterEntry(amountMl = amountMl, timestamp = System.currentTimeMillis()))
        }
    }

    fun removeLastWater() {
        viewModelScope.launch {
            val last = todayWater.value.firstOrNull() ?: return@launch
            waterDao.delete(last)
        }
    }

    fun previousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    fun nextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }

    fun goToToday() {
        _selectedDate.value = LocalDate.now()
    }

    fun delete(entry: FoodLogEntry) {
        viewModelScope.launch {
            dao.delete(entry)
            entry.photoPath?.let { PhotoStore.delete(File(it)) }
        }
    }

    private fun dayRangeMillis(date: LocalDate): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return start to end
    }
}
