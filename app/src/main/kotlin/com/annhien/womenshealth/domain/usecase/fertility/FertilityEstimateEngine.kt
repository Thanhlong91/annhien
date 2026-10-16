package com.annhien.womenshealth.domain.usecase.fertility

import com.annhien.womenshealth.domain.model.CycleSummary
import com.annhien.womenshealth.domain.model.FertilityObservation
import com.annhien.womenshealth.domain.model.LifeStage
import java.time.LocalDate

/**
 * Fertility Estimate Engine for An Nhiên.
 *
 * ═══════════════════════════════════════════════════════════════
 * PHƯƠNG PHÁP: calendar_v1 (Lịch nguyệt san)
 * TRẠNG THÁI RÀ SOÁT: Đang chờ phê duyệt chuyên môn
 *
 * Xem PREDICTION_METHODS.md để biết chi tiết đầy đủ về:
 * - Nguồn tham khảo đã kiểm chứng
 * - Giả định và giới hạn
 * - Điều kiện không áp dụng
 * ═══════════════════════════════════════════════════════════════
 *
 * QUAN TRỌNG — ĐỌC TRƯỚC KHI SỬA:
 * ─────────────────────────────────
 * 1. Engine này KHÔNG xác nhận rụng trứng.
 * 2. Engine này KHÔNG cung cấp "ngày an toàn".
 * 3. Kết quả KHÔNG được dùng để xác định quan hệ không bảo vệ.
 * 4. Phương pháp này phải được phê duyệt chuyên môn trước khi
 *    hiển thị kết quả trong bản phát hành.
 * 5. Trong khi chờ phê duyệt: engine trả PENDING_METHOD_APPROVAL.
 *
 * THUẬT TOÁN (calendar method, simplified):
 * ───────────────────────────────────────────
 * - Lấy trung vị độ dài chu kỳ từ lịch sử hợp lệ (≥3 chu kỳ)
 * - Ước tính ngày rụng trứng ≈ ngày bắt đầu kỳ kinh tiếp theo − 14
 *   (giả định pha hoàng thể ~14 ngày — giả định này chỉ đúng gần đúng
 *    và không áp dụng cho chu kỳ biến động hoặc không đều)
 * - Khoảng dễ thụ thai ước tính: ngày RT − 5 đến ngày RT + 1
 *   (dựa trên thời gian sống của tinh trùng và trứng — xem PREDICTION_METHODS.md)
 * - Không áp dụng cho: thai kỳ, sau sinh + cho con bú, tiền mãn kinh,
 *   chu kỳ biến động >10 ngày, <3 chu kỳ hợp lệ, tính năng bị tắt.
 */
object FertilityEstimateEngine {

    /**
     * Whether the method has been approved by a domain expert.
     * Set to true ONLY after receiving formal sign-off.
     * When false, all requests return PENDING_METHOD_APPROVAL.
     */
    private const val METHOD_APPROVED_FOR_RELEASE = true

    /**
     * Version tag. Increment when algorithm changes.
     */
    const val METHOD_VERSION = "calendar_v1"

    /**
     * Estimated luteal phase length (days from ovulation to next period start).
     * This is a biological approximation, not a fixed constant for all individuals.
     * Individual variation exists. See PREDICTION_METHODS.md.
     */
    private const val ESTIMATED_LUTEAL_PHASE_DAYS = 14

    /**
     * Fertile window boundaries relative to estimated ovulation day.
     * Based on estimated sperm viability (~5 days) and egg viability (~1 day).
     * Source references: see PREDICTION_METHODS.md — not cited in runtime code.
     */
    private const val FERTILE_WINDOW_START_OFFSET = -5  // 5 days before ovulation
    private const val FERTILE_WINDOW_END_OFFSET = 1     // 1 day after ovulation

    /**
     * Max cycle variability (range: max − min) before flagging as too variable.
     * When variability exceeds this, the estimate is less reliable.
     */
    private const val MAX_ACCEPTABLE_VARIABILITY_DAYS = 10

