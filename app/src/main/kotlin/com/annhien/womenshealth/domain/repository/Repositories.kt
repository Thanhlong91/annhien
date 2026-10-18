package com.annhien.womenshealth.domain.repository

import com.annhien.womenshealth.core.database.entity.*
import com.annhien.womenshealth.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface CycleRepository {
    fun observePeriodEpisodes(): Flow<List<PeriodEpisode>>
    suspend fun getRecentEpisodes(limit: Int = 12): List<PeriodEpisode>
    suspend fun recordPeriodStart(date: LocalDate, isConfirmed: Boolean = true): String
    suspend fun recordPeriodEnd(episodeId: String, endDate: LocalDate)
    suspend fun recordPeriodRange(startDate: LocalDate, endDate: LocalDate, isConfirmed: Boolean = true): String
    suspend fun togglePeriodDay(date: LocalDate)
    suspend fun deleteEpisode(episodeId: String)
    suspend fun saveBleedingObservation(observation: BleedingObservation)
    fun observeBleedingByDate(date: LocalDate): Flow<List<BleedingObservation>>
}

interface JournalRepository {
    fun observeEntriesForMonth(year: Int, month: Int): Flow<List<JournalEntry>>
    fun observeEntryByDate(date: LocalDate): Flow<JournalEntry?>
    suspend fun getEntryByDate(date: LocalDate): JournalEntry?
    suspend fun saveJournalEntry(entry: JournalEntry): String
    suspend fun deleteJournalEntry(entryId: String)
    suspend fun getSymptomObservations(entryId: String): List<SymptomObservation>
    suspend fun saveSymptomObservations(entryId: String, observations: List<SymptomObservation>)
    suspend fun getMoodEntry(entryId: String): MoodEntry?
    suspend fun saveMoodEntry(mood: MoodEntry)
    suspend fun getSleepEntry(entryId: String): SleepEntry?
    suspend fun saveSleepEntry(sleep: SleepEntry)
    suspend fun getMeasurements(entryId: String): List<MeasurementEntry>
    suspend fun saveMeasurement(measurement: MeasurementEntry)
    suspend fun searchNotes(query: String): List<JournalEntry>
}

interface DailyHealthRepository {
    fun observeHabits(): Flow<List<DailyHabitEntity>>
    fun observeHabitLogs(date: LocalDate): Flow<List<HabitLogEntity>>
    suspend fun toggleHabit(habitId: String, date: LocalDate, completed: Boolean)
    suspend fun recordWaterIntake(date: LocalDate, amountMl: Int)
    suspend fun saveDailyHabit(habit: DailyHabitEntity)
}

interface PregnancyRepository {
    fun observeActivePregnancy(): Flow<PregnancyProfileEntity?>
    suspend fun savePregnancyProfile(profile: PregnancyProfileEntity)
    suspend fun recordKick(pregnancyId: String)
    suspend fun recordContraction(pregnancyId: String, startEpochMs: Long, endEpochMs: Long, intensity: Int)
    fun observeContractions(pregnancyId: String): Flow<List<ContractionEntryEntity>>
    fun observeActivePostpartum(): Flow<PostpartumProfileEntity?>
    suspend fun savePostpartumProfile(profile: PostpartumProfileEntity)
    suspend fun recordFeeding(entry: FeedingEntryEntity)
    fun observeRecentFeedings(postpartumId: String): Flow<List<FeedingEntryEntity>>
}

interface MedicalRecordsRepository {
    fun observeVisits(): Flow<List<MedicalVisitEntity>>
    suspend fun getVisitById(id: String): MedicalVisitEntity?
    suspend fun saveVisit(visit: MedicalVisitEntity)
    suspend fun deleteVisit(visitId: String)
    suspend fun searchVisits(query: String): List<MedicalVisitEntity>
    fun observeMedicationSchedules(): Flow<List<MedicationScheduleEntity>>
    suspend fun saveMedicationSchedule(schedule: MedicationScheduleEntity)
    suspend fun deactivateMedication(scheduleId: String)
    fun observeDocuments(visitId: String): Flow<List<MedicalDocumentEntity>>
    fun observeAllDocuments(): Flow<List<MedicalDocumentEntity>>
    suspend fun saveDocument(doc: MedicalDocumentEntity)
    suspend fun deleteDocument(docId: String)
}

interface KnowledgeRepository {
    fun observeArticles(category: String? = null): Flow<List<KnowledgeArticleEntity>>
    suspend fun getArticleById(id: String): KnowledgeArticleEntity?
    suspend fun searchArticles(query: String): List<KnowledgeArticleEntity>
    fun observeBookmarks(): Flow<List<String>>
    suspend fun toggleBookmark(articleId: String)
}

interface ReminderRepository {
    fun observeReminders(): Flow<List<ReminderEntity>>
    suspend fun saveReminder(reminder: ReminderEntity)
    suspend fun toggleReminder(reminderId: String, isActive: Boolean)
    suspend fun deleteReminder(reminderId: String)
}
