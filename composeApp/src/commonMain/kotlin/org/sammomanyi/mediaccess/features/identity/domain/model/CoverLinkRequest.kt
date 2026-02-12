package org.sammomanyi.mediaccess.features.identity.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CoverLinkRequest(
    val id: String = "",
    val userId: String,
    val userEmail: String,
    val requestType: LinkRequestType,
    val insuranceProviderName: String? = null,
    val memberNumber: String? = null,
    val country: String = "KENYA", // Added field
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val reviewNote: String = "",
    val timestamp: Long = 0L
)

@Serializable
enum class LinkRequestType {
    AUTOMATIC, MANUAL
}