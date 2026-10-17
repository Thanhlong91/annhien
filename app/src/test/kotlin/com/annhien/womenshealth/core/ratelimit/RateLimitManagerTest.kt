package com.annhien.womenshealth.core.ratelimit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class RateLimitManagerTest {

    private val manager = RateLimitManager()

    @Test
    fun `source is available by default`() {
        assertTrue(manager.canUse("openfoodfacts"))
    }

    @Test
    fun `limited source cannot be used before reset time`() {
        val now = Instant.parse("2026-10-17T00:00:00Z")
        val resetAt = Instant.parse("2026-10-18T00:00:00Z")

        manager.markLimited("openfoodfacts", resetAt, null)

        assertFalse(manager.canUse("openfoodfacts", now))
    }

    @Test
    fun `limited source becomes available after reset time`() {
        val resetAt = Instant.parse("2026-10-18T00:00:00Z")
        val later = Instant.parse("2026-10-18T00:01:00Z")

        manager.markLimited("openfoodfacts", resetAt, null)
        manager.clearExpired(later)

        assertTrue(manager.canUse("openfoodfacts", later))
        assertEquals(RateLimitState.AVAILABLE, manager.getStatus("openfoodfacts").state)
    }

    @Test
    fun `disabled source cannot be used`() {
        manager.markDisabled("openaq", null)

        assertFalse(manager.canUse("openaq"))
        assertEquals(RateLimitState.DISABLED, manager.getStatus("openaq").state)
    }
}
