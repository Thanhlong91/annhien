package com.annhien.womenshealth.domain.model

import java.time.LocalDate
import java.time.LocalTime

// ─── Life stage & module ───────────────────────────────────────────────────

enum class LifeStage {
    CYCLE,          // Theo dõi chu kỳ kinh nguyệt
    CONCEPTION,     // Chuẩn bị mang thai
    PREGNANCY,      // Thai kỳ
    POSTPARTUM,     // Sau sinh
    PERIMENOPAUSE   // Tiền mãn kinh
}

enum class AppModule(val id: String, val displayNameVi: String) {
    CYCLE("cycle", "Chu kỳ kinh nguyệt"),
    CONCEPTION("conception", "Chuẩn bị mang thai"),
    PREGNANCY("pregnancy", "Thai kỳ"),
    POSTPARTUM("postpartum", "Sau sinh"),
    PERIMENOPAUSE("perimenopause", "Tiền mãn kinh"),
    DAILY_HEALTH("daily_health", "Sức khỏe hằng ngày"),
    RELAXATION("relaxation", "Thư giãn"),
    MEDICAL_RECORDS("medical_records", "Hồ sơ khám")
}

// ─── User profile ─────────────────────────────────────────────────────────

data class UserProfile(
    val id: Int = 1,
    val nickname: String?,
    val ageGroup: AgeGroup?,
    val dateOfBirth: LocalDate?,
    val primaryLifeStage: LifeStage,
    val temperatureUnit: TemperatureUnit,
    val weightUnit: WeightUnit,
    val liquidUnit: LiquidUnit,
    val appLockType: AppLockType,
    val isOnboardingComplete: Boolean
)

enum class AgeGroup(val labelVi: String) {
    GROUP_18_25("18–25"),
    GROUP_26_35("26–35"),
    GROUP_36_45("36–45"),
    GROUP_46_PLUS("46+"),
    PREFER_NOT_SAY("Không muốn cung cấp")
}

enum class TemperatureUnit(val symbol: String) {
    CELSIUS("°C"),
    FAHRENHEIT("°F")
}

enum class WeightUnit(val symbol: String) {
    KG("kg"),
    LBS("lbs")
}

enum class LiquidUnit(val symbol: String) {
    ML("ml"),
    OZ("oz")
}

enum class AppLockType {
    NONE,
    PIN,
    BIOMETRIC,
    PIN_AND_BIOMETRIC
}

// ─── Module preference ─────────────────────────────────────────────────────

data class ModulePreference(
    val moduleId: String,
    val isEnabled: Boolean
)

// ─── Life stage history ────────────────────────────────────────────────────

data class LifeStageTransition(
    val id: String,
    val fromStage: LifeStage?,
    val toStage: LifeStage,
    val confirmedAt: java.time.Instant,
    val note: String?
)

// ─── Period cycle ─────────────────────────────────────────────────────────

data class PeriodEpisode(
    val id: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val isOngoing: Boolean,
    val isUncertain: Boolean,
    val isMissedMarker: Boolean,
    val createdAt: java.time.Instant,
    val updatedAt: java.time.Instant
)

data class BleedingObservation(
    val id: String,
    val eventDate: LocalDate,
    val flowLevel: FlowLevel?,
    val isOutOfCycle: Boolean,
    val periodEpisodeId: String?,
    val note: String?,
    val createdAt: java.time.Instant
)

enum class FlowLevel(val labelVi: String, val intensity: Int) {
    SPOT("Rỉ rất ít", 1),
    LIGHT("Ít", 2),
    MEDIUM("Vừa", 3),
    HEAVY("Nhiều", 4),
    VERY_HEAVY("Rất nhiều", 5)
}

// ─── Cycle summary (for prediction) ───────────────────────────────────────

/**
 * Represents a completed cycle with known start and end.
 * A "valid" cycle is one where both startDate and endDate are known
 * and the episode is NOT marked as uncertain or missed.
 */
data class CycleSummary(
    val episodeId: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val periodLength: Int, // endDate - startDate + 1
    val cycleLength: Int,  // days from this start to next cycle start
    val isUncertain: Boolean
)

