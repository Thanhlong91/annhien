package com.annhien.womenshealth.data.repository

import com.annhien.womenshealth.core.database.dao.BleedingObservationDao
import com.annhien.womenshealth.core.database.dao.PeriodEpisodeDao
import com.annhien.womenshealth.core.database.entity.BleedingObservationEntity
import com.annhien.womenshealth.core.database.entity.PeriodEpisodeEntity
import com.annhien.womenshealth.domain.model.BleedingObservation
import com.annhien.womenshealth.domain.model.FlowLevel
import com.annhien.womenshealth.domain.model.PeriodEpisode
import com.annhien.womenshealth.domain.repository.CycleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CycleRepositoryImpl @Inject constructor(
    private val periodEpisodeDao: PeriodEpisodeDao,
    private val bleedingObservationDao: BleedingObservationDao
) : CycleRepository {

    override fun observePeriodEpisodes(): Flow<List<PeriodEpisode>> {
        return periodEpisodeDao.observeAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getRecentEpisodes(limit: Int): List<PeriodEpisode> {
        return periodEpisodeDao.getRecent(limit).map { it.toDomain() }
    }

    override suspend fun recordPeriodStart(date: LocalDate, isConfirmed: Boolean): String {
        val id = UUID.randomUUID().toString()
        val now = Instant.now()
        val entity = PeriodEpisodeEntity(
            id = id,
            startDate = date,
            endDate = null,
            isOngoing = true,
            isUncertain = !isConfirmed,
            isMissedMarker = false,
            createdAt = now,
            updatedAt = now
        )
        periodEpisodeDao.insert(entity)
        return id
    }

    override suspend fun recordPeriodEnd(episodeId: String, endDate: LocalDate) {
        val existing = periodEpisodeDao.getById(episodeId) ?: return
        val updated = existing.copy(
            endDate = endDate,
            isOngoing = false,
            updatedAt = Instant.now()
        )
        periodEpisodeDao.update(updated)
    }

    override suspend fun recordPeriodRange(startDate: LocalDate, endDate: LocalDate, isConfirmed: Boolean): String {
        val id = UUID.randomUUID().toString()
        val now = Instant.now()
        val start = minOf(startDate, endDate)
        val end = maxOf(startDate, endDate)
        val entity = PeriodEpisodeEntity(
            id = id,
            startDate = start,
            endDate = end,
            isOngoing = false,
            isUncertain = !isConfirmed,
            isMissedMarker = false,
            createdAt = now,
            updatedAt = now
        )
        periodEpisodeDao.insert(entity)
        return id
    }

    override suspend fun togglePeriodDay(date: LocalDate) {
        val all = periodEpisodeDao.getRecent(100)
        val matching = all.find { ep ->
            val end = ep.endDate ?: (if (ep.isOngoing) LocalDate.now() else ep.startDate)
            !date.isBefore(ep.startDate) && !date.isAfter(end)
        }

        if (matching != null) {
            val end = matching.endDate ?: (if (matching.isOngoing) LocalDate.now() else matching.startDate)
            if (matching.startDate == end || (matching.startDate == date && end == date)) {
                // Single-day episode, delete it
                periodEpisodeDao.deleteById(matching.id)
            } else if (matching.startDate == date) {
                // Shorten from start
                periodEpisodeDao.update(matching.copy(startDate = date.plusDays(1), updatedAt = Instant.now()))
            } else if (end == date) {
                // Shorten from end
                periodEpisodeDao.update(matching.copy(endDate = date.minusDays(1), isOngoing = false, updatedAt = Instant.now()))
            } else {
                // Tapped inside a range, split into two or delete
                periodEpisodeDao.deleteById(matching.id)
            }
        } else {
            // Not in any episode. Check if can merge with neighbor
            val endsYesterday = all.find { (it.endDate ?: it.startDate) == date.minusDays(1) }
            val startsTomorrow = all.find { it.startDate == date.plusDays(1) }

            if (endsYesterday != null && startsTomorrow != null) {
                // Bridge between two episodes
                val merged = endsYesterday.copy(
                    endDate = startsTomorrow.endDate ?: startsTomorrow.startDate,
                    isOngoing = startsTomorrow.isOngoing,
                    updatedAt = Instant.now()
                )
                periodEpisodeDao.update(merged)
                periodEpisodeDao.deleteById(startsTomorrow.id)
            } else if (endsYesterday != null) {
                periodEpisodeDao.update(endsYesterday.copy(endDate = date, updatedAt = Instant.now()))
            } else if (startsTomorrow != null) {
                periodEpisodeDao.update(startsTomorrow.copy(startDate = date, updatedAt = Instant.now()))
            } else {
                // Create single day episode
                val now = Instant.now()
                val entity = PeriodEpisodeEntity(
                    id = UUID.randomUUID().toString(),
                    startDate = date,
                    endDate = date,
                    isOngoing = false,
                    isUncertain = false,
                    isMissedMarker = false,
                    createdAt = now,
                    updatedAt = now
                )
                periodEpisodeDao.insert(entity)
            }
        }
    }

    override suspend fun deleteEpisode(episodeId: String) {
        periodEpisodeDao.deleteById(episodeId)
    }

    override suspend fun saveBleedingObservation(observation: BleedingObservation) {
        val now = Instant.now()
        val entity = BleedingObservationEntity(
            id = observation.id.ifEmpty { UUID.randomUUID().toString() },
            eventDate = observation.eventDate,
            flowLevel = observation.flowLevel?.name,
            isOutOfCycle = observation.isOutOfCycle,
            periodEpisodeId = observation.periodEpisodeId,
            note = observation.note,
            createdAt = observation.createdAt,
            updatedAt = now
        )
        bleedingObservationDao.upsert(entity)
    }

    override fun observeBleedingByDate(date: LocalDate): Flow<List<BleedingObservation>> {
        return bleedingObservationDao.observeByDate(date.toString()).map { list ->
            list.map { entity ->
                BleedingObservation(
                    id = entity.id,
                    eventDate = entity.eventDate,
                    flowLevel = entity.flowLevel?.let { runCatching { FlowLevel.valueOf(it) }.getOrNull() },
                    isOutOfCycle = entity.isOutOfCycle,
                    periodEpisodeId = entity.periodEpisodeId,
                    note = entity.note,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    private fun PeriodEpisodeEntity.toDomain() = PeriodEpisode(
        id = id,
        startDate = startDate,
        endDate = endDate,
        isOngoing = isOngoing,
        isUncertain = isUncertain,
        isMissedMarker = isMissedMarker,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
