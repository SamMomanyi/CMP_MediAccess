package org.sammomanyi.mediaccess.features.identity.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medical_records")
data class MedicalRecordEntity(
    @PrimaryKey val id: String,
    val patientId: String,
    val patientName: String,
    val diagnosis: String,
    val symptoms: String,
    val prescription: String,
    val doctorName: String,
    val hospital: String,
    val visitDate: Long,
    val followUpDate: Long?,
    val notes: String,
    val createdAt: Long
)