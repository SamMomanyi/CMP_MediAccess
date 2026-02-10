package org.sammomanyi.mediaccess.features.identity.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.sammomanyi.mediaccess.core.domain.util.DataError
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.features.identity.data.local.HospitalDao
import org.sammomanyi.mediaccess.features.identity.data.mappers.toDomain
import org.sammomanyi.mediaccess.features.identity.domain.model.Hospital
import org.sammomanyi.mediaccess.features.identity.domain.repository.HospitalRepository

class DesktopHospitalRepositoryImpl(
    private val hospitalDao: HospitalDao
) : HospitalRepository {

    override fun getAllHospitals(): Flow<List<Hospital>> {
        return hospitalDao.getAllHospitals().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncHospitals(): Result<Unit, DataError> {
        // Desktop doesn't sync
        return Result.Success(Unit)
    }

    override suspend fun getNearbyHospitals(
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): Result<List<Hospital>, DataError> {
        // Desktop doesn't support location
        return Result.Error(DataError.Local.UNKNOWN)
    }
}