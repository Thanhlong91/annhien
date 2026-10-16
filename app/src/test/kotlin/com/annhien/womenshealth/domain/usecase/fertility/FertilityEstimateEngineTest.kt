package com.annhien.womenshealth.domain.usecase.fertility

import com.annhien.womenshealth.domain.model.CycleSummary
import com.annhien.womenshealth.domain.model.LifeStage
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for FertilityEstimateEngine.
 *
 * Test coverage:
 * ✓ Stage = PREGNANCY → NotApplicable
 * ✓ Stage = PERIMENOPAUSE → NotApplicable
 * ✓ Stage = POSTPARTUM + breastfeeding → Ineligible
 * ✓ Feature disabled → Ineligible.FEATURE_DISABLED
 * ✓ Hormonal contraception active → Ineligible
 * ✓ <3 cycles → Ineligible.INSUFFICIENT_HISTORY
 * ✓ No recent period → Ineligible.NO_RECENT_PERIOD
 * ✓ Sufficient data → Eligible with non-null fertile window
 * ✓ Result NEVER contains "safe day" or guaranteed ovulation
 * ✓ Mandatory disclaimer is always present in Eligible result
 * ✓ Method version is set
 * ✓ Fertile window start <= ovulation <= fertile window end
 */
class FertilityEstimateEngineTest {

    private val lastPeriodStart = LocalDate.parse("2024-03-01")

    private val validSummaries = listOf(
        mockSummary(28),
        mockSummary(29),
        mockSummary(27)
    )

    private val enabledInput = FertilityEstimateInput(
        validCycleHistory = validSummaries,
        lastKnownPeriodStart = lastPeriodStart,
        healthStage = LifeStage.CYCLE,
        featureEnabled = true,
        isUsingHormonalContraception = false,
        isBreastfeeding = false
    )

    // ─── Stage tests ──────────────────────────────────────────────────────

    @Test
    fun `PREGNANCY stage returns NotApplicable`() {
        val result = FertilityEstimateEngine.estimate(
            enabledInput.copy(healthStage = LifeStage.PREGNANCY)
        )
        assertTrue(result is FertilityEstimateResult.NotApplicable)
        assertEquals(LifeStage.PREGNANCY, (result as FertilityEstimateResult.NotApplicable).stage)
    }

    @Test
    fun `PERIMENOPAUSE stage returns NotApplicable`() {
        val result = FertilityEstimateEngine.estimate(
            enabledInput.copy(healthStage = LifeStage.PERIMENOPAUSE)
        )
        assertTrue(result is FertilityEstimateResult.NotApplicable)
    }

    @Test
    fun `POSTPARTUM with breastfeeding returns Ineligible`() {
        val result = FertilityEstimateEngine.estimate(
            enabledInput.copy(healthStage = LifeStage.POSTPARTUM, isBreastfeeding = true)
        )
        assertTrue(result is FertilityEstimateResult.Ineligible)
        assertEquals(
            FertilityIneligibleReason.BREASTFEEDING_POSTPARTUM,
            (result as FertilityEstimateResult.Ineligible).reason
        )
    }

    // ─── Feature flag tests ───────────────────────────────────────────────

    @Test
    fun `feature disabled returns Ineligible with FEATURE_DISABLED reason`() {
        val result = FertilityEstimateEngine.estimate(
            enabledInput.copy(featureEnabled = false)
        )
        assertTrue(result is FertilityEstimateResult.Ineligible)
        assertEquals(
            FertilityIneligibleReason.FEATURE_DISABLED,
            (result as FertilityEstimateResult.Ineligible).reason
        )
    }

    @Test
    fun `hormonal contraception returns Ineligible`() {
        val result = FertilityEstimateEngine.estimate(
            enabledInput.copy(isUsingHormonalContraception = true)
        )
        assertTrue(result is FertilityEstimateResult.Ineligible)
        assertEquals(
            FertilityIneligibleReason.HORMONAL_CONTRACEPTION_ACTIVE,
            (result as FertilityEstimateResult.Ineligible).reason
        )
    }

    // ─── Insufficient data tests ──────────────────────────────────────────

    @Test
    fun `fewer than 3 cycles returns Ineligible INSUFFICIENT_HISTORY`() {
        val result = FertilityEstimateEngine.estimate(
            enabledInput.copy(validCycleHistory = listOf(mockSummary(28), mockSummary(29)))
        )
        assertTrue(result is FertilityEstimateResult.Ineligible)
        assertEquals(
            FertilityIneligibleReason.INSUFFICIENT_HISTORY,
            (result as FertilityEstimateResult.Ineligible).reason
        )
    }