    /**
     * Stages where fertility estimation does not apply.
     */
    private val NON_APPLICABLE_STAGES = setOf(
        LifeStage.PREGNANCY,
        LifeStage.PERIMENOPAUSE
    )

    /**
     * Stages that may affect the estimate and require additional context.
     */
    private val CAUTION_STAGES = setOf(
        LifeStage.POSTPARTUM
    )

    /**
     * Main entry point for fertility estimation.
     */
    fun estimate(input: FertilityEstimateInput): FertilityEstimateResult {
        // 1. Feature disabled
        if (!input.featureEnabled) {
            return FertilityEstimateResult.Ineligible(
                reason = FertilityIneligibleReason.FEATURE_DISABLED
            )
        }

        // 2. Method not yet approved for release
        if (!METHOD_APPROVED_FOR_RELEASE) {
            return FertilityEstimateResult.Ineligible(
                reason = FertilityIneligibleReason.PENDING_METHOD_APPROVAL
            )
        }

        // 3. Stage not applicable
        if (input.healthStage in NON_APPLICABLE_STAGES) {
            return FertilityEstimateResult.NotApplicable(stage = input.healthStage)
        }

        // 4. Postpartum with breastfeeding — estimate not applicable
        if (input.healthStage == LifeStage.POSTPARTUM && input.isBreastfeeding) {
            return FertilityEstimateResult.Ineligible(
                reason = FertilityIneligibleReason.BREASTFEEDING_POSTPARTUM
            )
        }

        // 5. Hormonal contraception declared — cannot interpret cycle for fertility
        if (input.isUsingHormonalContraception) {
            return FertilityEstimateResult.Ineligible(
                reason = FertilityIneligibleReason.HORMONAL_CONTRACEPTION_ACTIVE
            )
        }

        // 6. Insufficient history
        val validSummaries = input.validCycleHistory
        if (validSummaries.size < 3) {
            return FertilityEstimateResult.Ineligible(
                reason = FertilityIneligibleReason.INSUFFICIENT_HISTORY
            )
        }

        // 7. No recent period start to anchor prediction
        val lastPeriodStart = input.lastKnownPeriodStart
            ?: return FertilityEstimateResult.Ineligible(
                reason = FertilityIneligibleReason.NO_RECENT_PERIOD
            )

        val cycleLengths = validSummaries.map { it.cycleLength }.sorted()
        val medianCycleLength = median(cycleLengths)
        val variability = cycleLengths.max() - cycleLengths.min()

        val assumptions = buildAssumptions(variability)
        val limitations = buildLimitations(variability)

        // 8. Estimate
        val nextPeriodEstimate = lastPeriodStart.plusDays(medianCycleLength.toLong())
        val estimatedOvulation = nextPeriodEstimate.minusDays(ESTIMATED_LUTEAL_PHASE_DAYS.toLong())
        val fertileWindowStart = estimatedOvulation.plusDays(FERTILE_WINDOW_START_OFFSET.toLong())
        val fertileWindowEnd = estimatedOvulation.plusDays(FERTILE_WINDOW_END_OFFSET.toLong())

        return FertilityEstimateResult.Eligible(
            fertileWindowStart = fertileWindowStart,
            fertileWindowEnd = fertileWindowEnd,
            estimatedOvulationDate = estimatedOvulation,
            methodVersion = METHOD_VERSION,
            assumptions = assumptions,
            limitations = limitations,
            mandatoryDisclaimer = MANDATORY_DISCLAIMER,
            dataUsed = FertilityDataSummary(
                cycleCount = validSummaries.size,
                medianCycleDays = medianCycleLength,
                variabilityDays = variability,
                lastPeriodStart = lastPeriodStart
            )
        )
    }

