package org.sammomanyi.mediaccess.features.identity.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val dateOfBirth: String, // Store as ISO string: "1990-01-15"
    val gender: String,      // Store enum as string: "MALE", "FEMALE", etc.
    val role: String,        // "PATIENT" or "HOSPITAL_STAFF"
    val medicalId: String,
    val nationalId: String?,
    val balance: Double,
    val profileImageUrl: String?,
    val isEmailVerified: Boolean,
    val createdAt: Long
)