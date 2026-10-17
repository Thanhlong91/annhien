package com.annhien.womenshealth.di

import android.content.Context
import androidx.room.Room
import com.annhien.womenshealth.core.database.AnNhienDatabase
import com.annhien.womenshealth.core.database.dao.*
import com.annhien.womenshealth.core.security.KeystoreManager
import com.annhien.womenshealth.core.time.DateTimeProvider
import com.annhien.womenshealth.core.time.SystemDateTimeProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

/**
 * Hilt module providing application-level singletons.
 *
 * Database setup:
 * - SQLCipher is used to encrypt the Room database.
 * - The passphrase is managed by KeystoreManager (Android Keystore + AES-GCM).
 * - Destructive migration is DISABLED. All migrations must be explicit.
 * - allowMainThreadQueries is NOT set (all queries use coroutines).
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideKeystoreManager(
        @ApplicationContext context: Context
    ): KeystoreManager = KeystoreManager(context)

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        keystoreManager: KeystoreManager
    ): AnNhienDatabase {
        // Ensure SQLCipher native libraries are loaded
        net.sqlcipher.database.SQLiteDatabase.loadLibs(context)

        // Get the database passphrase from Keystore (never hardcoded)
        val passphrase = keystoreManager.getDatabasePassphrase()
        // SupportFactory internally zeroes out passphrase upon DB open when clearPassphrase = true (default)
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(
            context.applicationContext,
            AnNhienDatabase::class.java,
            AnNhienDatabase.DATABASE_NAME
        )
            .openHelperFactory(factory)
            .build()
    }

    @Provides
    @Singleton
    fun provideDateTimeProvider(): DateTimeProvider = SystemDateTimeProvider()

    // ─── DAOs ─────────────────────────────────────────────────────────────
    @Provides fun provideUserProfileDao(db: AnNhienDatabase) = db.userProfileDao()
    @Provides fun provideLifeStageHistoryDao(db: AnNhienDatabase) = db.lifeStageHistoryDao()
    @Provides fun provideModulePreferenceDao(db: AnNhienDatabase) = db.modulePreferenceDao()
    @Provides fun providePeriodEpisodeDao(db: AnNhienDatabase) = db.periodEpisodeDao()
    @Provides fun provideBleedingObservationDao(db: AnNhienDatabase) = db.bleedingObservationDao()
    @Provides fun provideSymptomDefinitionDao(db: AnNhienDatabase) = db.symptomDefinitionDao()
    @Provides fun provideJournalEntryDao(db: AnNhienDatabase) = db.journalEntryDao()
    @Provides fun provideSymptomObservationDao(db: AnNhienDatabase) = db.symptomObservationDao()
    @Provides fun provideMoodEntryDao(db: AnNhienDatabase) = db.moodEntryDao()
    @Provides fun provideSleepEntryDao(db: AnNhienDatabase) = db.sleepEntryDao()
    @Provides fun provideMeasurementEntryDao(db: AnNhienDatabase) = db.measurementEntryDao()
    @Provides fun provideFertilityObservationDao(db: AnNhienDatabase) = db.fertilityObservationDao()
    @Provides fun provideFertilityEstimateSettingsDao(db: AnNhienDatabase) = db.fertilityEstimateSettingsDao()
    @Provides fun provideTestResultDao(db: AnNhienDatabase) = db.testResultDao()
    @Provides fun providePregnancyProfileDao(db: AnNhienDatabase) = db.pregnancyProfileDao()
    @Provides fun provideContractionEntryDao(db: AnNhienDatabase) = db.contractionEntryDao()
    @Provides fun providePostpartumProfileDao(db: AnNhienDatabase) = db.postpartumProfileDao()
    @Provides fun provideFeedingEntryDao(db: AnNhienDatabase) = db.feedingEntryDao()
    @Provides fun provideDailyHabitDao(db: AnNhienDatabase) = db.dailyHabitDao()
    @Provides fun provideHabitLogDao(db: AnNhienDatabase) = db.dailyHabitDao()
    @Provides fun provideRelaxationSessionDao(db: AnNhienDatabase) = db.relaxationSessionDao()
    @Provides fun provideMedicalVisitDao(db: AnNhienDatabase) = db.medicalVisitDao()
    @Provides fun provideMedicalDocumentDao(db: AnNhienDatabase) = db.medicalDocumentDao()
    @Provides fun provideMedicationScheduleDao(db: AnNhienDatabase) = db.medicationScheduleDao()
    @Provides fun provideReminderDao(db: AnNhienDatabase) = db.reminderDao()
    @Provides fun provideKnowledgeArticleDao(db: AnNhienDatabase) = db.knowledgeArticleDao()
    @Provides fun provideBookmarkDao(db: AnNhienDatabase) = db.bookmarkDao()
    @Provides fun provideAttachmentDao(db: AnNhienDatabase) = db.attachmentDao()
}
