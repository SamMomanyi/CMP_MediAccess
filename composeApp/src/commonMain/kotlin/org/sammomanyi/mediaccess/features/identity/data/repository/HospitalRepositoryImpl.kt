package org.sammomanyi.mediaccess.features.identity.data.repository

import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.sammomanyi.mediaccess.core.domain.util.DataError
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.features.identity.data.local.HospitalDao
import org.sammomanyi.mediaccess.features.identity.data.mappers.toDomain
import org.sammomanyi.mediaccess.features.identity.data.mappers.toEntity
import org.sammomanyi.mediaccess.features.identity.domain.model.Hospital
import org.sammomanyi.mediaccess.features.identity.domain.repository.HospitalRepository

class HospitalRepositoryImpl(
    private val hospitalDao: HospitalDao,
    private val firestore: FirebaseFirestore
) : HospitalRepository {

    override fun getAllHospitals(): Flow<List<Hospital>> {
        return hospitalDao.getAllHospitals().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncHospitals(): Result<Unit, DataError> {
        return try {
            val snapshot = firestore.collection("hospitals").get()

            val hospitals = snapshot.documents.map { doc ->
                doc.data<Hospital>()
            }

            if (hospitals.isEmpty()) {
                // Seed initial data if empty
                seedInitialHospitals()
            } else {
                hospitalDao.insertHospitals(hospitals.map { it.toEntity() })
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            println("🔴 Error syncing hospitals: ${e.message}")
            Result.Error(DataError.Network.SERVER_ERROR)
        }
    }

    override suspend fun getNearbyHospitals(
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): Result<List<Hospital>, DataError> {
        return try {
            val allHospitals = hospitalDao.getAllHospitals().map { entities ->
                entities.map { it.toDomain() }
            }

            // Filter by distance
            val nearbyHospitals = allHospitals.map { hospitals ->
                hospitals.filter { hospital ->
                    hospital.distanceFrom(latitude, longitude) <= radiusKm
                }.sortedBy { it.distanceFrom(latitude, longitude) }
            }

            // For now, return first emission (in real app, collect flow properly)
            Result.Success(emptyList()) // TODO: Proper flow handling
        } catch (e: Exception) {
            Result.Error(DataError.Network.SERVER_ERROR)
        }
    }

    private suspend fun seedInitialHospitals() {
        val nairobiHospitals = listOf(
            Hospital(
                id = "hosp_001",
                name = "Nairobi Hospital",
                address = "Argwings Kodhek Road",
                city = "Nairobi",
                phoneNumber = "+254-20-2845000",
                email = "info@nbihos pital.org",
                        latitude = -1.2921,
                longitude = 36.8219,
                specialties = listOf("Emergency", "Surgery", "Cardiology", "Pediatrics"),
                operatingHours = "24/7",
                emergencyServices = true,
                rating = 4.5
            ),
            Hospital(
                id = "hosp_002",
                name = "Aga Khan University Hospital",
                address = "3rd Parklands Avenue",
                city = "Nairobi",
                phoneNumber = "+254-20-3662000",
                email = "info@aku.edu",
                latitude = -1.2634,
                longitude = 36.8078,
                specialties = listOf("Oncology", "Neurology", "Orthopedics"),
                operatingHours = "24/7",
                emergencyServices = true,
                rating = 4.7
            ),
            Hospital(
                id = "hosp_003",
                name = "Kenyatta National Hospital",
                address = "Hospital Road",
                city = "Nairobi",
                phoneNumber = "+254-20-2726300",
                email = "info@knh.or.ke",
                latitude = -1.3018,
                longitude = 36.8073,
                specialties = listOf("General Medicine", "Maternity", "ICU"),
                operatingHours = "24/7",
                emergencyServices = true,
                rating = 4.2
            )
        )

        // Save to Firestore
        nairobiHospitals.forEach { hospital ->
            firestore.collection("hospitals").document(hospital.id).set(hospital)
        }

        // Save locally
        hospitalDao.insertHospitals(nairobiHospitals.map { it.toEntity() })
    }
}