package org.sammomanyi.mediaccess.features.identity.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CoverLinkRequest(
    val id: String = "", // Will be set by Firestore
    val userId: String,
    val userEmail: String,
    val requestType: LinkRequestType,
    // Nullable because they depend on the type
    val insuranceProviderName: String? = null,
    val memberNumber: String? = null,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val timestamp: Long = 0L
)

@Serializable
enum class LinkRequestType {
    AUTOMATIC,
    MANUAL
}