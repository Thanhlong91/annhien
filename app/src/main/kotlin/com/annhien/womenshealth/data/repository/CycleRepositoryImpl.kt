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

    override suspend fun deleteEpisode(episodeId: String) {
        periodEpisodeDao.deleteById(episodeId)
    }

    override suspend fun saveBleedingObservation(observation: BleedingObservation) {
        val entity = BleedingObservationEntity(
            id = observation.id.ifEmpty { UUID.randomUUID().toString() },
            eventDate = observation.eventDate,
            flowLevel = observation.flowLevel?.name,
            isOutOfCycle = observation.isOutOfCycle,
            periodEpisodeId = observation.periodEpisodeId,
            note = observation.note,
            createdAt = observation.createdAt
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
