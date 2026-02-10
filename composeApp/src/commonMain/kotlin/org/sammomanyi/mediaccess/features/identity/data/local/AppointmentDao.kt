package org.sammomanyi.mediaccess.features.identity.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments WHERE patientId = :patientId ORDER BY scheduledDate DESC")
    fun getAppointmentsByPatientId(patientId: String): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE status = :status ORDER BY scheduledDate ASC")
    fun getAppointmentsByStatus(status: String): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE visitCode = :code")
    suspend fun getAppointmentByCode(code: String): AppointmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity)

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)

    @Query("DELETE FROM appointments WHERE patientId = :patientId")
    suspend fun deleteAppointmentsByPatientId(patientId: String)
}