package org.sammomanyi.mediaccess.features.identity.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.sammomanyi.mediaccess.core.domain.util.DataError
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.features.identity.data.local.MedicalRecordDao
import org.sammomanyi.mediaccess.features.identity.data.mappers.toDomain
import org.sammomanyi.mediaccess.features.identity.data.mappers.toEntity
import org.sammomanyi.mediaccess.features.identity.domain.model.MedicalRecord
import org.sammomanyi.mediaccess.features.identity.domain.repository.RecordsRepository

class DesktopRecordsRepositoryImpl(
    private val medicalRecordDao: MedicalRecordDao
) : RecordsRepository {

    override fun getRecordsByPatientId(patientId: String): Flow<List<MedicalRecord>> {
        return medicalRecordDao.getRecordsByPatientId(patientId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncRecords(patientId: String): Result<Unit, DataError> {
        // Desktop doesn't sync - data comes from Room only
        return Result.Success(Unit)
    }

    override suspend fun createRecord(record: MedicalRecord): Result<Unit, DataError> {
        return try {
            medicalRecordDao.insertRecord(record.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DataError.Local.DATABASE_ERROR)
        }
    }
}