// ─── Symptoms ─────────────────────────────────────────────────────────────

data class SymptomDefinition(
    val id: String,
    val nameVi: String,
    val category: SymptomCategory,
    val isDefault: Boolean,
    val isActive: Boolean
)

enum class SymptomCategory(val labelVi: String) {
    PAIN("Đau"),
    DIGESTIVE("Tiêu hóa"),
    MOOD_ENERGY("Tâm trạng & Năng lượng"),
    SKIN_HAIR("Da & Tóc"),
    SLEEP("Giấc ngủ"),
    PHYSICAL("Thể chất"),
    MENOPAUSE("Tiền mãn kinh"),
    OTHER("Khác")
}

// ─── Journal ──────────────────────────────────────────────────────────────

data class JournalEntry(
    val id: String,
    val eventDate: LocalDate,
    val eventTime: LocalTime?,
    val moduleId: String?,
    val entryType: JournalEntryType,
    val note: String?,
    val createdAt: java.time.Instant,
    val updatedAt: java.time.Instant
)

enum class JournalEntryType(val labelVi: String) {
    SYMPTOM("Triệu chứng"),
    MOOD("Tâm trạng"),
    SLEEP("Giấc ngủ"),
    MEASUREMENT("Chỉ số đo"),
    FERTILITY_OBSERVATION("Quan sát thụ thai"),
    TEST_RESULT("Kết quả thử"),
    NOTE("Ghi chú"),
    MEDICATION("Thuốc"),
    ACTIVITY("Hoạt động"),
    FEEDING("Cho bú"),
    CONTRACTION("Cơn gò")
}

data class SymptomObservation(
    val id: String,
    val journalEntryId: String,
    val symptomDefinitionId: String,
    val symptomName: String, // denormalized for display
    val severity: Int?,      // 1-5
    val bodyLocation: String?,
    val note: String?
)

data class MoodEntry(
    val id: String,
    val journalEntryId: String,
    val moodLevel: Int, // 1-5
    val moodTags: List<String>,
    val note: String?
)

data class SleepEntry(
    val id: String,
    val journalEntryId: String,
    val sleepStart: java.time.Instant,
    val sleepEnd: java.time.Instant,
    val qualityLevel: Int?, // 1-5
    val note: String?
)

enum class MeasurementType(val labelVi: String, val unit: String) {
    WEIGHT("Cân nặng", "kg"),
    BBT("Nhiệt độ cơ thể lúc nghỉ (BBT)", "°C"),
    TEMPERATURE("Nhiệt độ", "°C"),
    BLOOD_PRESSURE_SYSTOLIC("Huyết áp tâm thu", "mmHg"),
    BLOOD_PRESSURE_DIASTOLIC("Huyết áp tâm trương", "mmHg"),
    WATER("Nước uống", "ml"),
    STEPS("Bước chân", "bước")
}

data class MeasurementEntry(
    val id: String,
    val journalEntryId: String,
    val measurementType: MeasurementType,
    val value: Double,
    val unit: String, // actual unit used (may differ from default)
    val note: String?
)

// ─── Fertility ────────────────────────────────────────────────────────────

data class FertilityObservation(
    val id: String,
    val journalEntryId: String,
    val observationType: FertilityObservationType,
    val rawValue: String, // JSON, type-specific
    val note: String?
)

enum class FertilityObservationType(val labelVi: String) {
    DISCHARGE("Dịch tiết"),
    OPK("Kết quả que thử rụng trứng"),
    BBT_SHIFT("Thay đổi nhiệt độ BBT")
}

data class FertilityEstimateSettings(
    val id: Int = 1,
    val isEnabled: Boolean,
    val showOnCalendar: Boolean,
    val showReminders: Boolean,
    val methodVersion: String,
    val updatedAt: java.time.Instant
)

data class TestResult(
    val id: String,
    val journalEntryId: String,
    val testType: TestType,
    val result: TestOutcome,
    val photoAttachmentId: String?,
    val note: String?
)

enum class TestType(val labelVi: String) {
    OPK("Que thử rụng trứng"),
    PREGNANCY("Que thử thai")
}

