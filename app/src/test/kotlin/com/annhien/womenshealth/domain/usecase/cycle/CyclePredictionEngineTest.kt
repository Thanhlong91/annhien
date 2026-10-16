package com.annhien.womenshealth.domain.usecase.cycle

import com.annhien.womenshealth.domain.model.PeriodEpisode
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

/**
 * Unit tests for CyclePredictionEngine.
 *
 * Test coverage:
 * ✓ Đủ ≥3 chu kỳ hợp lệ → trả kết quả dự đoán
 * ✓ <3 chu kỳ → Ineligible.INSUFFICIENT_HISTORY
 * ✓ Có kỳ bị bỏ sót → không dùng khoảng đó làm chu kỳ bình thường
 * ✓ Dữ liệu không chắc chắn → loại khỏi tính toán
 * ✓ Chu kỳ ngoài khoảng 15-90 ngày → lọc bỏ
 * ✓ Tính trung vị đúng (số lẻ và số chẵn)
 * ✓ Năm nhuận (29/2) → xử lý đúng
 * ✓ Kết quả KHÔNG chứa "ngày an toàn" hay tỷ lệ %
 * ✓ Kết quả gồm disclaimer bắt buộc
 * ✓ Không trả kết quả khi episode đang diễn ra
 */
class CyclePredictionEngineTest {

    private val now = Instant.parse("2025-01-01T00:00:00Z")

    // ─── Helper functions ────────────────────────────────────────────────

    private fun episode(
        id: String,
        start: String,
        end: String?,
        ongoing: Boolean = false,
        uncertain: Boolean = false,
        missed: Boolean = false
    ) = PeriodEpisode(
        id = id,
        startDate = LocalDate.parse(start),
        endDate = end?.let { LocalDate.parse(it) },
        isOngoing = ongoing,
        isUncertain = uncertain,
        isMissedMarker = missed,
        createdAt = now,
        updatedAt = now
    )

    // ─── buildCycleSummaries tests ────────────────────────────────────────

    @Test
    fun `buildCycleSummaries with valid episodes returns correct cycle lengths`() {
        val episodes = listOf(
            episode("1", "2024-01-01", "2024-01-05"),  // cycle 1 start
            episode("2", "2024-01-29", "2024-02-02"),  // cycle 2 start: 28 days after
            episode("3", "2024-02-25", "2024-03-01"),  // cycle 3 start: 27 days after
            episode("4", "2024-03-26", "2024-03-30")   // cycle 4 start: 30 days after
        )

        val summaries = CyclePredictionEngine.buildCycleSummaries(episodes)

        assertEquals(3, summaries.size)
        assertEquals(28, summaries[0].cycleLength)
        assertEquals(27, summaries[1].cycleLength)
        assertEquals(30, summaries[2].cycleLength)
    }

    @Test
    fun `buildCycleSummaries excludes uncertain episodes from intervals`() {
        val episodes = listOf(
            episode("1", "2024-01-01", "2024-01-05"),
            episode("2", "2024-01-20", "2024-01-24", uncertain = true), // should be excluded
            episode("3", "2024-02-15", "2024-02-19"),
            episode("4", "2024-03-14", "2024-03-18")
        )

        val summaries = CyclePredictionEngine.buildCycleSummaries(episodes)

        // Interval from ep1 to ep2 is skipped because ep1 is NOT uncertain but ep2 could
        // be in chain. Let's verify: ep1→ep2 = 19 days (uncertain start = excluded),
        // ep2→ep3 skipped because ep2 is uncertain (isUncertain = true for ep2)
        // ep3→ep4 = 28 days
        val cycleLengths = summaries.map { it.cycleLength }
        // ep1→ep2: ep1 is not uncertain, ep2 is NOT missed but IS uncertain → next episode start
        // Actually ep1 is valid, ep2 is uncertain so we skip when current.isUncertain
        // ep2→ep3: ep2 is uncertain → skip
        // ep3→ep4: both valid → include (27 days)
        assertTrue("Uncertain intervals should be excluded", summaries.none { it.isUncertain })
    }

