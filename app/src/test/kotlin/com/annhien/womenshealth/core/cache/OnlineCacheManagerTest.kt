package com.annhien.womenshealth.core.cache

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Duration
import java.time.Instant

class OnlineCacheManagerTest {

    private val manager = OnlineCacheManager()

    @Test
    fun `cache returns stored value before expiry`() {
        val now = Instant.parse("2026-10-17T00:00:00Z")

        manager.put("key", "value", Duration.ofHours(1), now)

        assertEquals("value", manager.get<String>("key", now.plusMinutes(30)))
    }

    @Test
    fun `cache returns null after expiry`() {
        val now = Instant.parse("2026-10-17T00:00:00Z")

        manager.put("key", "value", Duration.ofHours(1), now)

        assertNull(manager.get<String>("key", now.plusHours(2)))
    }

    @Test
    fun `clearExpired removes only expired entries`() {
        val now = Instant.parse("2026-10-17T00:00:00Z")

        manager.put("expired", "old", Duration.ofMinutes(10), now)
        manager.put("active", "new", Duration.ofHours(1), now)
        manager.clearExpired(now.plusMinutes(20))

        assertNull(manager.get<String>("expired", now.plusMinutes(20)))
        assertEquals("new", manager.get<String>("active", now.plusMinutes(20)))
    }
}
