package org.sammomanyi.mediaccess.features.identity.data.repository

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

class DesktopAppointmentRepositoryImpl(
    private val appointmentDao: AppointmentDao
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
            appointmentDao.insertAppointment(appointment.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DataError.Local.DATABASE_ERROR)
        }
    }

    override suspend fun verifyAppointment(
        visitCode: String,
        verifiedBy: String
    ): Result<Appointment, DataError> {
        return try {
            val appointmentEntity = appointmentDao.getAppointmentByCode(visitCode)
                ?: return Result.Error(DataError.Validation.INVALID_VISIT_CODE)

            val appointment = appointmentEntity.toDomain()

            if (appointment.status != AppointmentStatus.PENDING) {
                return Result.Error(DataError.Validation.VISIT_CODE_USED)
            }

            val verifiedAppointment = appointment.copy(
                status = AppointmentStatus.VERIFIED,
                verifiedAt = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                verifiedBy = verifiedBy
            )

            appointmentDao.updateAppointment(verifiedAppointment.toEntity())

            Result.Success(verifiedAppointment)
        } catch (e: Exception) {
            Result.Error(DataError.Local.DATABASE_ERROR)
        }
    }

    override suspend fun updateAppointmentStatus(
        appointmentId: String,
        status: AppointmentStatus
    ): Result<Unit, DataError> {
        return try {
            // Desktop version doesn't support this yet
            Result.Error(DataError.Local.DATABASE_ERROR)
        } catch (e: Exception) {
            Result.Error(DataError.Local.DATABASE_ERROR)
        }
    }
}