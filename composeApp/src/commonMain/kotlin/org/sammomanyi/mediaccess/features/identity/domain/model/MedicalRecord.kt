package org.sammomanyi.mediaccess.features.identity.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MedicalRecord(
    val id: String = "",
    val patientId: String,
    val patientName: String,
    val diagnosis: String,
    val symptoms: String,
    val prescription: String,
    val doctorName: String,
    val hospital: String,
    val visitDate: Long,
    val followUpDate: Long? = null,
    val notes: String = "",
    val createdAt: Long = 0L
)