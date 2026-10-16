package com.annhien.womenshealth.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.annhien.womenshealth.domain.model.*
import com.annhien.womenshealth.domain.repository.CycleRepository
import com.annhien.womenshealth.domain.repository.DailyHealthRepository
import com.annhien.womenshealth.domain.repository.JournalRepository
import com.annhien.womenshealth.domain.usecase.cycle.CyclePredictionEngine
import com.annhien.womenshealth.domain.usecase.cycle.CyclePredictionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class HomeUiState(
    val currentDate: LocalDate = LocalDate.now(),
    val ongoingEpisode: PeriodEpisode? = null,
    val cycleDay: Int? = null,
    val predictionResult: CyclePredictionResult? = null,
    val daysUntilNextPeriod: Long? = null,
    val todayEntry: JournalEntry? = null,
    val todayWaterIntakeMl: Int = 0,
    val recentSymptoms: List<SymptomObservation> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val cycleRepository: CycleRepository,
    private val journalRepository: JournalRepository,
    private val dailyHealthRepository: DailyHealthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            val today = LocalDate.now()

            // 1. Observe episodes
            cycleRepository.observePeriodEpisodes().collect { episodes ->
                val ongoing = episodes.find { it.isOngoing }
                var cycleDay: Int? = null
                val sorted = episodes.sortedByDescending { it.startDate }

                val lastEpisode = ongoing ?: sorted.firstOrNull()
                if (lastEpisode != null) {
                    val days = ChronoUnit.DAYS.between(lastEpisode.startDate, today) + 1
                    if (days in 1..90) {
                        cycleDay = days.toInt()
                    }
                }

                // Predict next period if enough history
                val prediction = CyclePredictionEngine.predictNextPeriod(episodes, today)
                var daysUntil: Long? = null
                if (prediction is CyclePredictionResult.Predicted) {
                    daysUntil = ChronoUnit.DAYS.between(today, prediction.expectedStartDate)
                }

                _uiState.update { current ->
                    current.copy(
                        currentDate = today,
                        ongoingEpisode = ongoing,
                        cycleDay = cycleDay,
                        predictionResult = prediction,
                        daysUntilNextPeriod = daysUntil,
                        isLoading = false
                    )
                }
            }
        }

        viewModelScope.launch {
            val today = LocalDate.now()
            journalRepository.observeEntryByDate(today).collect { entry ->
                var symptoms: List<SymptomObservation> = emptyList()
                if (entry != null) {
                    symptoms = journalRepository.getSymptomObservations(entry.id)
                }
                _uiState.update { it.copy(todayEntry = entry, recentSymptoms = symptoms) }
            }
        }
    }

    fun togglePeriodToday() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val ongoing = _uiState.value.ongoingEpisode
            if (ongoing != null) {
                // End period
                cycleRepository.recordPeriodEnd(ongoing.id, today)
            } else {
                // Start period
                cycleRepository.recordPeriodStart(today, isConfirmed = true)
            }
            loadHomeData()
        }
    }

    fun logQuickWater(amountMl: Int) {
        viewModelScope.launch {
            val newTotal = _uiState.value.todayWaterIntakeMl + amountMl
            dailyHealthRepository.recordWaterIntake(LocalDate.now(), newTotal)
            _uiState.update { it.copy(todayWaterIntakeMl = newTotal) }
        }
    }
}
