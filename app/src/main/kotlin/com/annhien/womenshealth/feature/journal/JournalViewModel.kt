package com.annhien.womenshealth.feature.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.annhien.womenshealth.domain.model.*
import com.annhien.womenshealth.domain.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class JournalUiState(
    val entries: List<JournalEntry> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

data class JournalEditUiState(
    val entryId: String? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedSymptoms: Set<String> = emptySet(), // symptom names
    val symptomSeverityMap: Map<String, Int> = emptyMap(),
    val selectedMoodLevel: Int = 3,
    val selectedMoodTags: Set<String> = emptySet(),
    val sleepHours: Float = 7.0f,
    val sleepQuality: Int = 3,
    val temperatureCelsius: Float? = null,
    val note: String = "",
    val isSaved: Boolean = false
)

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val journalRepository: JournalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    private val _editState = MutableStateFlow(JournalEditUiState())
    val editState: StateFlow<JournalEditUiState> = _editState.asStateFlow()

    init {
        loadEntries()
    }

    fun loadEntries() {
        viewModelScope.launch {
            val now = LocalDate.now()
            journalRepository.observeEntriesForMonth(now.year, now.monthValue).collect { list ->
                _uiState.update { it.copy(entries = list.sortedByDescending { e -> e.eventDate }) }
            }
        }
    }

    fun searchNotes(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            if (query.isBlank()) {
                loadEntries()
            } else {
                val results = journalRepository.searchNotes(query)
                _uiState.update { it.copy(entries = results) }
            }
        }
    }

    fun initEdit(date: LocalDate, entryId: String? = null) {
        _editState.value = JournalEditUiState(selectedDate = date, entryId = entryId)
        viewModelScope.launch {
            val existing = if (entryId != null) {
                journalRepository.getEntryByDate(date)
            } else {
                journalRepository.getEntryByDate(date)
            }

            if (existing != null) {
                val symptoms = journalRepository.getSymptomObservations(existing.id)
                val mood = journalRepository.getMoodEntry(existing.id)
                val sleep = journalRepository.getSleepEntry(existing.id)
                val measurements = journalRepository.getMeasurements(existing.id)

                val temp = measurements.find { it.measurementType == MeasurementType.BBT }?.value?.toFloat()

                val sleepMinutes = sleep?.let { java.time.Duration.between(it.sleepStart, it.sleepEnd).toMinutes() } ?: 420L

                _editState.update {
                    it.copy(
                        entryId = existing.id,
                        note = existing.note ?: "",
                        selectedSymptoms = symptoms.map { s -> s.symptomName }.toSet(),
                        symptomSeverityMap = symptoms.associate { s -> s.symptomName to (s.severity ?: 3) },
                        selectedMoodLevel = mood?.moodLevel ?: 3,
                        selectedMoodTags = mood?.moodTags?.toSet() ?: emptySet(),
                        sleepHours = sleepMinutes / 60f,
                        sleepQuality = sleep?.qualityLevel ?: 3,
                        temperatureCelsius = temp
                    )
                }
            }
        }
    }

    fun toggleSymptom(name: String) {
        _editState.update { state ->
            val set = state.selectedSymptoms.toMutableSet()
            if (set.contains(name)) set.remove(name) else set.add(name)
            state.copy(selectedSymptoms = set)
        }
    }

    fun updateMoodLevel(level: Int) {
        _editState.update { it.copy(selectedMoodLevel = level) }
    }

    fun toggleMoodTag(tag: String) {
        _editState.update { state ->
            val set = state.selectedMoodTags.toMutableSet()
            if (set.contains(tag)) set.remove(tag) else set.add(tag)
            state.copy(selectedMoodTags = set)
        }
    }

    fun updateNote(note: String) {
        _editState.update { it.copy(note = note) }
    }

    fun saveJournal() {
        viewModelScope.launch {
            val state = _editState.value
            val entry = JournalEntry(
                id = state.entryId ?: "",
                eventDate = state.selectedDate,
                eventTime = LocalTime.now(),
                moduleId = null,
                entryType = JournalEntryType.SYMPTOM,
                note = state.note.ifBlank { null },
                createdAt = java.time.Instant.now(),
                updatedAt = java.time.Instant.now()
            )

            val entryId = journalRepository.saveJournalEntry(entry)

            // Save symptoms
            val symptomObs = state.selectedSymptoms.map { name ->
                SymptomObservation(
                    id = "",
                    journalEntryId = entryId,
                    symptomDefinitionId = name,
                    symptomName = name,
                    severity = state.symptomSeverityMap[name] ?: 3,
                    bodyLocation = null,
                    note = null
                )
            }
            journalRepository.saveSymptomObservations(entryId, symptomObs)

            // Save mood
            journalRepository.saveMoodEntry(
                MoodEntry(
                    id = "",
                    journalEntryId = entryId,
                    moodLevel = state.selectedMoodLevel,
                    moodTags = state.selectedMoodTags.toList(),
                    note = null
                )
            )

            // Save sleep
            val nowInstant = java.time.Instant.now()
            val sleepSeconds = (state.sleepHours * 3600).toLong()
            journalRepository.saveSleepEntry(
                SleepEntry(
                    id = "",
                    journalEntryId = entryId,
                    sleepStart = nowInstant.minusSeconds(sleepSeconds),
                    sleepEnd = nowInstant,
                    qualityLevel = state.sleepQuality,
                    note = null
                )
            )

            _editState.update { it.copy(isSaved = true) }
            loadEntries()
        }
    }
}