enum class TestOutcome(val labelVi: String) {
    POSITIVE("Dương tính"),
    NEGATIVE("Âm tính"),
    INVALID("Không rõ / Không hợp lệ")
}

// ─── Pregnancy ────────────────────────────────────────────────────────────

data class PregnancyProfile(
    val id: String,
    val displayName: String?,
    val estimatedDueDate: LocalDate,
    val lmpDate: LocalDate?,
    val dueDateSource: DueDateSource,
    val status: PregnancyStatus,
    val createdAt: java.time.Instant,
    val updatedAt: java.time.Instant,
    val note: String?
)

enum class DueDateSource(val labelVi: String) {
    LMP("Tính từ ngày kinh cuối"),
    ULTRASOUND("Siêu âm"),
    USER_ENTERED("Tự nhập")
}

enum class PregnancyStatus {
    ACTIVE,
    ARCHIVED
}

data class ContractionEntry(
    val id: String,
    val pregnancyProfileId: String,
    val startTime: java.time.Instant,
    val endTime: java.time.Instant?,
    val durationSeconds: Int?,
    val intervalFromPreviousSeconds: Int?,
    val intensityLevel: Int?, // 1-5
    val note: String?
)

// ─── Postpartum ───────────────────────────────────────────────────────────

data class PostpartumProfile(
    val id: String,
    val deliveryDate: LocalDate,
    val linkedPregnancyProfileId: String?,
    val isBreastfeeding: Boolean,
    val status: TrackingStatus,
    val createdAt: java.time.Instant,
    val updatedAt: java.time.Instant
)

enum class TrackingStatus {
    ACTIVE,
    PAUSED,
    ARCHIVED
}

data class FeedingEntry(
    val id: String,
    val postpartumProfileId: String,
    val startTime: java.time.Instant,
    val endTime: java.time.Instant?,
    val feedingType: FeedingType,
    val sideOrNote: String?,
    val volumeMl: Int?
)

enum class FeedingType(val labelVi: String) {
    BREASTFEED("Bú mẹ"),
    PUMP("Hút sữa"),
    FORMULA("Sữa công thức")
}

// ─── Daily health ─────────────────────────────────────────────────────────

data class DailyHabit(
    val id: String,
    val name: String,
    val habitType: HabitType,
    val targetValue: Double?,
    val targetUnit: String?,
    val isActive: Boolean,
    val isPaused: Boolean,
    val createdAt: java.time.Instant
)

enum class HabitType(val labelVi: String) {
    WATER("Nước uống"),
    EXERCISE("Vận động"),
    SLEEP("Giấc ngủ"),
    MEDICATION("Thuốc"),
    CUSTOM("Tùy chỉnh")
}

data class HabitLog(
    val id: String,
    val habitId: String,
    val logDate: LocalDate,
    val value: Double?,
    val note: String?,
    val createdAt: java.time.Instant
)

// ─── Relaxation ───────────────────────────────────────────────────────────

data class RelaxationSession(
    val id: String,
    val sessionType: String,
    val startTime: java.time.Instant,
    val endTime: java.time.Instant?,
    val durationSeconds: Int,
    val moodBefore: Int?,
    val moodAfter: Int?,
    val note: String?
)

// ─── Medical records ──────────────────────────────────────────────────────

data class MedicalVisit(
    val id: String,
    val visitDate: LocalDate,
    val visitTime: LocalTime?,
    val facility: String?,
    val doctor: String?,
    val preVisitQuestions: String?,
    val postVisitNotes: String?,
    val userConclusion: String?,
    val labels: List<String>,
    val createdAt: java.time.Instant,
    val updatedAt: java.time.Instant
)

data class MedicalDocument(
    val id: String,
    val medicalVisitId: String?,
    val displayName: String,
    val mimeType: String,
    val internalPath: String,
    val fileSizeBytes: Long,
    val isEncrypted: Boolean,
    val createdAt: java.time.Instant
)

data class MedicationSchedule(
    val id: String,
    val medicationName: String,
    val dosageNote: String?,
    val frequency: String, // JSON schedule descriptor
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val isActive: Boolean,
    val createdAt: java.time.Instant
)

// ─── Reminders ────────────────────────────────────────────────────────────