    @Test
    fun `no recent period start returns Ineligible NO_RECENT_PERIOD`() {
        val result = FertilityEstimateEngine.estimate(
            enabledInput.copy(lastKnownPeriodStart = null)
        )
        assertTrue(result is FertilityEstimateResult.Ineligible)
        assertEquals(
            FertilityIneligibleReason.NO_RECENT_PERIOD,
            (result as FertilityEstimateResult.Ineligible).reason
        )
    }

    @Test
    fun `empty cycle history returns Ineligible`() {
        val result = FertilityEstimateEngine.estimate(
            enabledInput.copy(validCycleHistory = emptyList())
        )
        assertTrue(result is FertilityEstimateResult.Ineligible)
    }

    // ─── Successful estimation tests ──────────────────────────────────────

    @Test
    fun `valid input returns Eligible result with fertile window`() {
        val result = FertilityEstimateEngine.estimate(enabledInput)
        assertTrue(result is FertilityEstimateResult.Eligible)
    }

    @Test
    fun `fertile window start is before or equal to ovulation date`() {
        val result = FertilityEstimateEngine.estimate(enabledInput)
        val eligible = result as FertilityEstimateResult.Eligible
        assertTrue(
            "Fertile window start should be before ovulation",
            eligible.fertileWindowStart <= eligible.estimatedOvulationDate
        )
    }

    @Test
    fun `fertile window end is after or equal to ovulation date`() {
        val result = FertilityEstimateEngine.estimate(enabledInput)
        val eligible = result as FertilityEstimateResult.Eligible
        assertTrue(
            "Fertile window end should be on or after ovulation",
            eligible.fertileWindowEnd >= eligible.estimatedOvulationDate
        )
    }

    @Test
    fun `result NEVER contains safe day or certainty guarantee`() {
        val result = FertilityEstimateEngine.estimate(enabledInput)
        val resultStr = result.toString()
        assertFalse("No 'an toàn' in result", resultStr.lowercase().contains("an toàn"))
        assertFalse("No 'chắc chắn' in result", resultStr.lowercase().contains("chắc chắn"))
        assertFalse("No percentage confidence in result", resultStr.contains(Regex("\\d+%")))
    }

    @Test
    fun `mandatory disclaimer is always present in Eligible result`() {
        val result = FertilityEstimateEngine.estimate(enabledInput)
        val eligible = result as FertilityEstimateResult.Eligible
        assertTrue("Mandatory disclaimer must be set", eligible.mandatoryDisclaimer.isNotBlank())
        assertTrue(
            "Disclaimer must mention it cannot be used for unprotected intercourse",
            eligible.mandatoryDisclaimer.contains("không được dùng")
        )
    }

    @Test
    fun `method version is set and non-empty`() {
        val result = FertilityEstimateEngine.estimate(enabledInput)
        val eligible = result as FertilityEstimateResult.Eligible
        assertTrue(eligible.methodVersion.isNotBlank())
        assertEquals(FertilityEstimateEngine.METHOD_VERSION, eligible.methodVersion)
    }

    @Test
    fun `assumptions and limitations are non-empty`() {
        val result = FertilityEstimateEngine.estimate(enabledInput)
        val eligible = result as FertilityEstimateResult.Eligible
        assertTrue("Assumptions list must not be empty", eligible.assumptions.isNotEmpty())
        assertTrue("Limitations list must not be empty", eligible.limitations.isNotEmpty())
    }

    @Test
    fun `data used summary contains correct cycle count`() {
        val result = FertilityEstimateEngine.estimate(enabledInput)
        val eligible = result as FertilityEstimateResult.Eligible
        assertEquals(validSummaries.size, eligible.dataUsed.cycleCount)
        assertEquals(lastPeriodStart, eligible.dataUsed.lastPeriodStart)
    }

    @Test
    fun `high variability input still returns result but with additional warning`() {
        val highVariabilitySummaries = listOf(
            mockSummary(20),
            mockSummary(28),
            mockSummary(35) // variability = 15 > 10
        )
        val result = FertilityEstimateEngine.estimate(
            enabledInput.copy(validCycleHistory = highVariabilitySummaries)
        )
        // High variability still returns Eligible (user has data), but warnings should be present
        assertTrue(result is FertilityEstimateResult.Eligible)
        val eligible = result as FertilityEstimateResult.Eligible
        // Assumptions or limitations should mention the variability
        val allWarnings = (eligible.assumptions + eligible.limitations).joinToString()
        assertTrue("High variability should be mentioned", allWarnings.contains("dao động"))
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    private fun mockSummary(cycleLength: Int) = CycleSummary(
        episodeId = "test-${cycleLength}-${System.nanoTime()}",
        startDate = LocalDate.parse("2024-01-01"),
        endDate = LocalDate.parse("2024-01-05"),
        periodLength = 5,
        cycleLength = cycleLength,
        isUncertain = false
    )
}
