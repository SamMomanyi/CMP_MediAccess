package org.sammomanyi.mediaccess.features.pharmacy.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PharmacyQueueEntry(
    val id: String,
    val patientUserId: String,
    val patientName: String,
    val patientEmail: String,
    val prescriptionId: String,
    val queuePosition: Int,
    val status: PharmacyStatus,
    val assignedAt: Long,
    val dispensedAt: Long? = null,
    val date: String
)

enum class PharmacyStatus {
    WAITING,      // In pharmacy queue
    DISPENSING,   // Pharmacist is serving
    COMPLETED     // Medication received
}