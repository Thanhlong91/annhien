package com.annhien.womenshealth.core.cache

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

data class CacheEntry<T>(
    val value: T,
    val updatedAt: Instant,
    val expiresAt: Instant?
)

@Singleton
class OnlineCacheManager @Inject constructor() {

    private val cache = MutableStateFlow<Map<String, CacheEntry<Any>>>(emptyMap())

    val snapshot: StateFlow<Map<String, CacheEntry<Any>>> = cache.asStateFlow()

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String, now: Instant = Instant.now()): T? {
        val entry = cache.value[key] ?: return null
        val expired = entry.expiresAt != null && now.isAfter(entry.expiresAt)
        return if (expired) null else entry.value as? T
    }

    fun <T> put(
        key: String,
        value: T,
        ttl: Duration? = null,
        now: Instant = Instant.now()
    ) {
        val entry = CacheEntry(
            value = value as Any,
            updatedAt = now,
            expiresAt = ttl?.let { now.plus(it) }
        )
        cache.value = cache.value + (key to entry)
    }

    fun remove(key: String) {
        cache.value = cache.value - key
    }

    fun clear() {
        cache.value = emptyMap()
    }

    fun clearExpired(now: Instant = Instant.now()) {
        cache.value = cache.value.filterValues { entry ->
            entry.expiresAt == null || now.isBefore(entry.expiresAt)
        }
    }
}
