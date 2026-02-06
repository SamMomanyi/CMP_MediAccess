package org.sammomanyi.mediaccess.features.identity.domain.model

data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val balance: Double,
    val medicalId: String // This is what the QR code will represent
)