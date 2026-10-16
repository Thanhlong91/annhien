package com.annhien.womenshealth.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.annhien.womenshealth.domain.model.*
import com.annhien.womenshealth.domain.repository.CycleRepository
import com.annhien.womenshealth.domain.repository.JournalRepository
import com.annhien.womenshealth.domain.usecase.cycle.CyclePredictionEngine
import com.annhien.womenshealth.domain.usecase.cycle.CyclePredictionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class CalendarUiState(
    val currentYearMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val periodEpisodes: List<PeriodEpisode> = emptyList(),
    val selectedDayJournalEntry: JournalEntry? = null,
    val selectedDaySymptoms: List<SymptomObservation> = emptyList(),
    val predictedNextStart: LocalDate? = null,
    val predictedNextEnd: LocalDate? = null,
    val monthEntriesMap: Map<LocalDate, JournalEntry> = emptyMap()
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val cycleRepository: CycleRepository,
    private val journalRepository: JournalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadDataForMonth(_uiState.value.currentYearMonth)
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        loadDayDetails(date)
    }

    fun previousMonth() {
        val prev = _uiState.value.currentYearMonth.minusMonths(1)
        _uiState.update { it.copy(currentYearMonth = prev) }
        loadDataForMonth(prev)
    }

    fun nextMonth() {
        val next = _uiState.value.currentYearMonth.plusMonths(1)
        _uiState.update { it.copy(currentYearMonth = next) }
        loadDataForMonth(next)
    }

    private fun loadDataForMonth(yearMonth: YearMonth) {
        viewModelScope.launch {
            cycleRepository.observePeriodEpisodes().collect { episodes ->
                val prediction = CyclePredictionEngine.predictNextPeriod(episodes, LocalDate.now())
                var pStart: LocalDate? = null
                var pEnd: LocalDate? = null
                if (prediction is CyclePredictionResult.Predicted) {
                    pStart = prediction.expectedStartDate
                    pEnd = prediction.expectedEndDate
                }

                _uiState.update {
                    it.copy(
                        periodEpisodes = episodes,
                        predictedNextStart = pStart,
                        predictedNextEnd = pEnd
                    )
                }
            }
        }

        viewModelScope.launch {
            journalRepository.observeEntriesForMonth(yearMonth.year, yearMonth.monthValue).collect { entries ->
                val map = entries.associateBy { it.eventDate }
                _uiState.update { it.copy(monthEntriesMap = map) }
                loadDayDetails(_uiState.value.selectedDate)
            }
        }
    }

    private fun loadDayDetails(date: LocalDate) {
        viewModelScope.launch {
            val entry = journalRepository.getEntryByDate(date)
            var symptoms = emptyList<SymptomObservation>()
            if (entry != null) {
                symptoms = journalRepository.getSymptomObservations(entry.id)
            }
            _uiState.update {
                it.copy(
                    selectedDayJournalEntry = entry,
                    selectedDaySymptoms = symptoms
                )
            }
        }
    }

    fun togglePeriodOnSelectedDate() {
        viewModelScope.launch {
            val date = _uiState.value.selectedDate
            val existing = _uiState.value.periodEpisodes.find { ep ->
                val end = ep.endDate ?: LocalDate.now()
                !date.isBefore(ep.startDate) && !date.isAfter(end)
            }

            if (existing != null) {
                cycleRepository.deleteEpisode(existing.id)
            } else {
                cycleRepository.recordPeriodStart(date, isConfirmed = true)
            }
            loadDataForMonth(_uiState.value.currentYearMonth)
        }
    }
}
