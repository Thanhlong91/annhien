package com.annhien.womenshealth.core.cache

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

data class CacheEntry<T>(
    val value: T,
    val updatedAt: Instant,
    val expiresAt: Instant?,
    val typeName: String? = null
)

@Singleton
class OnlineCacheManager @Inject constructor() {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    @PublishedApi
    internal val cache = MutableStateFlow<Map<String, CacheEntry<Any>>>(emptyMap())

    val snapshot: StateFlow<Map<String, CacheEntry<Any>>> = cache.asStateFlow()

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String, now: Instant = Instant.now()): T? {
        val entry = cache.value[key] ?: return null
        val expired = entry.expiresAt != null && now.isAfter(entry.expiresAt)
        if (expired) return null
        return try {
            entry.value as? T
        } catch (_: ClassCastException) {
            null
        }
    }

    inline fun <reified T : Any> getTyped(key: String, now: Instant = Instant.now()): T? {
        val entry = cache.value[key] ?: return null
        val expired = entry.expiresAt != null && now.isAfter(entry.expiresAt)
        if (expired) return null
        val value = entry.value
        return if (T::class.java.isInstance(value)) {
            value as T
        } else {
            null
        }
    }

    fun <T> getSerializable(
        key: String,
        deserializer: KSerializer<T>,
        now: Instant = Instant.now()
    ): T? {
        val entry = cache.value[key] ?: return null
        val expired = entry.expiresAt != null && now.isAfter(entry.expiresAt)
        if (expired) return null
        val raw = entry.value
        return try {
            when (raw) {
                is String -> json.decodeFromString(deserializer, raw)
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun <T> putSerializable(
        key: String,
        value: T,
        serializer: KSerializer<T>,
        ttl: Duration? = null,
        now: Instant = Instant.now()
    ) {
        val jsonString = json.encodeToString(serializer, value)
        put(key, jsonString, ttl, now)
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
            expiresAt = ttl?.let { now.plus(it) },
            typeName = value?.let { it::class.qualifiedName }
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
