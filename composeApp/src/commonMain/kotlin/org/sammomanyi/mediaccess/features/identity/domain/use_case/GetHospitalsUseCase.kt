package org.sammomanyi.mediaccess.features.identity.domain.use_case

import kotlinx.coroutines.flow.Flow
import org.sammomanyi.mediaccess.features.identity.domain.model.Hospital
import org.sammomanyi.mediaccess.features.identity.domain.repository.HospitalRepository

class GetHospitalsUseCase(
    private val repository: HospitalRepository
) {
    operator fun invoke(): Flow<List<Hospital>> {
        return repository.getAllHospitals()
    }
}