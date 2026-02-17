package org.sammomanyi.mediaccess.features.queue.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class StaffAccount(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "DOCTOR",            // matches StaffRole enum name
    val roomNumber: String = "",
    val specialization: String = "",
    val isOnDuty: Boolean = false,
    val lastSeenAt: Long = 0L,
    val passwordHash: String = ""           // SHA-256, for desktop login
)