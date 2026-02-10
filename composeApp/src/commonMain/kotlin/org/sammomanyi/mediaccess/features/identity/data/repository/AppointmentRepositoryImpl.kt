package org.sammomanyi.mediaccess.features.identity.data.repository

import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.sammomanyi.mediaccess.core.domain.util.DataError
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.features.identity.data.local.AppointmentDao
import org.sammomanyi.mediaccess.features.identity.data.mappers.toDomain
import org.sammomanyi.mediaccess.features.identity.data.mappers.toEntity
import org.sammomanyi.mediaccess.features.identity.domain.model.Appointment
import org.sammomanyi.mediaccess.features.identity.domain.model.AppointmentStatus
import org.sammomanyi.mediaccess.features.identity.domain.repository.AppointmentRepository

class AppointmentRepositoryImpl(
    private val appointmentDao: AppointmentDao,
    private val firestore: FirebaseFirestore
) : AppointmentRepository {

    override fun getAppointmentsByPatientId(patientId: String): Flow<List<Appointment>> {
        return appointmentDao.getAppointmentsByPatientId(patientId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPendingAppointments(): Flow<List<Appointment>> {
        return appointmentDao.getAppointmentsByStatus(AppointmentStatus.PENDING.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun createAppointment(appointment: Appointment): Result<Unit, DataError> {
        return try {
            // Save to Firestore
            firestore.collection("appointments")
                .document(appointment.id)
                .set(appointment)

            // Save locally
            appointmentDao.insertAppointment(appointment.toEntity())

            Result.Success(Unit)
        } catch (e: Exception) {
            println("🔴 Error creating appointment: ${e.message}")
            Result.Error(DataError.Network.SERVER_ERROR)
        }
    }

    override suspend fun verifyAppointment(
        visitCode: String,
        verifiedBy: String
    ): Result<Appointment, DataError> {
        return try {
            // Find appointment by visit code
            val appointmentEntity = appointmentDao.getAppointmentByCode(visitCode)
                ?: return Result.Error(DataError.Validation.INVALID_VISIT_CODE)

            val appointment = appointmentEntity.toDomain()

            if (appointment.status != AppointmentStatus.PENDING) {
                return Result.Error(DataError.Validation.VISIT_CODE_USED)
            }

            // Update appointment
            val verifiedAppointment = appointment.copy(
                status = AppointmentStatus.VERIFIED,
                verifiedAt = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                verifiedBy = verifiedBy
            )

            // Update in Firestore
            firestore.collection("appointments")
                .document(appointment.id)
                .set(verifiedAppointment)

            // Update locally
            appointmentDao.updateAppointment(verifiedAppointment.toEntity())

            Result.Success(verifiedAppointment)
        } catch (e: Exception) {
            println("🔴 Error verifying appointment: ${e.message}")
            Result.Error(DataError.Network.SERVER_ERROR)
        }
    }

    override suspend fun updateAppointmentStatus(
        appointmentId: String,
        status: AppointmentStatus
    ): Result<Unit, DataError> {
        return try {
            // Update in Firestore
            firestore.collection("appointments")
                .document(appointmentId)
                .update("status" to status.name)

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DataError.Network.SERVER_ERROR)
        }
    }
}