package org.sammomanyi.mediaccess.features.identity.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HospitalDao {
    @Query("SELECT * FROM hospitals ORDER BY name ASC")
    fun getAllHospitals(): Flow<List<HospitalEntity>>

    @Query("SELECT * FROM hospitals WHERE city = :city")
    fun getHospitalsByCity(city: String): Flow<List<HospitalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHospital(hospital: HospitalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHospitals(hospitals: List<HospitalEntity>)

    @Query("DELETE FROM hospitals")
    suspend fun deleteAllHospitals()
}