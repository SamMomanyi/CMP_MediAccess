package org.sammomanyi.mediaccess.features.identity.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey val id: String,
    val patientId: String,
    val patientName: String,
    val patientMedicalId: String,
    val hospitalId: String,
    val hospitalName: String,
    val visitCode: String,
    val purpose: String,
    val scheduledDate: Long,
    val status: String,
    val verifiedAt: Long?,
    val verifiedBy: String?,
    val createdAt: Long
)