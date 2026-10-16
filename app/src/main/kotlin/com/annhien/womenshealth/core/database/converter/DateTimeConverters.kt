package com.annhien.womenshealth.core.database.converter

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Room TypeConverters for java.time types.
 * Uses epoch-based storage for Instant, ISO strings for LocalDate/LocalTime.
 * These ensure dates are stored in a timezone-independent way — LocalDate
 * is stored as ISO string "yyyy-MM-dd" so it never shifts due to timezone.
 */
class DateTimeConverters {

    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString() // ISO "yyyy-MM-dd"

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? = value?.toString() // ISO "HH:mm:ss"

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }
}
