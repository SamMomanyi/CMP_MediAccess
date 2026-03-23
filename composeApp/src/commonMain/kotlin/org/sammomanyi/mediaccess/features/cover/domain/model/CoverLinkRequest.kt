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
    val reviewNote: String = "",

    // ✅ NEW: Cover amount tracking
    val coverAmount: Double = 100000.0,      // Initial cover (KES 100,000)
    val remainingBalance: Double = 100000.0, // Current balance
    val totalSpent: Double = 0.0             // Total expenditure
)

enum class CoverStatus {
    PENDING, APPROVED, REJECTED
}