data class Reminder(
    val id: String,
    val reminderType: ReminderType,
    val title: String,
    val scheduledAt: java.time.Instant?,
    val repeatConfig: String?, // JSON
    val isActive: Boolean,
    val moduleId: String?,
    val linkedEntityId: String?,
    val createdAt: java.time.Instant,
    val updatedAt: java.time.Instant
)

enum class ReminderType(val labelVi: String) {
    JOURNAL("Ghi nhật ký"),
    PERIOD("Kỳ kinh"),
    FERTILE_WINDOW("Khoảng dễ thụ thai"),
    MEDICAL_VISIT("Lịch khám"),
    MEDICATION("Thuốc"),
    HABIT("Thói quen"),
    RELAXATION("Thư giãn"),
    BACKUP("Sao lưu")
}

// ─── Knowledge ────────────────────────────────────────────────────────────

data class KnowledgeArticle(
    val id: String,
    val titleVi: String,
    val summaryVi: String,
    val contentVi: String,
    val category: String,
    val moduleId: String?,
    val sourceRef: String?,
    val reviewStatus: ReviewStatus,
    val reviewerNote: String?,
    val contentVersion: Int,
    val updatedAt: java.time.Instant
)

enum class ReviewStatus(val labelVi: String) {
    DRAFT("Bản thảo"),
    PENDING_REVIEW("Chờ rà soát"),
    APPROVED("Đã duyệt")
}

data class Bookmark(
    val id: String,
    val articleId: String,
    val lastReadPosition: Int,
    val createdAt: java.time.Instant
)

// ─── Attachments ──────────────────────────────────────────────────────────

data class Attachment(
    val id: String,
    val ownerType: String,
    val ownerId: String,
    val displayName: String,
    val mimeType: String,
    val internalPath: String,
    val fileSizeBytes: Long,
    val isEncrypted: Boolean,
    val createdAt: java.time.Instant
)

// ─── Offline Specialized Health Modules ──────────────────────────────────

data class PillPack28Status(
    val id: String,
    val packStartDate: LocalDate,
    val totalPills: Int = 28,
    val activePills: Int = 21,
    val placeboPills: Int = 7,
    val takenPillsMap: Map<Int, Boolean>, // Pill day 1..28 -> taken
    val brandName: String?,
    val dailyReminderTime: LocalTime?,
    val updatedAt: java.time.Instant
)

data class KegelSession(
    val id: String,
    val date: LocalDate,
    val repetitionsCompleted: Int,
    val targetRepetitions: Int = 10,
    val contractionSeconds: Int = 5,
    val relaxationSeconds: Int = 5,
    val durationMinutes: Int,
    val createdAt: java.time.Instant
)

data class BreastSelfExamRecord(
    val id: String,
    val date: LocalDate,
    val daysAfterPeriodEnded: Int?,
    val findingsNoted: Boolean,
    val findingsDescription: String?,
    val nextRecommendedDate: LocalDate,
    val createdAt: java.time.Instant
)

data class EpdsScreening(
    val id: String,
    val date: LocalDate,
    val answers: List<Int>, // 10 answers, 0-3 points each
    val totalScore: Int,
    val riskCategory: EpdsRiskCategory,
    val question10Score: Int, // Item 10: Thoughts of harming oneself
    val notes: String?,
    val createdAt: java.time.Instant
)

enum class EpdsRiskCategory(val labelVi: String, val clinicalAdviceVi: String) {
    LOW_RISK("Nguy cơ thấp (0–9 điểm)", "Tâm trạng trong giới hạn bình thường. Tiếp tục duy trì lối sống lành mạnh và tự quan tâm bản thân."),
    MODERATE_RISK("Nguy cơ trung bình (10–12 điểm)", "Có dấu hiệu căng thẳng hoặc lo âu hậu sản. Nên theo dõi lại sau 1-2 tuần và chia sẻ với người thân."),
    HIGH_RISK("Nguy cơ cao (13+ điểm)", "Khả năng cao mắc trầm cảm sau sinh. Khuyến cáo nên đi khám chuyên khoa tâm lý/tâm thần sớm để được hỗ trợ chuyên sâu.")
}

