package org.sammomanyi.mediaccess.features.identity.domain.use_case

import org.sammomanyi.mediaccess.core.domain.util.DataError
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.features.identity.domain.repository.RecordsRepository

class SyncRecordsUseCase(
    private val repository: RecordsRepository
) {
    suspend operator fun invoke(patientId: String): Result<Unit, DataError> {
        return repository.syncRecords(patientId)
    }
}