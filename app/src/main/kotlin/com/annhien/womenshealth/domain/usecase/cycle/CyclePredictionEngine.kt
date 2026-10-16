package com.annhien.womenshealth.domain.usecase.cycle

import com.annhien.womenshealth.domain.model.CycleSummary
import com.annhien.womenshealth.domain.model.PeriodEpisode
import java.time.LocalDate

/**
 * Cycle prediction engine for An Nhiên.
 *
 * DESIGN PRINCIPLES:
 * ─────────────────
 * 1. Tách khỏi UI — không có Android dependencies, hoàn toàn testable.
 * 2. Không đưa ngày giả khi thiếu dữ liệu.
 * 3. Không mặc định chu kỳ 28 ngày.
 * 4. Không dùng khoảng giữa hai kỳ bị bỏ sót làm chu kỳ bình thường.
 * 5. Kết quả gồm khoảng dự kiến VÀ dữ liệu đã dùng.
 * 6. Không gọi khoảng dao động lịch sử là "khoảng tin cậy y khoa".
 *
 * MINIMUM DATA THRESHOLD (product decision, v1.0):
 * ─────────────────────────────────────────────────
 * Cần ≥ 3 khoảng chu kỳ hợp lệ để tạo dự đoán.
 * "Hợp lệ" nghĩa là: cả hai đầu của khoảng đều đã biết,
 * không có kỳ bị bỏ sót (isMissedMarker = false),
 * và không được đánh dấu không chắc chắn (isUncertain = false).
 *
 * GHI CHÚ Y TẾ:
 * ─────────────
 * Phương pháp tính này dựa trên lịch sử ghi nhận của người dùng.
 * Đây KHÔNG phải phương pháp tránh thai được kiểm chứng y khoa.
 * Thời điểm thực tế có thể thay đổi đáng kể.
 */
object CyclePredictionEngine {

    /**
     * Minimum number of valid cycle intervals required before
     * the engine will produce a prediction.
     *
     * This threshold is a product decision for v1.0, not a medical guarantee.
     * It should be documented in PREDICTION_METHODS.md.
     */
    const val MIN_VALID_INTERVALS_FOR_PREDICTION = 3

    /**
     * Cycle lengths outside this range are excluded from the calculation
     * as they likely indicate data entry errors or unusual circumstances.
     * Range: 15–90 days. This is a conservative filter, not a medical threshold.
     */
    const val MIN_PLAUSIBLE_CYCLE_DAYS = 15
    const val MAX_PLAUSIBLE_CYCLE_DAYS = 90

    /**
     * Builds CycleSummary list from a list of PeriodEpisodes.
     *
     * Rules:
     * - Episodes must have a known endDate (not ongoing).
     * - Episodes marked as uncertain or missed markers are excluded from cycle length calculation.
     * - Cycle length = days from one episode's startDate to the next episode's startDate.
     * - Episodes are sorted by startDate ascending.
     */
    fun buildCycleSummaries(episodes: List<PeriodEpisode>): List<CycleSummary> {
        val sorted = episodes
            .filter { it.endDate != null && !it.isOngoing }
            .sortedBy { it.startDate }

        if (sorted.size < 2) return emptyList()

        val summaries = mutableListOf<CycleSummary>()

        for (i in 0 until sorted.size - 1) {
            val current = sorted[i]
            val next = sorted[i + 1]

            // Skip if either endpoint is uncertain
            if (current.isUncertain || current.isMissedMarker) continue
            // Skip if next episode is a missed marker (can't use this as a valid interval)
            if (next.isMissedMarker) continue

            val cycleLength = java.time.temporal.ChronoUnit.DAYS.between(
                current.startDate,
                next.startDate
            ).toInt()

            // Filter implausible lengths
            if (cycleLength < MIN_PLAUSIBLE_CYCLE_DAYS || cycleLength > MAX_PLAUSIBLE_CYCLE_DAYS) {
                continue
            }

            val periodLength = java.time.temporal.ChronoUnit.DAYS.between(
                current.startDate,
                current.endDate!!
            ).toInt() + 1

            summaries.add(
                CycleSummary(
                    episodeId = current.id,
                    startDate = current.startDate,
                    endDate = current.endDate,
                    periodLength = periodLength,
                    cycleLength = cycleLength,
                    isUncertain = current.isUncertain
                )
            )
        }

        return summaries
    }

