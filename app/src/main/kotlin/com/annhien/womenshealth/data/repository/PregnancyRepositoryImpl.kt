package com.annhien.womenshealth.data.repository

import com.annhien.womenshealth.core.database.dao.ContractionEntryDao
import com.annhien.womenshealth.core.database.dao.FeedingEntryDao
import com.annhien.womenshealth.core.database.dao.PostpartumProfileDao
import com.annhien.womenshealth.core.database.dao.PregnancyProfileDao
import com.annhien.womenshealth.core.database.entity.ContractionEntryEntity
import com.annhien.womenshealth.core.database.entity.FeedingEntryEntity
import com.annhien.womenshealth.core.database.entity.PostpartumProfileEntity
import com.annhien.womenshealth.core.database.entity.PregnancyProfileEntity
import com.annhien.womenshealth.domain.repository.PregnancyRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PregnancyRepositoryImpl @Inject constructor(
    private val pregnancyProfileDao: PregnancyProfileDao,
    private val contractionEntryDao: ContractionEntryDao,
    private val postpartumProfileDao: PostpartumProfileDao,
    private val feedingEntryDao: FeedingEntryDao
) : PregnancyRepository {

    override fun observeActivePregnancy(): Flow<PregnancyProfileEntity?> {
        return pregnancyProfileDao.observeActive()
    }

    override suspend fun savePregnancyProfile(profile: PregnancyProfileEntity) {
        pregnancyProfileDao.upsert(profile)
    }

    override suspend fun recordKick(pregnancyId: String) {
        // Increment kick count or record a kick marker
        val profile = pregnancyProfileDao.getActive() ?: return
        pregnancyProfileDao.update(profile.copy(updatedAt = Instant.now()))
    }

    override suspend fun recordContraction(
        pregnancyId: String,
        startEpochMs: Long,
        endEpochMs: Long,
        intensity: Int
    ) {
        val durationSeconds = ((endEpochMs - startEpochMs) / 1000).toInt()
        val entity = ContractionEntryEntity(
            id = UUID.randomUUID().toString(),
            pregnancyProfileId = pregnancyId,
            startTime = Instant.ofEpochMilli(startEpochMs),
            endTime = Instant.ofEpochMilli(endEpochMs),
            durationSeconds = durationSeconds,
            intervalFromPreviousSeconds = null,
            intensityLevel = intensity,
            note = null
        )
        contractionEntryDao.upsert(entity)
    }

    override fun observeContractions(pregnancyId: String): Flow<List<ContractionEntryEntity>> {
        return contractionEntryDao.observeRecent(pregnancyId)
    }

    override fun observeActivePostpartum(): Flow<PostpartumProfileEntity?> {
        return postpartumProfileDao.observeActive()
    }

    override suspend fun savePostpartumProfile(profile: PostpartumProfileEntity) {
        postpartumProfileDao.upsert(profile)
    }

    override suspend fun recordFeeding(entry: FeedingEntryEntity) {
        feedingEntryDao.upsert(entry)
    }

    override fun observeRecentFeedings(postpartumId: String): Flow<List<FeedingEntryEntity>> {
        return feedingEntryDao.observeRecent(postpartumId)
    }
}
