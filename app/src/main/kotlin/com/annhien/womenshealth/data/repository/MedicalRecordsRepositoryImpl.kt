package com.annhien.womenshealth.data.repository

import com.annhien.womenshealth.core.database.dao.MedicalDocumentDao
import com.annhien.womenshealth.core.database.dao.MedicalVisitDao
import com.annhien.womenshealth.core.database.dao.MedicationScheduleDao
import com.annhien.womenshealth.core.database.entity.MedicalDocumentEntity
import com.annhien.womenshealth.core.database.entity.MedicalVisitEntity
import com.annhien.womenshealth.core.database.entity.MedicationScheduleEntity
import com.annhien.womenshealth.domain.repository.MedicalRecordsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicalRecordsRepositoryImpl @Inject constructor(
    private val medicalVisitDao: MedicalVisitDao,
    private val medicalDocumentDao: MedicalDocumentDao,
    private val medicationScheduleDao: MedicationScheduleDao
) : MedicalRecordsRepository {

    override fun observeVisits(): Flow<List<MedicalVisitEntity>> {
        return medicalVisitDao.observeAll()
    }

    override suspend fun getVisitById(id: String): MedicalVisitEntity? {
        return medicalVisitDao.getById(id)
    }

    override suspend fun saveVisit(visit: MedicalVisitEntity) {
        medicalVisitDao.upsert(visit)
    }

    override suspend fun deleteVisit(visitId: String) {
        val existing = medicalVisitDao.getById(visitId)
        if (existing != null) {
            medicalVisitDao.delete(existing)
        }
    }

    override suspend fun searchVisits(query: String): List<MedicalVisitEntity> {
        return medicalVisitDao.search(query)
    }

    override fun observeMedicationSchedules(): Flow<List<MedicationScheduleEntity>> {
        return medicationScheduleDao.observeActive()
    }

    override suspend fun saveMedicationSchedule(schedule: MedicationScheduleEntity) {
        medicationScheduleDao.upsert(schedule)
    }

    override suspend fun deactivateMedication(scheduleId: String) {
        medicationScheduleDao.deactivate(scheduleId)
    }

    override fun observeDocuments(visitId: String): Flow<List<MedicalDocumentEntity>> {
        return medicalDocumentDao.observeByVisit(visitId)
    }

    override fun observeAllDocuments(): Flow<List<MedicalDocumentEntity>> {
        return medicalDocumentDao.observeAll()
    }

    override suspend fun saveDocument(doc: MedicalDocumentEntity) {
        medicalDocumentDao.upsert(doc)
    }

    override suspend fun deleteDocument(docId: String) {
        medicalDocumentDao.deleteById(docId)
    }
}
