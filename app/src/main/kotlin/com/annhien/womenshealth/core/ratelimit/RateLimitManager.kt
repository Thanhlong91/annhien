package com.annhien.womenshealth.core.ratelimit

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

enum class RateLimitState {
    AVAILABLE,
    LIMITED,
    DISABLED
}

data class RateLimitStatus(
    val state: RateLimitState = RateLimitState.AVAILABLE,
    val resetAt: Instant? = null,
    val message: String? = null
)

@Singleton
class RateLimitManager @Inject constructor() {

    private val statuses = MutableStateFlow<Map<String, RateLimitStatus>>(emptyMap())

    val statusBySource: StateFlow<Map<String, RateLimitStatus>> = statuses.asStateFlow()

    fun getStatus(sourceId: String): RateLimitStatus {
        return statuses.value[sourceId] ?: RateLimitStatus()
    }

    fun markLimited(sourceId: String, resetAt: Instant?, message: String?) {
        statuses.value = statuses.value + (
            sourceId to RateLimitStatus(
                state = RateLimitState.LIMITED,
                resetAt = resetAt,
                message = message ?: "Tiện ích này đã đạt giới hạn sử dụng. Vui lòng thử lại sau."
            )
        )
    }

    fun markAvailable(sourceId: String) {
        statuses.value = statuses.value + (
            sourceId to RateLimitStatus(state = RateLimitState.AVAILABLE)
        )
    }

    fun markDisabled(sourceId: String, message: String?) {
        statuses.value = statuses.value + (
            sourceId to RateLimitStatus(
                state = RateLimitState.DISABLED,
                message = message ?: "Tiện ích này hiện không khả dụng."
            )
        )
    }

    fun canUse(sourceId: String, now: Instant = Instant.now()): Boolean {
        val status = getStatus(sourceId)
        return when (status.state) {
            RateLimitState.AVAILABLE -> true
            RateLimitState.DISABLED -> false
            RateLimitState.LIMITED -> {
                val resetAt = status.resetAt
                resetAt == null || now.isAfter(resetAt)
            }
        }
    }

    fun clearExpired(now: Instant = Instant.now()) {
        val updated = statuses.value.mapValues { (_, status) ->
            if (status.state == RateLimitState.LIMITED && status.resetAt != null && now.isAfter(status.resetAt)) {
                status.copy(state = RateLimitState.AVAILABLE, resetAt = null, message = null)
            } else {
                status
            }
        }
        statuses.value = updated
    }
}
