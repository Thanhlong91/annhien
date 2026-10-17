package com.annhien.womenshealth.core.database.dao

import androidx.room.*
import com.annhien.womenshealth.core.database.entity.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

// ─── UserProfile ──────────────────────────────────────────────────────────

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun observeProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET isOnboardingComplete = 1, updatedAt = :now WHERE id = 1")
    suspend fun markOnboardingComplete(now: Long)
}

// ─── LifeStageHistory ─────────────────────────────────────────────────────

@Dao
interface LifeStageHistoryDao {
    @Query("SELECT * FROM life_stage_history ORDER BY confirmedAt DESC")
    fun observeHistory(): Flow<List<LifeStageHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: LifeStageHistoryEntity)
}

// ─── ModulePreference ─────────────────────────────────────────────────────

@Dao
interface ModulePreferenceDao {
    @Query("SELECT * FROM module_preference")
    fun observeAll(): Flow<List<ModulePreferenceEntity>>

    @Query("SELECT * FROM module_preference WHERE moduleId = :moduleId")
    suspend fun getByModule(moduleId: String): ModulePreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(pref: ModulePreferenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(prefs: List<ModulePreferenceEntity>)
}

// ─── PeriodEpisode ────────────────────────────────────────────────────────

@Dao
interface PeriodEpisodeDao {
    @Query("SELECT * FROM period_episode ORDER BY startDate DESC")
    fun observeAll(): Flow<List<PeriodEpisodeEntity>>

    @Query("SELECT * FROM period_episode WHERE startDate >= :from AND startDate <= :to ORDER BY startDate ASC")
    suspend fun getInRange(from: String, to: String): List<PeriodEpisodeEntity>

    @Query("SELECT * FROM period_episode WHERE id = :id")
    suspend fun getById(id: String): PeriodEpisodeEntity?

    @Query("SELECT * FROM period_episode WHERE isOngoing = 1 LIMIT 1")
    fun observeOngoing(): Flow<PeriodEpisodeEntity?>

    @Query("SELECT * FROM period_episode ORDER BY startDate DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<PeriodEpisodeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(episode: PeriodEpisodeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(episode: PeriodEpisodeEntity)

    @Update
    suspend fun update(episode: PeriodEpisodeEntity)

    @Delete
    suspend fun delete(episode: PeriodEpisodeEntity)

    @Query("DELETE FROM period_episode WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM period_episode WHERE isMissedMarker = 0 AND isUncertain = 0 AND endDate IS NOT NULL")
    suspend fun countValidEpisodes(): Int
}

// ─── BleedingObservation ──────────────────────────────────────────────────

@Dao
interface BleedingObservationDao {
    @Query("SELECT * FROM bleeding_observation WHERE eventDate >= :from AND eventDate <= :to ORDER BY eventDate ASC")
    suspend fun getInRange(from: String, to: String): List<BleedingObservationEntity>

    @Query("SELECT * FROM bleeding_observation WHERE periodEpisodeId = :episodeId")
    fun observeByEpisode(episodeId: String): Flow<List<BleedingObservationEntity>>

    @Query("SELECT * FROM bleeding_observation WHERE eventDate = :date")
    fun observeByDate(date: String): Flow<List<BleedingObservationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(obs: BleedingObservationEntity)

    @Delete
    suspend fun delete(obs: BleedingObservationEntity)
}

// ─── SymptomDefinition ────────────────────────────────────────────────────

@Dao
interface SymptomDefinitionDao {
    @Query("SELECT * FROM symptom_definition WHERE isActive = 1 ORDER BY isDefault DESC, nameVi ASC")
    fun observeActive(): Flow<List<SymptomDefinitionEntity>>

    @Query("SELECT * FROM symptom_definition WHERE isDefault = 1")
    suspend fun getDefaults(): List<SymptomDefinitionEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfNotExists(symptom: SymptomDefinitionEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIfNotExists(symptoms: List<SymptomDefinitionEntity>)

    @Update
    suspend fun update(symptom: SymptomDefinitionEntity)

    @Query("UPDATE symptom_definition SET isActive = :isActive WHERE id = :id")
    suspend fun setActive(id: String, isActive: Boolean)
}

// ─── JournalEntry ─────────────────────────────────────────────────────────

@Dao
interface JournalEntryDao {
    @Query("SELECT * FROM journal_entry WHERE eventDate = :date ORDER BY eventTime ASC, createdAt ASC")
    fun observeByDate(date: String): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entry WHERE eventDate >= :from AND eventDate <= :to ORDER BY eventDate DESC, eventTime DESC")
    fun observeInRange(from: String, to: String): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entry WHERE eventDate >= :from AND eventDate <= :to ORDER BY eventDate DESC, eventTime DESC")
    suspend fun getByRange(from: String, to: String): List<JournalEntryEntity>

