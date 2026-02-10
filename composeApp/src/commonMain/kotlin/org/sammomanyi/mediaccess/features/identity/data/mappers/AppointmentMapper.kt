package org.sammomanyi.mediaccess.features.identity.data.mappers

import org.sammomanyi.mediaccess.features.identity.data.local.AppointmentEntity
import org.sammomanyi.mediaccess.features.identity.domain.model.Appointment
import org.sammomanyi.mediaccess.features.identity.domain.model.AppointmentStatus

fun AppointmentEntity.toDomain(): Appointment {
    return Appointment(
        id = id,
        patientId = patientId,
        patientName = patientName,
        patientMedicalId = patientMedicalId,
        hospitalId = hospitalId,
        hospitalName = hospitalName,
        visitCode = visitCode,
        purpose = purpose,
        scheduledDate = scheduledDate,
        status = AppointmentStatus.valueOf(status),
        verifiedAt = verifiedAt,
        verifiedBy = verifiedBy,
        createdAt = createdAt
    )
}

fun Appointment.toEntity(): AppointmentEntity {
    return AppointmentEntity(
        id = id,
        patientId = patientId,
        patientName = patientName,
        patientMedicalId = patientMedicalId,
        hospitalId = hospitalId,
        hospitalName = hospitalName,
        visitCode = visitCode,
        purpose = purpose,
        scheduledDate = scheduledDate,
        status = status.name,
        verifiedAt = verifiedAt,
        verifiedBy = verifiedBy,
        createdAt = createdAt
    )
}