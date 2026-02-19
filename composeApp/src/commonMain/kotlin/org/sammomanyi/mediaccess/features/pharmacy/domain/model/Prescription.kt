package org.sammomanyi.mediaccess.features.pharmacy.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Prescription(
    val id: String,
    val patientUserId: String,
    val patientName: String,
    val patientEmail: String,
    val doctorId: String,
    val doctorName: String,
    val queueEntryId: String,
    val medications: List<PrescriptionItem>,
    val notes: String,
    val status: PrescriptionStatus,
    val createdAt: Long,
    val dispensedAt: Long? = null,
    val totalCost: Double? = null,
    val date: String
)

@Serializable
data class PrescriptionItem(
    val medicationName: String,
    val dosage: String,
    val frequency: String,
    val duration: String,
    val quantity: Int,
    val unitPrice: Double = 0.0
)

enum class PrescriptionStatus {
    PENDING,      // Created by doctor, waiting for pharmacy
    DISPENSING,   // Pharmacist is processing
    COMPLETED     // Medication dispensed, paid
}