package org.sammomanyi.mediaccess.features.identity.domain.model

import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime


data class MedicalRecord @OptIn(ExperimentalTime::class) constructor(
    val id: String,
    val patientId: String,
    val diagnosis: String,
    val prescription: String,
    val doctorName: String,
    val visitDate: Instant,
    val notes: String
)