    @Query("SELECT * FROM journal_entry WHERE id = :id")
    suspend fun getById(id: String): JournalEntryEntity?

    @Query("""
        SELECT * FROM journal_entry 
        WHERE note LIKE '%' || :query || '%' 
        ORDER BY eventDate DESC LIMIT 100
    """)
    suspend fun searchByNote(query: String): List<JournalEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: JournalEntryEntity)

    @Update
    suspend fun update(entry: JournalEntryEntity)

    @Delete
    suspend fun delete(entry: JournalEntryEntity)

    @Query("DELETE FROM journal_entry WHERE id = :id")
    suspend fun deleteById(id: String)
}

// ─── SymptomObservation ───────────────────────────────────────────────────

@Dao
interface SymptomObservationDao {
    @Query("SELECT * FROM symptom_observation WHERE journalEntryId = :entryId")
    fun observeByEntry(entryId: String): Flow<List<SymptomObservationEntity>>

    @Query("SELECT * FROM symptom_observation WHERE journalEntryId = :entryId")
    suspend fun getByEntry(entryId: String): List<SymptomObservationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(obs: SymptomObservationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(observations: List<SymptomObservationEntity>)

    @Delete
    suspend fun delete(obs: SymptomObservationEntity)

    @Query("DELETE FROM symptom_observation WHERE journalEntryId = :entryId")
    suspend fun deleteByEntry(entryId: String)
}

// ─── MoodEntry ────────────────────────────────────────────────────────────

@Dao
interface MoodEntryDao {
    @Query("SELECT * FROM mood_entry WHERE journalEntryId = :entryId LIMIT 1")
    suspend fun getByEntry(entryId: String): MoodEntryEntity?

    @Query("SELECT * FROM mood_entry WHERE journalEntryId IN (:entryIds)")
    suspend fun getByEntries(entryIds: List<String>): List<MoodEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(mood: MoodEntryEntity)

    @Delete
    suspend fun delete(mood: MoodEntryEntity)
}

// ─── SleepEntry ───────────────────────────────────────────────────────────

@Dao
interface SleepEntryDao {
    @Query("SELECT * FROM sleep_entry WHERE journalEntryId = :entryId")
    suspend fun getByEntry(entryId: String): List<SleepEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(sleep: SleepEntryEntity)

    @Delete
    suspend fun delete(sleep: SleepEntryEntity)
}

// ─── MeasurementEntry ─────────────────────────────────────────────────────

@Dao
interface MeasurementEntryDao {
    @Query("SELECT * FROM measurement_entry WHERE journalEntryId = :entryId")
    suspend fun getByEntry(entryId: String): List<MeasurementEntryEntity>

    @Query("""
        SELECT me.* FROM measurement_entry me
        INNER JOIN journal_entry je ON me.journalEntryId = je.id
        WHERE me.measurementType = :type AND je.eventDate >= :from AND je.eventDate <= :to
        ORDER BY je.eventDate ASC
    """)
    suspend fun getByTypeInRange(type: String, from: String, to: String): List<MeasurementEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(measurement: MeasurementEntryEntity)

    @Delete
    suspend fun delete(measurement: MeasurementEntryEntity)
}

// ─── FertilityObservation ─────────────────────────────────────────────────

@Dao
interface FertilityObservationDao {
    @Query("SELECT * FROM fertility_observation WHERE journalEntryId = :entryId")
    suspend fun getByEntry(entryId: String): List<FertilityObservationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(obs: FertilityObservationEntity)

    @Delete
    suspend fun delete(obs: FertilityObservationEntity)
}

@Dao
interface FertilityEstimateSettingsDao {
    @Query("SELECT * FROM fertility_estimate_settings WHERE id = 1")
    fun observe(): Flow<FertilityEstimateSettingsEntity?>

    @Query("SELECT * FROM fertility_estimate_settings WHERE id = 1")
    suspend fun get(): FertilityEstimateSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: FertilityEstimateSettingsEntity)
}

@Dao
interface TestResultDao {
    @Query("SELECT * FROM test_result WHERE journalEntryId = :entryId")
    suspend fun getByEntry(entryId: String): List<TestResultEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(result: TestResultEntity)