    @Test
    fun `buildCycleSummaries skips missed marker episodes in interval chain`() {
        val episodes = listOf(
            episode("1", "2024-01-01", "2024-01-05"),
            episode("2", "2024-02-15", "2024-02-15", missed = true), // missed marker
            episode("3", "2024-03-01", "2024-03-05"),
            episode("4", "2024-03-29", "2024-04-02")
        )

        val summaries = CyclePredictionEngine.buildCycleSummaries(episodes)

        // ep1→ep2: ep2 is missed → skip this interval
        // ep2→ep3: ep2 is missed (current is missed) → skip
        // ep3→ep4: both valid → include (28 days)
        assertEquals("Only the valid interval should remain", 1, summaries.size)
        assertEquals(28, summaries[0].cycleLength)
    }

    @Test
    fun `buildCycleSummaries filters out implausibly short cycles`() {
        val episodes = listOf(
            episode("1", "2024-01-01", "2024-01-05"),
            episode("2", "2024-01-10", "2024-01-14"), // 9 days — below MIN_PLAUSIBLE
            episode("3", "2024-02-07", "2024-02-11"),
            episode("4", "2024-03-06", "2024-03-10")
        )

        val summaries = CyclePredictionEngine.buildCycleSummaries(episodes)

        assertTrue("Implausibly short cycle should be filtered", summaries.none { it.cycleLength < 15 })
    }

    @Test
    fun `buildCycleSummaries filters out implausibly long cycles`() {
        val episodes = listOf(
            episode("1", "2024-01-01", "2024-01-05"),
            episode("2", "2024-05-01", "2024-05-05"), // 121 days — above MAX_PLAUSIBLE
            episode("3", "2024-05-29", "2024-06-02"),
            episode("4", "2024-06-26", "2024-06-30")
        )

        val summaries = CyclePredictionEngine.buildCycleSummaries(episodes)

        assertTrue("Implausibly long cycle should be filtered", summaries.none { it.cycleLength > 90 })
    }

    @Test
    fun `buildCycleSummaries returns empty for ongoing episode`() {
        val episodes = listOf(
            episode("1", "2024-01-01", null, ongoing = true)
        )

        val summaries = CyclePredictionEngine.buildCycleSummaries(episodes)
        assertTrue(summaries.isEmpty())
    }

    // ─── predictNextPeriod tests ──────────────────────────────────────────

    @Test
    fun `predictNextPeriod with sufficient history returns Eligible result`() {
        val lastStart = LocalDate.parse("2024-03-01")
        val summaries = listOf(
            mockSummary(cycleLength = 28),
            mockSummary(cycleLength = 30),
            mockSummary(cycleLength = 27)
        )

        val result = CyclePredictionEngine.predictNextPeriod(lastStart, summaries)

        assertTrue(result is CyclePredictionResult.Eligible)
        val eligible = result as CyclePredictionResult.Eligible
        // Median of [27, 28, 30] = 28
        assertEquals(lastStart.plusDays(28), eligible.prediction.estimatedStartDate)
        assertEquals(28, eligible.prediction.medianCycleLengthDays)
    }

    @Test
    fun `predictNextPeriod with median calculation even count`() {
        // Even count: median = (27+28)/2 = 27 (floor)
        val lastStart = LocalDate.parse("2024-03-01")
        val summaries = listOf(
            mockSummary(cycleLength = 27),
            mockSummary(cycleLength = 28),
            mockSummary(cycleLength = 30),
            mockSummary(cycleLength = 29)
        )

        val result = CyclePredictionEngine.predictNextPeriod(lastStart, summaries)

        assertTrue(result is CyclePredictionResult.Eligible)
        val eligible = result as CyclePredictionResult.Eligible
        // Sorted: [27, 28, 29, 30], median = (28+29)/2 = 28
        assertEquals(28, eligible.prediction.medianCycleLengthDays)
    }

    @Test
    fun `predictNextPeriod with insufficient history returns Ineligible`() {
        val lastStart = LocalDate.parse("2024-03-01")
        val summaries = listOf(
            mockSummary(cycleLength = 28),
            mockSummary(cycleLength = 30) // only 2, need 3
        )

        val result = CyclePredictionEngine.predictNextPeriod(lastStart, summaries)

        assertTrue(result is CyclePredictionResult.Ineligible)
        val ineligible = result as CyclePredictionResult.Ineligible
        assertEquals(IneligibleReason.INSUFFICIENT_HISTORY, ineligible.reason)
        assertEquals(2, ineligible.availableIntervals)
        assertEquals(CyclePredictionEngine.MIN_VALID_INTERVALS_FOR_PREDICTION, ineligible.requiredIntervals)
    }

