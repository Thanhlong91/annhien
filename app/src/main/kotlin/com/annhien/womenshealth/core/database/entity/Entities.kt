package com.annhien.womenshealth.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

// ─── User & Profile ───────────────────────────────────────────────────────

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val nickname: String?,
    val ageGroup: String?,          // AgeGroup enum name
    val dateOfBirth: LocalDate?,
    val primaryLifeStage: String,   // LifeStage enum name
    val temperatureUnit: String,    // TemperatureUnit enum name
    val weightUnit: String,         // WeightUnit enum name
    val liquidUnit: String,         // LiquidUnit enum name
    val appLockType: String,        // AppLockType enum name
    val isOnboardingComplete: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)

@Entity(tableName = "module_preference")
data class ModulePreferenceEntity(
    @PrimaryKey val moduleId: String,
    val isEnabled: Boolean,
    val updatedAt: Instant
)

@Entity(tableName = "life_stage_history")
data class LifeStageHistoryEntity(
    @PrimaryKey val id: String,
    val fromStage: String?,
    val toStage: String,
    val confirmedAt: Instant,
    val note: String?
)

// ─── Period ───────────────────────────────────────────────────────────────

@Entity(
    tableName = "period_episode",
    indices = [Index("startDate")]
)
data class PeriodEpisodeEntity(
    @PrimaryKey val id: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val isOngoing: Boolean,
    val isUncertain: Boolean,
    val isMissedMarker: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)