    @Delete
    suspend fun delete(result: TestResultEntity)
}

// ─── PregnancyProfile ─────────────────────────────────────────────────────

@Dao
interface PregnancyProfileDao {
    @Query("SELECT * FROM pregnancy_profile ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PregnancyProfileEntity>>

    @Query("SELECT * FROM pregnancy_profile WHERE status = 'ACTIVE' LIMIT 1")
    fun observeActive(): Flow<PregnancyProfileEntity?>

    @Query("SELECT * FROM pregnancy_profile WHERE status = 'ACTIVE' LIMIT 1")
    suspend fun getActive(): PregnancyProfileEntity?

    @Query("SELECT * FROM pregnancy_profile WHERE id = :id")
    suspend fun getById(id: String): PregnancyProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: PregnancyProfileEntity)

    @Update
    suspend fun update(profile: PregnancyProfileEntity)
}

@Dao
interface ContractionEntryDao {
    @Query("SELECT * FROM contraction_entry WHERE pregnancyProfileId = :profileId ORDER BY startTime DESC LIMIT 50")
    fun observeRecent(profileId: String): Flow<List<ContractionEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: ContractionEntryEntity)

    @Update
    suspend fun update(entry: ContractionEntryEntity)

    @Delete
    suspend fun delete(entry: ContractionEntryEntity)
}

// ─── PostpartumProfile ────────────────────────────────────────────────────

@Dao
interface PostpartumProfileDao {
    @Query("SELECT * FROM postpartum_profile WHERE status = 'ACTIVE' LIMIT 1")
    fun observeActive(): Flow<PostpartumProfileEntity?>

    @Query("SELECT * FROM postpartum_profile ORDER BY deliveryDate DESC")
    fun observeAll(): Flow<List<PostpartumProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: PostpartumProfileEntity)

    @Update
    suspend fun update(profile: PostpartumProfileEntity)
}

@Dao
interface FeedingEntryDao {
    @Query("SELECT * FROM feeding_entry WHERE postpartumProfileId = :profileId ORDER BY startTime DESC LIMIT 100")
    fun observeRecent(profileId: String): Flow<List<FeedingEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: FeedingEntryEntity)

    @Update
    suspend fun update(entry: FeedingEntryEntity)

    @Delete
    suspend fun delete(entry: FeedingEntryEntity)
}

// ─── DailyHabit & HabitLog ────────────────────────────────────────────────

@Dao
interface DailyHabitDao {
    @Query("SELECT * FROM daily_habit WHERE isActive = 1 ORDER BY createdAt ASC")
    fun observeActive(): Flow<List<DailyHabitEntity>>

    @Query("SELECT * FROM daily_habit")
    fun observeAll(): Flow<List<DailyHabitEntity>>

    @Query("SELECT * FROM daily_habit WHERE habitType = :category")
    suspend fun getByCategory(category: String): List<DailyHabitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(habit: DailyHabitEntity)

    @Update
    suspend fun update(habit: DailyHabitEntity)

    @Query("UPDATE daily_habit SET isActive = :isActive WHERE id = :id")
    suspend fun setActive(id: String, isActive: Boolean)
}

@Dao
interface HabitLogDao {
    @Query("SELECT * FROM habit_log WHERE logDate = :date ORDER BY createdAt ASC")
    fun observeByDate(date: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_log WHERE habitId = :habitId AND logDate >= :from AND logDate <= :to")
    suspend fun getByHabitInRange(habitId: String, from: String, to: String): List<HabitLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: HabitLogEntity)

    @Delete
    suspend fun delete(log: HabitLogEntity)
}

// ─── RelaxationSession ────────────────────────────────────────────────────

@Dao
interface RelaxationSessionDao {
    @Query("SELECT * FROM relaxation_session ORDER BY startTime DESC LIMIT 50")
    fun observeRecent(): Flow<List<RelaxationSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: RelaxationSessionEntity)

    @Update
    suspend fun update(session: RelaxationSessionEntity)
}

// ─── MedicalVisit & MedicalDocument ──────────────────────────────────────

@Dao
interface MedicalVisitDao {
    @Query("SELECT * FROM medical_visit ORDER BY visitDate DESC")
    fun observeAll(): Flow<List<MedicalVisitEntity>>

    @Query("SELECT * FROM medical_visit WHERE id = :id")
    suspend fun getById(id: String): MedicalVisitEntity?

