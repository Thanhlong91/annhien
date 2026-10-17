package com.annhien.womenshealth.data.repository

import com.annhien.womenshealth.core.database.dao.*
import com.annhien.womenshealth.core.database.entity.*
import com.annhien.womenshealth.domain.model.*
import com.annhien.womenshealth.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JournalRepositoryImpl @Inject constructor(
    private val journalEntryDao: JournalEntryDao,
    private val symptomObservationDao: SymptomObservationDao,
    private val moodEntryDao: MoodEntryDao,
    private val sleepEntryDao: SleepEntryDao,
    private val measurementEntryDao: MeasurementEntryDao
) : JournalRepository {

    override fun observeEntriesForMonth(year: Int, month: Int): Flow<List<JournalEntry>> {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.plusMonths(1).minusDays(1)
        return journalEntryDao.observeInRange(startDate.toString(), endDate.toString()).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeEntryByDate(date: LocalDate): Flow<JournalEntry?> {
        return journalEntryDao.observeInRange(date.toString(), date.toString()).map { list ->
            list.firstOrNull()?.toDomain()
        }
    }

    override suspend fun getEntryByDate(date: LocalDate): JournalEntry? {
        val list = journalEntryDao.getByRange(date.toString(), date.toString())
        return list.firstOrNull()?.toDomain()
    }

    override suspend fun saveJournalEntry(entry: JournalEntry): String {
        val id = if (entry.id.isNotEmpty()) entry.id else UUID.randomUUID().toString()
        val now = Instant.now()
        val entity = JournalEntryEntity(
            id = id,
            eventDate = entry.eventDate,
            eventTime = entry.eventTime,
            moduleId = entry.moduleId,
            entryType = entry.entryType.name,
            note = entry.note,
            createdAt = if (entry.id.isNotEmpty()) entry.createdAt else now,
            updatedAt = now
        )
        journalEntryDao.upsert(entity)
        return id
    }

    override suspend fun deleteJournalEntry(entryId: String) {
        val entity = journalEntryDao.getById(entryId)
        if (entity != null) {
            journalEntryDao.delete(entity)
        }
    }

    override suspend fun getSymptomObservations(entryId: String): List<SymptomObservation> {
        return symptomObservationDao.getByEntry(entryId).map { entity ->
            SymptomObservation(
                id = entity.id,
                journalEntryId = entity.journalEntryId,
                symptomDefinitionId = entity.symptomDefinitionId,
                symptomName = entity.symptomDefinitionId,
                severity = entity.severity,
                bodyLocation = entity.bodyLocation,
                note = entity.note
            )
        }
    }

    override suspend fun saveSymptomObservations(entryId: String, observations: List<SymptomObservation>) {
        // Clear previous observations for this entry
        symptomObservationDao.deleteByEntry(entryId)
        val entities = observations.map { obs ->
            SymptomObservationEntity(
                id = if (obs.id.isNotEmpty()) obs.id else UUID.randomUUID().toString(),
                journalEntryId = entryId,
                symptomDefinitionId = obs.symptomDefinitionId,
                severity = obs.severity,
                bodyLocation = obs.bodyLocation,
                note = obs.note
            )
        }
        entities.forEach { symptomObservationDao.upsert(it) }
    }

    override suspend fun getMoodEntry(entryId: String): MoodEntry? {
        val entity = moodEntryDao.getByEntry(entryId) ?: return null
        val tags = if (entity.moodTagsJson.isNotEmpty()) {
            entity.moodTagsJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        } else emptyList()
        return MoodEntry(
            id = entity.id,
            journalEntryId = entity.journalEntryId,
            moodLevel = entity.moodLevel,
            moodTags = tags,
            note = entity.note
        )
    }

    override suspend fun saveMoodEntry(mood: MoodEntry) {
        val entity = MoodEntryEntity(
            id = if (mood.id.isNotEmpty()) mood.id else UUID.randomUUID().toString(),
            journalEntryId = mood.journalEntryId,
            moodLevel = mood.moodLevel,
            moodTagsJson = mood.moodTags.joinToString(","),
            note = mood.note
        )
        moodEntryDao.upsert(entity)
    }

    override suspend fun getSleepEntry(entryId: String): SleepEntry? {
        val entity = sleepEntryDao.getByEntry(entryId).firstOrNull() ?: return null
        return SleepEntry(
            id = entity.id,
            journalEntryId = entity.journalEntryId,
            sleepStart = entity.sleepStart,
            sleepEnd = entity.sleepEnd,
            qualityLevel = entity.qualityLevel,
            note = entity.note
        )
    }

    override suspend fun saveSleepEntry(sleep: SleepEntry) {
        val entity = SleepEntryEntity(
            id = if (sleep.id.isNotEmpty()) sleep.id else UUID.randomUUID().toString(),
            journalEntryId = sleep.journalEntryId,
            sleepStart = sleep.sleepStart,
            sleepEnd = sleep.sleepEnd,
            qualityLevel = sleep.qualityLevel,
            note = sleep.note
        )
        sleepEntryDao.upsert(entity)
    }

    override suspend fun getMeasurements(entryId: String): List<MeasurementEntry> {
        return measurementEntryDao.getByEntry(entryId).map { entity ->
            MeasurementEntry(
                id = entity.id,
                journalEntryId = entity.journalEntryId,
                measurementType = runCatching { MeasurementType.valueOf(entity.measurementType) }.getOrDefault(MeasurementType.WEIGHT),
                value = entity.value,
                unit = entity.unit,
                note = entity.note
            )
        }
    }

    override suspend fun saveMeasurement(measurement: MeasurementEntry) {
        val entity = MeasurementEntryEntity(
            id = if (measurement.id.isNotEmpty()) measurement.id else UUID.randomUUID().toString(),
            journalEntryId = measurement.journalEntryId,
            measurementType = measurement.measurementType.name,
            value = measurement.value,
            unit = measurement.unit,
            note = measurement.note
        )
        measurementEntryDao.upsert(entity)
    }

    override suspend fun searchNotes(query: String): List<JournalEntry> {
        return journalEntryDao.searchByNote(query).map { it.toDomain() }
    }

    private fun JournalEntryEntity.toDomain() = JournalEntry(
        id = id,
        eventDate = eventDate,
        eventTime = eventTime,
        moduleId = moduleId,
        entryType = runCatching { JournalEntryType.valueOf(entryType) }.getOrDefault(JournalEntryType.NOTE),
        note = note,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
