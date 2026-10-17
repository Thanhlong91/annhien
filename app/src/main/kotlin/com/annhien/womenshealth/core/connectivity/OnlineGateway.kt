package com.annhien.womenshealth.core.connectivity

import com.annhien.womenshealth.core.cache.OnlineCacheManager
import com.annhien.womenshealth.core.ratelimit.RateLimitManager
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnlineGateway @Inject constructor(
    private val connectivityChecker: ConnectivityChecker,
    private val rateLimitManager: RateLimitManager,
    private val cacheManager: OnlineCacheManager
) {

    fun <T> execute(
        sourceId: String,
        cacheKey: String,
        ttl: Duration? = null,
        now: Instant = Instant.now(),
        fetch: () -> T
    ): OnlineResult<T> {
        if (!connectivityChecker.isOnline()) {
            return OnlineResult.Error.Offline
        }

        rateLimitManager.clearExpired(now)

        if (!rateLimitManager.canUse(sourceId, now)) {
            return OnlineResult.Error.RateLimited
        }

        val cached = cacheManager.get<T>(cacheKey, now)
        if (cached != null) {
            return OnlineResult.Success(cached, fromCache = true)
        }

        return try {
            val data = fetch()
            cacheManager.put(cacheKey, data, ttl, now)
            rateLimitManager.markAvailable(sourceId)
            OnlineResult.Success(data)
        } catch (e: Exception) {
            OnlineResult.Error.Unknown(e.message)
        }
    }
}
