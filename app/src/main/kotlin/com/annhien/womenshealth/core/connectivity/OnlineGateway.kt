package com.annhien.womenshealth.core.connectivity

import com.annhien.womenshealth.core.cache.OnlineCacheManager
import com.annhien.womenshealth.core.ratelimit.RateLimitManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
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

    suspend fun <T> execute(
        sourceId: String,
        cacheKey: String,
        ttl: Duration? = null,
        now: Instant = Instant.now(),
        fetch: suspend () -> T
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
            val data = withContext(Dispatchers.IO) {
                fetch()
            }
            cacheManager.put(cacheKey, data, ttl, now)
            rateLimitManager.markAvailable(sourceId)
            OnlineResult.Success(data)
        } catch (e: HttpException) {
            when (e.code()) {
                401, 403 -> OnlineResult.Error.InvalidApiKey
                404 -> OnlineResult.Error.NotFound
                429 -> {
                    val retryAfterSeconds = e.response()?.headers()?.get("Retry-After")?.toLongOrNull()
                    rateLimitManager.markLimitedFromHttp(
                        sourceId = sourceId,
                        retryAfterSeconds = retryAfterSeconds,
                        now = now
                    )
                    OnlineResult.Error.RateLimited
                }
                in 500..599 -> OnlineResult.Error.ServiceUnavailable
                else -> OnlineResult.Error.Unknown("Lỗi kết nối máy chủ (${e.code()}): ${e.message()}")
            }
        } catch (e: IOException) {
            OnlineResult.Error.Offline
        } catch (e: Exception) {
            OnlineResult.Error.Unknown(e.message)
        }
    }
}
