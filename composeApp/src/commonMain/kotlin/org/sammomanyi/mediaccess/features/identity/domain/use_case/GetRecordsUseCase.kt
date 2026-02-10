package org.sammomanyi.mediaccess.features.identity.domain.use_case

import kotlinx.coroutines.flow.Flow
import org.sammomanyi.mediaccess.features.identity.domain.model.MedicalRecord
import org.sammomanyi.mediaccess.features.identity.domain.repository.RecordsRepository

class GetRecordsUseCase(
    private val repository: RecordsRepository
) {
    operator fun invoke(patientId: String): Flow<List<MedicalRecord>> {
        return repository.getRecordsByPatientId(patientId)
    }
}