package org.sammomanyi.mediaccess.features.cover.domain.model

data class CoverLinkRequest(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val country: String = "",
    val insuranceName: String = "",
    val memberNumber: String = "",
    val status: CoverStatus = CoverStatus.PENDING,
    val submittedAt: Long = 0L,
    val reviewedAt: Long? = null,
    val reviewNote: String = ""
)

enum class CoverStatus {
    PENDING, APPROVED, REJECTED
}