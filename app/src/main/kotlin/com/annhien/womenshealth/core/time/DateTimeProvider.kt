package com.annhien.womenshealth.core.time

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Abstraction over time to enable deterministic testing.
 * Inject this instead of calling LocalDate.now() or Instant.now() directly.
 */
interface DateTimeProvider {
    fun now(): Instant
    fun today(): LocalDate
    fun currentDateTime(): LocalDateTime
    fun zoneId(): ZoneId
}

/**
 * Production implementation backed by the system clock.
 */
class SystemDateTimeProvider(
    private val clock: Clock = Clock.systemDefaultZone()
) : DateTimeProvider {

    override fun now(): Instant = Instant.now(clock)

    override fun today(): LocalDate = LocalDate.now(clock)

    override fun currentDateTime(): LocalDateTime = LocalDateTime.now(clock)

    override fun zoneId(): ZoneId = clock.zone
}

/**
 * Test implementation with a fixed point in time.
 */
class FixedDateTimeProvider(
    private val fixedInstant: Instant,
    private val zone: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh")
) : DateTimeProvider {

    override fun now(): Instant = fixedInstant

    override fun today(): LocalDate = fixedInstant.atZone(zone).toLocalDate()

    override fun currentDateTime(): LocalDateTime = fixedInstant.atZone(zone).toLocalDateTime()

    override fun zoneId(): ZoneId = zone
}
