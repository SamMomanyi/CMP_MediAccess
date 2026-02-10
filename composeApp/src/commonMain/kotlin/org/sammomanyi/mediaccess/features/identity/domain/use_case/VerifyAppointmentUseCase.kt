package org.sammomanyi.mediaccess.features.identity.domain.use_case

import org.sammomanyi.mediaccess.core.domain.util.DataError
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.features.identity.domain.model.Appointment
import org.sammomanyi.mediaccess.features.identity.domain.repository.AppointmentRepository

class VerifyAppointmentUseCase(
    private val repository: AppointmentRepository
) {
    suspend operator fun invoke(visitCode: String, verifiedBy: String): Result<Appointment, DataError> {
        if (visitCode.isBlank()) {
            return Result.Error(DataError.Validation.INVALID_VISIT_CODE)
        }

        return repository.verifyAppointment(visitCode, verifiedBy)
    }
}