package org.sammomanyi.mediaccess.features.identity.domain.use_case

import org.sammomanyi.mediaccess.core.domain.util.DataError
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.features.identity.domain.model.Appointment
import org.sammomanyi.mediaccess.features.identity.domain.model.AppointmentStatus
import org.sammomanyi.mediaccess.features.identity.domain.model.User
import org.sammomanyi.mediaccess.features.identity.domain.repository.AppointmentRepository
import java.util.UUID

class BookAppointmentUseCase(
    private val appointmentRepository: AppointmentRepository
) {
    suspend operator fun invoke(
        user: User,
        hospitalId: String,
        hospitalName: String,
        visitCode: String,
        purpose: String,
        scheduledDate: Long
    ): Result<Appointment, DataError> {
        val appointment = Appointment(
            id = "appt_${UUID.randomUUID()}",
            patientId = user.id,
            patientName = user.fullName,
            patientMedicalId = user.medicalId,
            hospitalId = hospitalId,
            hospitalName = hospitalName,
            visitCode = visitCode,
            purpose = purpose,
            scheduledDate = scheduledDate,
            status = AppointmentStatus.PENDING,
            createdAt = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        )

        return when (val result = appointmentRepository.createAppointment(appointment)) {
            is Result.Success -> Result.Success(appointment)
            is Result.Error -> Result.Error(result.error)
        }
    }
}