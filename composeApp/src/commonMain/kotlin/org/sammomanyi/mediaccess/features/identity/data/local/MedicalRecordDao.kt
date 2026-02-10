package org.sammomanyi.mediaccess.features.identity.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicalRecordDao {
    @Query("SELECT * FROM medical_records WHERE patientId = :patientId ORDER BY visitDate DESC")
    fun getRecordsByPatientId(patientId: String): Flow<List<MedicalRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: MedicalRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<MedicalRecordEntity>)

    @Query("DELETE FROM medical_records WHERE patientId = :patientId")
    suspend fun deleteRecordsByPatientId(patientId: String)
}