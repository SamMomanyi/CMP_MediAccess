package org.sammomanyi.mediaccess.features.identity.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hospitals")
data class HospitalEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val city: String,
    val phoneNumber: String,
    val email: String,
    val latitude: Double,
    val longitude: Double,
    val specialties: String, // JSON string
    val operatingHours: String,
    val emergencyServices: Boolean,
    val rating: Double,
    val imageUrl: String?
)