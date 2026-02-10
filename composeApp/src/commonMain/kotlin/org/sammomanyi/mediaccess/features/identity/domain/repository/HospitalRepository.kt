package org.sammomanyi.mediaccess.features.identity.domain.repository

import kotlinx.coroutines.flow.Flow
import org.sammomanyi.mediaccess.core.domain.util.DataError
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.features.identity.domain.model.Hospital

interface HospitalRepository {
    fun getAllHospitals(): Flow<List<Hospital>>
    suspend fun syncHospitals(): Result<Unit, DataError>
    suspend fun getNearbyHospitals(latitude: Double, longitude: Double, radiusKm: Double): Result<List<Hospital>, DataError>
}