    @Query("""
        SELECT * FROM medical_visit 
        WHERE facility LIKE '%' || :query || '%' 
        OR doctor LIKE '%' || :query || '%'
        OR postVisitNotes LIKE '%' || :query || '%'
        ORDER BY visitDate DESC LIMIT 50
    """)
    suspend fun search(query: String): List<MedicalVisitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(visit: MedicalVisitEntity)

    @Update
    suspend fun update(visit: MedicalVisitEntity)

    @Delete
    suspend fun delete(visit: MedicalVisitEntity)
}

@Dao
interface MedicalDocumentDao {
    @Query("SELECT * FROM medical_document WHERE medicalVisitId = :visitId")
    fun observeByVisit(visitId: String): Flow<List<MedicalDocumentEntity>>

    @Query("SELECT * FROM medical_document WHERE medicalVisitId IS NULL")
    suspend fun getUnlinked(): List<MedicalDocumentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(doc: MedicalDocumentEntity)

    @Delete
    suspend fun delete(doc: MedicalDocumentEntity)
}

@Dao
interface MedicationScheduleDao {
    @Query("SELECT * FROM medication_schedule WHERE isActive = 1 ORDER BY startDate DESC")
    fun observeActive(): Flow<List<MedicationScheduleEntity>>

    @Query("SELECT * FROM medication_schedule ORDER BY startDate DESC")
    fun observeAll(): Flow<List<MedicationScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(schedule: MedicationScheduleEntity)

    @Update
    suspend fun update(schedule: MedicationScheduleEntity)

    @Query("UPDATE medication_schedule SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: String)
}

// ─── Reminder ─────────────────────────────────────────────────────────────

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminder WHERE isActive = 1 ORDER BY scheduledAt ASC")
    fun observeActive(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminder WHERE isActive = 1 ORDER BY scheduledAt ASC")
    suspend fun getActiveReminders(): List<ReminderEntity>

    @Query("SELECT * FROM reminder WHERE reminderType = :type AND isActive = 1")
    suspend fun getActiveByType(type: String): List<ReminderEntity>

    @Query("SELECT * FROM reminder WHERE moduleId = :moduleId AND isActive = 1")
    suspend fun getActiveByModule(moduleId: String): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(reminder: ReminderEntity)

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Query("UPDATE reminder SET isActive = 0, updatedAt = :now WHERE id = :id")
    suspend fun deactivate(id: String, now: Long)

    @Query("UPDATE reminder SET isActive = 0, updatedAt = :now WHERE reminderType = :type")
    suspend fun deactivateByType(type: String, now: Long)
}

// ─── KnowledgeArticle & Bookmark ──────────────────────────────────────────

@Dao
interface KnowledgeArticleDao {
    @Query("SELECT * FROM knowledge_article WHERE reviewStatus = 'APPROVED' ORDER BY updatedAt DESC")
    fun observeApproved(): Flow<List<KnowledgeArticleEntity>>

    @Query("SELECT * FROM knowledge_article WHERE category = :category AND reviewStatus = 'APPROVED' ORDER BY updatedAt DESC")
    fun observeByCategory(category: String): Flow<List<KnowledgeArticleEntity>>

    @Query("SELECT * FROM knowledge_article ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<KnowledgeArticleEntity>>

    @Query("SELECT * FROM knowledge_article WHERE id = :id")
    suspend fun getById(id: String): KnowledgeArticleEntity?

    @Query("""
        SELECT * FROM knowledge_article 
        WHERE (titleVi LIKE '%' || :query || '%' OR summaryVi LIKE '%' || :query || '%')
        AND reviewStatus = 'APPROVED'
        ORDER BY updatedAt DESC LIMIT 50
    """)
    suspend fun search(query: String): List<KnowledgeArticleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(article: KnowledgeArticleEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfNotExists(article: KnowledgeArticleEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIfNotExists(articles: List<KnowledgeArticleEntity>)
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmark")
    fun observeAll(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmark WHERE articleId = :articleId LIMIT 1")
    suspend fun getByArticle(articleId: String): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmark WHERE articleId = :articleId")
    suspend fun deleteByArticle(articleId: String)
}

// ─── Attachment ───────────────────────────────────────────────────────────

@Dao
interface AttachmentDao {
    @Query("SELECT * FROM attachment WHERE ownerId = :ownerId AND ownerType = :ownerType")
    fun observeByOwner(ownerId: String, ownerType: String): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachment")
    suspend fun getAll(): List<AttachmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(attachment: AttachmentEntity)

    @Delete
    suspend fun delete(attachment: AttachmentEntity)

    @Query("DELETE FROM attachment WHERE id = :id")
    suspend fun deleteById(id: String)
}
