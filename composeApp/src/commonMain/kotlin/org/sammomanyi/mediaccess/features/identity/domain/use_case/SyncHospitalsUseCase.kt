package org.sammomanyi.mediaccess.features.identity.domain.use_case

import org.sammomanyi.mediaccess.core.domain.util.DataError
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.features.identity.domain.repository.HospitalRepository

class SyncHospitalsUseCase(
    private val repository: HospitalRepository
) {
    suspend operator fun invoke(): Result<Unit, DataError> {
        return repository.syncHospitals()
    }
}