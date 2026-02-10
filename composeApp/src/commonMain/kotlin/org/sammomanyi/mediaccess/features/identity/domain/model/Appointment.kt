package org.sammomanyi.mediaccess.features.identity.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Appointment(
    val id: String = "",
    val patientId: String,
    val patientName: String,
    val patientMedicalId: String,
    val hospitalId: String,
    val hospitalName: String,
    val visitCode: String,
    val purpose: String,
    val scheduledDate: Long,
    val status: AppointmentStatus,
    val verifiedAt: Long? = null,
    val verifiedBy: String? = null,
    val createdAt: Long
)

@Serializable
enum class AppointmentStatus {
    PENDING,
    VERIFIED,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}

