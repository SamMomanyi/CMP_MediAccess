package org.sammomanyi.mediaccess.features.identity.domain.model

import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime


data class Appointment @OptIn(ExperimentalTime::class) constructor(
    val id: String,
    val patientId: String,
    val doctorName: String,
    val department: String,
    val dateTime: Instant,
    val status: AppointmentStatus
)



enum class AppointmentStatus {
    SCHEDULED, COMPLETED, CANCELLED, NO_SHOW
}