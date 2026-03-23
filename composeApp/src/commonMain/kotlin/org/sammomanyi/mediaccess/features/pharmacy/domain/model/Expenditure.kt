package org.sammomanyi.mediaccess.features.pharmacy.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Expenditure(
    val id: String = "",
    val patientUserId: String,
    val patientName: String,
    val patientEmail: String,

    // Medical staff
    val doctorId: String,
    val doctorName: String,
    val pharmacistId: String? = null,
    val pharmacistName: String? = null,

    // Visit details
    val hospitalName: String = "MediAccess Hospital", // Default for now
    val visitType: String, // "Consultation", "Follow-up", "Emergency", etc.
    val prescriptionId: String? = null,

    // Financial
    val consultationFee: Double = 500.0, // Doctor consultation fee
    val medicationCost: Double = 0.0,    // Cost of prescribed medications
    val totalAmount: Double,              // Total charged
    val coverUsed: Double,                // Amount paid by insurance
    val outOfPocket: Double = 0.0,        // Amount paid by patient (if cover insufficient)

    // Insurance details
    val insuranceName: String,
    val memberNumber: String,
    val coverBalanceBefore: Double,
    val coverBalanceAfter: Double,

    // Timestamps
    val date: String,      // YYYY-MM-DD format
    val timestamp: Long    // Epoch milliseconds
)