package org.sammomanyi.mediaccess.features.identity.domain.repository

import kotlinx.coroutines.flow.Flow
import org.sammomanyi.mediaccess.core.domain.util.DataError
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.features.identity.domain.model.MedicalRecord

interface RecordsRepository {
    fun getRecordsByPatientId(patientId: String): Flow<List<MedicalRecord>>
    suspend fun syncRecords(patientId: String): Result<Unit, DataError>
    suspend fun createRecord(record: MedicalRecord): Result<Unit, DataError>
}