    /**
     * Predicts the next period based on cycle history.
     *
     * @param lastPeriodStart The start date of the most recent period
     * @param validSummaries Valid cycle summaries (≥ MIN_VALID_INTERVALS)
     * @return PeriodPrediction or IneligibleReason if insufficient data
     */
    fun predictNextPeriod(
        lastPeriodStart: LocalDate,
        validSummaries: List<CycleSummary>
    ): CyclePredictionResult {
        if (validSummaries.size < MIN_VALID_INTERVALS_FOR_PREDICTION) {
            return CyclePredictionResult.Ineligible(
                reason = IneligibleReason.INSUFFICIENT_HISTORY,
                availableIntervals = validSummaries.size,
                requiredIntervals = MIN_VALID_INTERVALS_FOR_PREDICTION
            )
        }

        val cycleLengths = validSummaries.map { it.cycleLength }.sorted()
        val medianCycleLength = median(cycleLengths)
        val minCycleLength = cycleLengths.min()
        val maxCycleLength = cycleLengths.max()

        val predictedStart = lastPeriodStart.plusDays(medianCycleLength.toLong())
        val rangeStart = lastPeriodStart.plusDays(minCycleLength.toLong())
        val rangeEnd = lastPeriodStart.plusDays(maxCycleLength.toLong())

        return CyclePredictionResult.Eligible(
            prediction = PeriodPrediction(
                estimatedStartDate = predictedStart,
                earliestEstimate = rangeStart,
                latestEstimate = rangeEnd,
                basedOnCycleCount = validSummaries.size,
                medianCycleLengthDays = medianCycleLength,
                minCycleLengthDays = minCycleLength,
                maxCycleLengthDays = maxCycleLength,
                disclaimer = PREDICTION_DISCLAIMER,
                methodVersion = METHOD_VERSION
            ),
            dataUsed = validSummaries
        )
    }

    /**
     * Compute the median of a sorted list.
     * Uses whole-number median (floor of average for even count).
     */
    private fun median(sorted: List<Int>): Int {
        require(sorted.isNotEmpty())
        val mid = sorted.size / 2
        return if (sorted.size % 2 == 0) {
            (sorted[mid - 1] + sorted[mid]) / 2
        } else {
            sorted[mid]
        }
    }

    const val PREDICTION_DISCLAIMER =
        "Dự kiến dựa trên lịch sử bạn đã ghi. Thời điểm thực tế có thể thay đổi."

    /**
     * Version tag for the prediction method.
     * Increment when algorithm changes to allow result invalidation.
     */
    const val METHOD_VERSION = "cycle_median_v1"
}

// ─── Result types ─────────────────────────────────────────────────────────

sealed class CyclePredictionResult {
    data class Eligible(
        val prediction: PeriodPrediction,
        val dataUsed: List<CycleSummary>
    ) : CyclePredictionResult()

    data class Ineligible(
        val reason: IneligibleReason,
        val availableIntervals: Int,
        val requiredIntervals: Int
    ) : CyclePredictionResult()
}

data class PeriodPrediction(
    val estimatedStartDate: LocalDate,
    val earliestEstimate: LocalDate,
    val latestEstimate: LocalDate,
    val basedOnCycleCount: Int,
    val medianCycleLengthDays: Int,
    val minCycleLengthDays: Int,
    val maxCycleLengthDays: Int,
    val disclaimer: String,
    val methodVersion: String
)

enum class IneligibleReason(val messageVi: String) {
    INSUFFICIENT_HISTORY("Chưa đủ dữ liệu để ước tính. Cần thêm lịch sử chu kỳ."),
    NO_RECENT_PERIOD("Không xác định được kỳ kinh gần nhất."),
    HIGH_VARIABILITY("Chu kỳ biến động quá lớn để đưa ra ước tính đáng tin cậy."),
    STAGE_NOT_APPLICABLE("Tính năng này không áp dụng cho giai đoạn hiện tại."),
    FEATURE_DISABLED("Tính năng dự đoán đã được tắt."),
    PENDING_METHOD_APPROVAL("Phương pháp dự đoán đang chờ phê duyệt chuyên môn.")
}