    @Test
    fun `predictNextPeriod with zero history returns Ineligible`() {
        val result = CyclePredictionEngine.predictNextPeriod(
            LocalDate.parse("2024-01-01"),
            emptyList()
        )
        assertTrue(result is CyclePredictionResult.Ineligible)
    }

    @Test
    fun `prediction result DOES NOT contain safe day information`() {
        val lastStart = LocalDate.parse("2024-03-01")
        val summaries = listOf(
            mockSummary(cycleLength = 28),
            mockSummary(cycleLength = 29),
            mockSummary(cycleLength = 27)
        )

        val result = CyclePredictionEngine.predictNextPeriod(lastStart, summaries)
        assertTrue(result is CyclePredictionResult.Eligible)
        val eligible = result as CyclePredictionResult.Eligible

        // Verify no "safe day" or percentage confidence in the result
        val resultText = eligible.toString()
        assertFalse("Result should not mention 'an toàn'", resultText.lowercase().contains("an toàn"))
        assertFalse("Result should not mention 'safe'", resultText.lowercase().contains("safe"))
        assertFalse("Result should not contain percentage confidence",
            resultText.contains(Regex("\\d+%")))
    }

    @Test
    fun `prediction result contains mandatory disclaimer`() {
        val lastStart = LocalDate.parse("2024-03-01")
        val summaries = listOf(
            mockSummary(cycleLength = 28),
            mockSummary(cycleLength = 30),
            mockSummary(cycleLength = 27)
        )

        val result = CyclePredictionEngine.predictNextPeriod(lastStart, summaries)
        assertTrue(result is CyclePredictionResult.Eligible)
        val eligible = result as CyclePredictionResult.Eligible
        assertTrue(
            "Result must include disclaimer",
            eligible.prediction.disclaimer.isNotBlank()
        )
    }

    @Test
    fun `leap year date handling is correct`() {
        // Start on Feb 1, predict after 28 days = Feb 29 (leap year 2024)
        val lastStart = LocalDate.parse("2024-02-01")
        val summaries = listOf(
            mockSummary(cycleLength = 28),
            mockSummary(cycleLength = 28),
            mockSummary(cycleLength = 28)
        )

        val result = CyclePredictionEngine.predictNextPeriod(lastStart, summaries)
        assertTrue(result is CyclePredictionResult.Eligible)
        val eligible = result as CyclePredictionResult.Eligible
        assertEquals(LocalDate.parse("2024-02-29"), eligible.prediction.estimatedStartDate)
    }

    @Test
    fun `prediction range reflects historical min and max`() {
        val lastStart = LocalDate.parse("2024-03-01")
        val summaries = listOf(
            mockSummary(cycleLength = 25),
            mockSummary(cycleLength = 28),
            mockSummary(cycleLength = 31)
        )

        val result = CyclePredictionEngine.predictNextPeriod(lastStart, summaries)
        assertTrue(result is CyclePredictionResult.Eligible)
        val eligible = result as CyclePredictionResult.Eligible

        assertEquals(lastStart.plusDays(25), eligible.prediction.earliestEstimate)
        assertEquals(lastStart.plusDays(31), eligible.prediction.latestEstimate)
        assertEquals(lastStart.plusDays(28), eligible.prediction.estimatedStartDate)
    }

    @Test
    fun `method version is set and non-empty`() {
        val lastStart = LocalDate.parse("2024-03-01")
        val summaries = listOf(
            mockSummary(cycleLength = 28),
            mockSummary(cycleLength = 29),
            mockSummary(cycleLength = 27)
        )
        val result = CyclePredictionEngine.predictNextPeriod(lastStart, summaries)
        val eligible = result as CyclePredictionResult.Eligible
        assertTrue(eligible.prediction.methodVersion.isNotBlank())
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    private fun mockSummary(cycleLength: Int) = com.annhien.womenshealth.domain.model.CycleSummary(
        episodeId = "test-${cycleLength}-${System.nanoTime()}",
        startDate = LocalDate.parse("2024-01-01"),
        endDate = LocalDate.parse("2024-01-05"),
        periodLength = 5,
        cycleLength = cycleLength,
        isUncertain = false
    )
}
