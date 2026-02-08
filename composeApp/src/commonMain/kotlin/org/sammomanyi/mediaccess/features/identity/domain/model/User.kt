package org.sammomanyi.mediaccess.features.identity.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "", // Empty string for new users
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,

    // Store as string for Firestore compatibility
    val dateOfBirth: String, // Changed from LocalDate
    val gender: String,      // Changed from Gender enum
    val role: String,        // Changed from UserRole enum

    // Medical identifiers
    val medicalId: String, // Unique medical record number
    val nationalId: String? = null, // Optional: National ID/SSN

    // Financial (if needed - consider moving to separate domain)
    val balance: Double = 0.0,

    // Profile
    val profileImageUrl: String? = null,
    val isEmailVerified: Boolean = false,
    val createdAt: Long = 0L
) {
    val fullName: String get() = "$firstName $lastName"
    val displayName: String get() = fullName
}

enum class Gender {
    MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
}

enum class UserRole {
    PATIENT,        // Mobile app users
    HOSPITAL_STAFF, // Desktop users (receptionist, doctor, nurse)
    ADMIN           // Future: System administrators
}