@Entity(
    tableName = "bleeding_observation",
    foreignKeys = [
        ForeignKey(
            entity = PeriodEpisodeEntity::class,
            parentColumns = ["id"],
            childColumns = ["periodEpisodeId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("eventDate"), Index("periodEpisodeId")]
)
data class BleedingObservationEntity(
    @PrimaryKey val id: String,
    val eventDate: LocalDate,
    val flowLevel: String?,         // FlowLevel enum name
    val isOutOfCycle: Boolean,
    val periodEpisodeId: String?,
    val note: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)

// ─── Symptoms ─────────────────────────────────────────────────────────────

@Entity(tableName = "symptom_definition")
data class SymptomDefinitionEntity(
    @PrimaryKey val id: String,
    val nameVi: String,
    val category: String,           // SymptomCategory enum name
    val isDefault: Boolean,
    val isActive: Boolean,
    val createdAt: Instant
)

// ─── Journal ──────────────────────────────────────────────────────────────

@Entity(
    tableName = "journal_entry",
    indices = [Index("eventDate"), Index("moduleId"), Index("entryType")]
)
data class JournalEntryEntity(
    @PrimaryKey val id: String,
    val eventDate: LocalDate,
    val eventTime: LocalTime?,
    val moduleId: String?,
    val entryType: String,          // JournalEntryType enum name
    val note: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)

@Entity(
    tableName = "symptom_observation",
    foreignKeys = [
        ForeignKey(
            entity = JournalEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["journalEntryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SymptomDefinitionEntity::class,
            parentColumns = ["id"],
            childColumns = ["symptomDefinitionId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("journalEntryId"), Index("symptomDefinitionId")]
)
data class SymptomObservationEntity(
    @PrimaryKey val id: String,
    val journalEntryId: String,
    val symptomDefinitionId: String,
    val severity: Int?,
    val bodyLocation: String?,
    val note: String?
)

@Entity(
    tableName = "mood_entry",
    foreignKeys = [
        ForeignKey(
            entity = JournalEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["journalEntryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("journalEntryId")]
)
data class MoodEntryEntity(
    @PrimaryKey val id: String,
    val journalEntryId: String,
    val moodLevel: Int,
    val moodTagsJson: String,       // JSON array
    val note: String?
)

@Entity(
    tableName = "sleep_entry",
    foreignKeys = [
        ForeignKey(
            entity = JournalEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["journalEntryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("journalEntryId")]
)
data class SleepEntryEntity(
    @PrimaryKey val id: String,
    val journalEntryId: String,
    val sleepStart: Instant,
    val sleepEnd: Instant,
    val qualityLevel: Int?,
    val note: String?
)

@Entity(
    tableName = "measurement_entry",
    foreignKeys = [
        ForeignKey(
            entity = JournalEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["journalEntryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("journalEntryId"), Index("measurementType")]
)
data class MeasurementEntryEntity(
    @PrimaryKey val id: String,
    val journalEntryId: String,
    val measurementType: String,    // MeasurementType enum name
    val value: Double,
    val unit: String,
    val note: String?
)

// ─── Fertility ────────────────────────────────────────────────────────────

@Entity(
    tableName = "fertility_observation",
    foreignKeys = [
        ForeignKey(
            entity = JournalEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["journalEntryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("journalEntryId")]
)
data class FertilityObservationEntity(
    @PrimaryKey val id: String,
    val journalEntryId: String,
    val observationType: String,    // FertilityObservationType enum name
    val rawValueJson: String,
    val note: String?
)

@Entity(tableName = "fertility_estimate_settings")
data class FertilityEstimateSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val isEnabled: Boolean,
    val showOnCalendar: Boolean,
    val showReminders: Boolean,
    val methodVersion: String,
    val updatedAt: Instant
)

@Entity(
    tableName = "test_result",
    foreignKeys = [
        ForeignKey(
            entity = JournalEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["journalEntryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("journalEntryId")]
)
data class TestResultEntity(
    @PrimaryKey val id: String,
    val journalEntryId: String,
    val testType: String,           // TestType enum name
    val result: String,             // TestOutcome enum name
    val photoAttachmentId: String?,
    val note: String?
)

// ─── Pregnancy ────────────────────────────────────────────────────────────

@Entity(
    tableName = "pregnancy_profile",
    indices = [Index("status")]
)
data class PregnancyProfileEntity(
    @PrimaryKey val id: String,
    val displayName: String?,
    val estimatedDueDate: LocalDate,
    val lmpDate: LocalDate?,
    val dueDateSource: String,      // DueDateSource enum name
    val status: String,             // PregnancyStatus enum name
    val createdAt: Instant,
    val updatedAt: Instant,
    val note: String?
)

@Entity(
    tableName = "contraction_entry",
    foreignKeys = [
        ForeignKey(
            entity = PregnancyProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["pregnancyProfileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("pregnancyProfileId"), Index("startTime")]
)
data class ContractionEntryEntity(
    @PrimaryKey val id: String,
    val pregnancyProfileId: String,
    val startTime: Instant,
    val endTime: Instant?,
    val durationSeconds: Int?,
    val intervalFromPreviousSeconds: Int?,
    val intensityLevel: Int?,
    val note: String?
)

// ─── Postpartum ───────────────────────────────────────────────────────────

@Entity(tableName = "postpartum_profile")
data class PostpartumProfileEntity(
    @PrimaryKey val id: String,
    val deliveryDate: LocalDate,
    val linkedPregnancyProfileId: String?,
    val isBreastfeeding: Boolean,
    val status: String,             // TrackingStatus enum name
    val createdAt: Instant,
    val updatedAt: Instant
)

@Entity(
    tableName = "feeding_entry",
    foreignKeys = [
        ForeignKey(
            entity = PostpartumProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["postpartumProfileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("postpartumProfileId"), Index("startTime")]
)
data class FeedingEntryEntity(
    @PrimaryKey val id: String,
    val postpartumProfileId: String,
    val startTime: Instant,
    val endTime: Instant?,
    val feedingType: String,        // FeedingType enum name
    val sideOrNote: String?,
    val volumeMl: Int?
)

// ─── Daily health ─────────────────────────────────────────────────────────

@Entity(tableName = "daily_habit")
data class DailyHabitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val habitType: String,          // HabitType enum name
    val targetValue: Double?,
    val targetUnit: String?,
    val isActive: Boolean,
    val isPaused: Boolean,
    val createdAt: Instant
)

@Entity(
    tableName = "habit_log",
    foreignKeys = [
        ForeignKey(
            entity = DailyHabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("habitId"), Index("logDate")]
)
data class HabitLogEntity(
    @PrimaryKey val id: String,
    val habitId: String,
    val logDate: LocalDate,
    val value: Double?,
    val note: String?,
    val createdAt: Instant
)

// ─── Relaxation ───────────────────────────────────────────────────────────

@Entity(tableName = "relaxation_session")
data class RelaxationSessionEntity(
    @PrimaryKey val id: String,
    val sessionType: String,
    val startTime: Instant,
    val endTime: Instant?,
    val durationSeconds: Int,
    val moodBefore: Int?,
    val moodAfter: Int?,
    val note: String?
)

// ─── Medical records ──────────────────────────────────────────────────────

@Entity(
    tableName = "medical_visit",
    indices = [Index("visitDate")]
)
data class MedicalVisitEntity(
    @PrimaryKey val id: String,
    val visitDate: LocalDate,
    val visitTime: LocalTime?,
    val facility: String?,
    val doctor: String?,
    val preVisitQuestions: String?,
    val postVisitNotes: String?,
    val userConclusion: String?,
    val labelsJson: String,         // JSON array
    val createdAt: Instant,
    val updatedAt: Instant
)

@Entity(
    tableName = "medical_document",
    foreignKeys = [
        ForeignKey(
            entity = MedicalVisitEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicalVisitId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("medicalVisitId")]
)
data class MedicalDocumentEntity(
    @PrimaryKey val id: String,
    val medicalVisitId: String?,
    val displayName: String,
    val mimeType: String,
    val internalPath: String,       // relative to app files dir
    val fileSizeBytes: Long,
    val isEncrypted: Boolean,
    val createdAt: Instant
)

@Entity(tableName = "medication_schedule")
data class MedicationScheduleEntity(
    @PrimaryKey val id: String,
    val medicationName: String,
    val dosageNote: String?,
    val frequencyJson: String,      // JSON schedule descriptor
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val isActive: Boolean,
    val createdAt: Instant
)

// ─── Reminders ────────────────────────────────────────────────────────────

@Entity(
    tableName = "reminder",
    indices = [Index("reminderType"), Index("isActive"), Index("moduleId")]
)
data class ReminderEntity(
    @PrimaryKey val id: String,
    val reminderType: String,       // ReminderType enum name
    val title: String,
    val scheduledAt: Instant?,
    val repeatConfigJson: String?,  // JSON
    val isActive: Boolean,
    val moduleId: String?,
    val linkedEntityId: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)

// ─── Knowledge ────────────────────────────────────────────────────────────

@Entity(
    tableName = "knowledge_article",
    indices = [Index("category"), Index("moduleId"), Index("reviewStatus")]
)
data class KnowledgeArticleEntity(
    @PrimaryKey val id: String,
    val titleVi: String,
    val summaryVi: String,
    val contentVi: String,
    val category: String,
    val moduleId: String?,
    val sourceRef: String?,
    val reviewStatus: String,       // ReviewStatus enum name
    val reviewerNote: String?,
    val contentVersion: Int,
    val updatedAt: Instant
)

@Entity(
    tableName = "bookmark",
    foreignKeys = [
        ForeignKey(
            entity = KnowledgeArticleEntity::class,
            parentColumns = ["id"],
            childColumns = ["articleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("articleId", unique = true)]
)
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val articleId: String,
    val lastReadPosition: Int,
    val createdAt: Instant
)

// ─── Attachments ──────────────────────────────────────────────────────────

@Entity(
    tableName = "attachment",
    indices = [Index("ownerId"), Index("ownerType")]
)
data class AttachmentEntity(
    @PrimaryKey val id: String,
    val ownerType: String,
    val ownerId: String,
    val displayName: String,
    val mimeType: String,
    val internalPath: String,
    val fileSizeBytes: Long,
    val isEncrypted: Boolean,
    val createdAt: Instant
)
