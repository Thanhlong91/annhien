package com.annhien.womenshealth.core.connectivity

import com.annhien.womenshealth.core.cache.OnlineCacheManager
import com.annhien.womenshealth.core.ratelimit.RateLimitManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
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
    fun `returns offline when connectivity is unavailable`() = runTest {
        every { connectivityChecker.isOnline() } returns false

        val result = gateway.execute(
            sourceId = "openfoodfacts",
            cacheKey = "key"
        ) { "network" }

        assertTrue(result is OnlineResult.Error.Offline)
    }

    @Test
    fun `returns rate limited when source cannot be used`() = runTest {
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
    fun `returns cached value without calling fetch`() = runTest {
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
    fun `fetches stores cache and marks source available`() = runTest {
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

    @Test
    fun `maps HTTP 401 and 403 to InvalidApiKey`() = runTest {
        val now = Instant.parse("2026-10-17T00:00:00Z")
        every { connectivityChecker.isOnline() } returns true
        every { rateLimitManager.clearExpired(now) } returns Unit
        every { rateLimitManager.canUse("openaq", now) } returns true
        every { cacheManager.get<String>("key", now) } returns null

        val httpException401 = HttpException(
            Response.error<String>(
                401,
                "Unauthorized".toResponseBody("text/plain".toMediaType())
            )
        )

        val result401 = gateway.execute(
            sourceId = "openaq",
            cacheKey = "key",
            now = now
        ) { throw httpException401 }

        assertTrue(result401 is OnlineResult.Error.InvalidApiKey)

        val httpException403 = HttpException(
            Response.error<String>(
                403,
                "Forbidden".toResponseBody("text/plain".toMediaType())
            )
        )

        val result403 = gateway.execute(
            sourceId = "openaq",
            cacheKey = "key",
            now = now
        ) { throw httpException403 }

        assertTrue(result403 is OnlineResult.Error.InvalidApiKey)
    }

    @Test
    fun `maps HTTP 404 to NotFound`() = runTest {
        val now = Instant.parse("2026-10-17T00:00:00Z")
        every { connectivityChecker.isOnline() } returns true
        every { rateLimitManager.clearExpired(now) } returns Unit
        every { rateLimitManager.canUse("openaq", now) } returns true
        every { cacheManager.get<String>("key", now) } returns null

        val httpException404 = HttpException(
            Response.error<String>(
                404,
                "Not Found".toResponseBody("text/plain".toMediaType())
            )
        )

        val result = gateway.execute(
            sourceId = "openaq",
            cacheKey = "key",
            now = now
        ) { throw httpException404 }

        assertTrue(result is OnlineResult.Error.NotFound)
    }

    @Test
    fun `maps HTTP 429 to RateLimited and calls markLimitedFromHttp`() = runTest {
        val now = Instant.parse("2026-10-17T00:00:00Z")
        every { connectivityChecker.isOnline() } returns true
        every { rateLimitManager.clearExpired(now) } returns Unit
        every { rateLimitManager.canUse("openaq", now) } returns true
        every { cacheManager.get<String>("key", now) } returns null
        every { rateLimitManager.markLimitedFromHttp("openaq", 60L, any(), now) } returns Unit

        val rawResponse = okhttp3.Response.Builder()
            .code(429)
            .message("Too Many Requests")
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("https://api.openaq.org").build())
            .header("Retry-After", "60")
            .build()

        val httpException429 = HttpException(
            Response.error<String>(
                "Too Many Requests".toResponseBody("text/plain".toMediaType()),
                rawResponse
            )
        )

        val result = gateway.execute(
            sourceId = "openaq",
            cacheKey = "key",
            now = now
        ) { throw httpException429 }

        assertTrue(result is OnlineResult.Error.RateLimited)
        verify(exactly = 1) { rateLimitManager.markLimitedFromHttp("openaq", 60L, any(), now) }
    }

    @Test
    fun `maps HTTP 500 to ServiceUnavailable`() = runTest {
        val now = Instant.parse("2026-10-17T00:00:00Z")
        every { connectivityChecker.isOnline() } returns true
        every { rateLimitManager.clearExpired(now) } returns Unit
        every { rateLimitManager.canUse("openaq", now) } returns true
        every { cacheManager.get<String>("key", now) } returns null

        val httpException500 = HttpException(
            Response.error<String>(
                500,
                "Server Error".toResponseBody("text/plain".toMediaType())
            )
        )

        val result = gateway.execute(
            sourceId = "openaq",
            cacheKey = "key",
            now = now
        ) { throw httpException500 }

        assertTrue(result is OnlineResult.Error.ServiceUnavailable)
    }

    @Test
    fun `maps IOException to Offline`() = runTest {
        val now = Instant.parse("2026-10-17T00:00:00Z")
        every { connectivityChecker.isOnline() } returns true
        every { rateLimitManager.clearExpired(now) } returns Unit
        every { rateLimitManager.canUse("openaq", now) } returns true
        every { cacheManager.get<String>("key", now) } returns null

        val result = gateway.execute(
            sourceId = "openaq",
            cacheKey = "key",
            now = now
        ) { throw IOException("Connection reset") }

        assertTrue(result is OnlineResult.Error.Offline)
    }
}