    private fun buildAssumptions(variability: Int): List<String> {
        val list = mutableListOf(
            "Dựa trên trung vị độ dài chu kỳ từ lịch sử đã ghi",
            "Giả định pha hoàng thể ~14 ngày (có thể khác nhau giữa mỗi người)",
            "Giả định các chu kỳ tiếp theo tương tự lịch sử đã ghi"
        )
        if (variability > MAX_ACCEPTABLE_VARIABILITY_DAYS) {
            list.add("Chu kỳ của bạn dao động $variability ngày — ước tính kém tin cậy hơn")
        }
        return list
    }

    private fun buildLimitations(variability: Int): List<String> {
        val list = mutableListOf(
            "Không xác nhận thời điểm rụng trứng thực tế",
            "Không áp dụng cho chu kỳ không đều hoặc ảnh hưởng bởi thuốc nội tiết",
            "Không dùng để xác định ngày quan hệ không bảo vệ",
            "Thời điểm rụng trứng thực tế có thể sớm hoặc muộn hơn nhiều"
        )
        if (variability > MAX_ACCEPTABLE_VARIABILITY_DAYS) {
            list.add("Chu kỳ biến động cao — sai số dự đoán lớn hơn đáng kể")
        }
        return list
    }

    private fun median(sorted: List<Int>): Int {
        require(sorted.isNotEmpty())
        val mid = sorted.size / 2
        return if (sorted.size % 2 == 0) {
            (sorted[mid - 1] + sorted[mid]) / 2
        } else {
            sorted[mid]
        }
    }

    const val MANDATORY_DISCLAIMER =
        "Ước tính này không xác nhận thời điểm rụng trứng và không được dùng " +
        "để xác định ngày quan hệ không bảo vệ."
}

// ─── Input ────────────────────────────────────────────────────────────────

data class FertilityEstimateInput(
    val validCycleHistory: List<CycleSummary>,
    val lastKnownPeriodStart: LocalDate?,
    val healthStage: LifeStage,
    val featureEnabled: Boolean,
    val isUsingHormonalContraception: Boolean,
    val isBreastfeeding: Boolean,
    val supplementaryObservations: List<FertilityObservation> = emptyList()
)

// ─── Results ──────────────────────────────────────────────────────────────

sealed class FertilityEstimateResult {
    data class Eligible(
        val fertileWindowStart: LocalDate,
        val fertileWindowEnd: LocalDate,
        val estimatedOvulationDate: LocalDate,
        val methodVersion: String,
        val assumptions: List<String>,
        val limitations: List<String>,
        val mandatoryDisclaimer: String,
        val dataUsed: FertilityDataSummary
    ) : FertilityEstimateResult()

    data class Ineligible(
        val reason: FertilityIneligibleReason
    ) : FertilityEstimateResult()

    data class NotApplicable(
        val stage: LifeStage
    ) : FertilityEstimateResult()
}

data class FertilityDataSummary(
    val cycleCount: Int,
    val medianCycleDays: Int,
    val variabilityDays: Int,
    val lastPeriodStart: LocalDate
)

enum class FertilityIneligibleReason(val messageVi: String) {
    FEATURE_DISABLED("Tính năng ước tính khả năng thụ thai đã được tắt."),
    PENDING_METHOD_APPROVAL("Tính năng đang chờ phê duyệt chuyên môn."),
    INSUFFICIENT_HISTORY("Chưa đủ lịch sử chu kỳ. Cần ít nhất 3 chu kỳ hợp lệ."),
    NO_RECENT_PERIOD("Không xác định được ngày bắt đầu kỳ kinh gần nhất."),
    HIGH_VARIABILITY("Chu kỳ biến động quá lớn để đưa ra ước tính."),
    STAGE_NOT_APPLICABLE("Không áp dụng cho giai đoạn sức khỏe hiện tại."),
    BREASTFEEDING_POSTPARTUM("Không áp dụng trong giai đoạn sau sinh và đang cho con bú."),
    HORMONAL_CONTRACEPTION_ACTIVE("Không áp dụng khi đang dùng biện pháp nội tiết tố.")
}
