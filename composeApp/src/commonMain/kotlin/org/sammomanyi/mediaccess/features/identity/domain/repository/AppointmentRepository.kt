package org.sammomanyi.mediaccess.features.identity.domain.repository

import kotlinx.coroutines.flow.Flow
import org.sammomanyi.mediaccess.core.domain.util.DataError
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.features.identity.domain.model.Appointment
import org.sammomanyi.mediaccess.features.identity.domain.model.AppointmentStatus

interface AppointmentRepository {
    fun getAppointmentsByPatientId(patientId: String): Flow<List<Appointment>>
    fun getPendingAppointments(): Flow<List<Appointment>>
    suspend fun createAppointment(appointment: Appointment): Result<Unit, DataError>
    suspend fun verifyAppointment(visitCode: String, verifiedBy: String): Result<Appointment, DataError>
    suspend fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus): Result<Unit, DataError>
}