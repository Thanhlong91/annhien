package com.annhien.womenshealth.core.connectivity

import com.annhien.womenshealth.core.cache.OnlineCacheManager
import com.annhien.womenshealth.core.ratelimit.RateLimitManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class OnlineGatewayTest {

    private val connectivityChecker: ConnectivityChecker = mockk()
    private val rateLimitManager: RateLimitManager = mockk()
    private val cacheManager: OnlineCacheManager = mockk()

    private val gateway = OnlineGateway(
        connectivityChecker = connectivityChecker,
        rateLimitManager = rateLimitManager,
        cacheManager = cacheManager
    )

    @Test
    fun `returns offline when connectivity is unavailable`() {
        every { connectivityChecker.isOnline() } returns false

        val result = gateway.execute(
            sourceId = "openfoodfacts",
            cacheKey = "key"
        ) { "network" }

        assertTrue(result is OnlineResult.Error.Offline)
    }

    @Test
    fun `returns rate limited when source cannot be used`() {
        val now = Instant.parse("2026-10-17T00:00:00Z")

        every { connectivityChecker.isOnline() } returns true
        every { rateLimitManager.clearExpired(now) } returns Unit
        every { rateLimitManager.canUse("openfoodfacts", now) } returns false

        val result = gateway.execute(
            sourceId = "openfoodfacts",
            cacheKey = "key",
            now = now
        ) { "network" }

        assertTrue(result is OnlineResult.Error.RateLimited)
    }

    @Test
    fun `returns cached value without calling fetch`() {
        val now = Instant.parse("2026-10-17T00:00:00Z")

        every { connectivityChecker.isOnline() } returns true
        every { rateLimitManager.clearExpired(now) } returns Unit
        every { rateLimitManager.canUse("openfoodfacts", now) } returns true
        every { cacheManager.get<String>("key", now) } returns "cached"

        var fetchCalled = false

        val result = gateway.execute(
            sourceId = "openfoodfacts",
            cacheKey = "key",
            now = now
        ) {
            fetchCalled = true
            "network"
        }

        assertEquals("cached", (result as OnlineResult.Success).data)
        assertTrue(result.fromCache)
        assertFalse(fetchCalled)
    }

    @Test
    fun `fetches stores cache and marks source available`() {
        val now = Instant.parse("2026-10-17T00:00:00Z")

        every { connectivityChecker.isOnline() } returns true
        every { rateLimitManager.clearExpired(now) } returns Unit
        every { rateLimitManager.canUse("openfoodfacts", now) } returns true
        every { cacheManager.get<String>("key", now) } returns null
        every { cacheManager.put("key", "network", null, now) } returns Unit
        every { rateLimitManager.markAvailable("openfoodfacts") } returns Unit

        val result = gateway.execute(
            sourceId = "openfoodfacts",
            cacheKey = "key",
            now = now
        ) { "network" }

        assertEquals("network", (result as OnlineResult.Success).data)
        assertFalse(result.fromCache)
        verify(exactly = 1) { cacheManager.put("key", "network", null, now) }
        verify(exactly = 1) { rateLimitManager.markAvailable("openfoodfacts") }
    }
}
