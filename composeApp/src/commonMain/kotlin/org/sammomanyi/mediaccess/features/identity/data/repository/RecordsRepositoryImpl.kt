package org.sammomanyi.mediaccess.features.identity.data.repository

import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.sammomanyi.mediaccess.core.domain.util.DataError
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.features.identity.data.local.MedicalRecordDao
import org.sammomanyi.mediaccess.features.identity.data.mappers.toDomain
import org.sammomanyi.mediaccess.features.identity.data.mappers.toEntity
import org.sammomanyi.mediaccess.features.identity.domain.model.MedicalRecord
import org.sammomanyi.mediaccess.features.identity.domain.repository.RecordsRepository

class RecordsRepositoryImpl(
    private val medicalRecordDao: MedicalRecordDao,
    private val firestore: FirebaseFirestore
) : RecordsRepository {

    override fun getRecordsByPatientId(patientId: String): Flow<List<MedicalRecord>> {
        return medicalRecordDao.getRecordsByPatientId(patientId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncRecords(patientId: String): Result<Unit, DataError> {
        return try {
            val snapshot = firestore.collection("medical_records")
                .where { "patientId" equalTo patientId }
                .get()

            val records = snapshot.documents.map { doc ->
                doc.data<MedicalRecord>()
            }

            medicalRecordDao.insertRecords(records.map { it.toEntity() })
            Result.Success(Unit)
        } catch (e: Exception) {
            println("🔴 Error syncing records: ${e.message}")
            Result.Error(DataError.Network.SERVER_ERROR)
        }
    }

    override suspend fun createRecord(record: MedicalRecord): Result<Unit, DataError> {
        return try {
            // Save to Firestore
            firestore.collection("medical_records")
                .document(record.id)
                .set(record)

            // Save locally
            medicalRecordDao.insertRecord(record.toEntity())

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DataError.Network.SERVER_ERROR)
        }
    }
}