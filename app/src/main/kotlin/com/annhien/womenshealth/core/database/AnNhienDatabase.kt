package com.annhien.womenshealth.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.annhien.womenshealth.core.database.converter.DateTimeConverters
import com.annhien.womenshealth.core.database.dao.*
import com.annhien.womenshealth.core.database.entity.*

/**
 * An Nhiên Room Database.
 *
 * DATABASE_VERSION: Increment when schema changes.
 * All migrations must be additive — destructive migration is DISABLED.
 *
 * Security: This database is opened through SQLCipher via
 * [AnNhienDatabaseFactory], which provides the encryption key from
 * Android Keystore. The key is derived per-device and optionally
 * reinforced with the user's PIN via PBKDF2.
 */
@Database(
    entities = [
        UserProfileEntity::class,
        ModulePreferenceEntity::class,
        LifeStageHistoryEntity::class,
        PeriodEpisodeEntity::class,
        BleedingObservationEntity::class,
        SymptomDefinitionEntity::class,
        JournalEntryEntity::class,
        SymptomObservationEntity::class,
        MoodEntryEntity::class,
        SleepEntryEntity::class,
        MeasurementEntryEntity::class,
        FertilityObservationEntity::class,
        FertilityEstimateSettingsEntity::class,
        TestResultEntity::class,
        PregnancyProfileEntity::class,
        ContractionEntryEntity::class,
        PostpartumProfileEntity::class,
        FeedingEntryEntity::class,
        DailyHabitEntity::class,
        HabitLogEntity::class,
        RelaxationSessionEntity::class,
        MedicalVisitEntity::class,
        MedicalDocumentEntity::class,
        MedicationScheduleEntity::class,
        ReminderEntity::class,
        KnowledgeArticleEntity::class,
        BookmarkEntity::class,
        AttachmentEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DateTimeConverters::class)
abstract class AnNhienDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun lifeStageHistoryDao(): LifeStageHistoryDao
    abstract fun modulePreferenceDao(): ModulePreferenceDao
    abstract fun periodEpisodeDao(): PeriodEpisodeDao
    abstract fun bleedingObservationDao(): BleedingObservationDao
    abstract fun symptomDefinitionDao(): SymptomDefinitionDao
    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun symptomObservationDao(): SymptomObservationDao
    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun sleepEntryDao(): SleepEntryDao
    abstract fun measurementEntryDao(): MeasurementEntryDao
    abstract fun fertilityObservationDao(): FertilityObservationDao
    abstract fun fertilityEstimateSettingsDao(): FertilityEstimateSettingsDao
    abstract fun testResultDao(): TestResultDao
    abstract fun pregnancyProfileDao(): PregnancyProfileDao
    abstract fun contractionEntryDao(): ContractionEntryDao
    abstract fun postpartumProfileDao(): PostpartumProfileDao
    abstract fun feedingEntryDao(): FeedingEntryDao
    abstract fun dailyHabitDao(): DailyHabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun relaxationSessionDao(): RelaxationSessionDao
    abstract fun medicalVisitDao(): MedicalVisitDao
    abstract fun medicalDocumentDao(): MedicalDocumentDao
    abstract fun medicationScheduleDao(): MedicationScheduleDao
    abstract fun reminderDao(): ReminderDao
    abstract fun knowledgeArticleDao(): KnowledgeArticleDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun attachmentDao(): AttachmentDao

    companion object {
        const val DATABASE_NAME = "annhien_db"
        const val DATABASE_VERSION = 1
    }
}
