package org.sammomanyi.mediaccess.features.verification.data.desktop

import org.sammomanyi.mediaccess.features.cover.data.desktop.FirestoreRestClient

// ── Domain models ─────────────────────────────────────────────

data class VerifiedVisitResult(
    // Visit code info
    val code: String,
    val purpose: String,
    val expiresAt: Long,
    val usedAt: Long?,
    val isActive: Boolean,

    // Patient info
    val userId: String,
    val patientName: String,
    val patientEmail: String,
    val medicalId: String,

    // Cover info — verification REQUIRES APPROVED cover
    val coverStatus: CoverVerificationStatus,
    val insuranceName: String?,
    val memberNumber: String?,
    val country: String?,
    val coverSubmittedAt: Long?
)

enum class CoverVerificationStatus {
    // ✓ Can mark as used
    APPROVED,

    // ✗ Cannot proceed — different reason messages
    COVER_PENDING,      // Cover exists but not approved yet
    COVER_REJECTED,     // Cover was rejected
    COVER_NONE,         // No cover request found at all
    CODE_EXPIRED,       // Code past expiry time
    CODE_ALREADY_USED,  // Code was already consumed
    CODE_INVALID,       // Code not found in Firestore
}

// ── Client ────────────────────────────────────────────────────

class VisitVerificationRestClient(
    private val firestoreClient: FirestoreRestClient
) {

    suspend fun verifyCode(code: String): Result<VerifiedVisitResult> {
        return try {
            // 1. Fetch the visit code
            val visitDocs = firestoreClient.getCollection("visit_codes")
            val visitDoc = visitDocs.firstOrNull { it["code"] as? String == code }
                ?: return Result.success(invalidCodeResult(code))

            val userId     = visitDoc["userId"] as? String ?: ""
            val expiresAt  = (visitDoc["expiresAt"] as? Long) ?: 0L
            val usedAt     = visitDoc["usedAt"] as? Long
            val isActive   = visitDoc["isActive"] as? Boolean ?: true
            val purpose    = visitDoc["purpose"] as? String ?: "CONSULTATION"

            // 2. Validate the code itself
            val now = System.currentTimeMillis()
            if (!isActive || now > expiresAt) {
                return Result.success(
                    buildResult(
                        code, purpose, expiresAt, usedAt, isActive,
                        userId, "", "", "",
                        CoverVerificationStatus.CODE_EXPIRED,
                        null, null, null, null
                    )
                )
            }
            if (usedAt != null) {
                return Result.success(
                    buildResult(
                        code, purpose, expiresAt, usedAt, isActive,
                        userId, "", "", "",
                        CoverVerificationStatus.CODE_ALREADY_USED,
                        null, null, null, null
                    )
                )
            }

            // 3. Fetch patient details
            val userDocs = firestoreClient.getCollection("users")
            val userDoc  = userDocs.firstOrNull { it["id"] as? String == userId }

            val firstName   = userDoc?.get("firstName") as? String ?: ""
            val lastName    = userDoc?.get("lastName") as? String ?: ""
            val patientName = "$firstName $lastName".trim().ifEmpty { "Unknown Patient" }
            val email       = userDoc?.get("email") as? String ?: ""
            val medicalId   = userDoc?.get("medicalId") as? String ?: ""

            // 4. Fetch cover requests — CRITICAL: code is only valid if cover is APPROVED
            val coverDocs = firestoreClient.getCollection("cover_requests")
            val userCoverRequests = coverDocs
                .filter { it["userId"] as? String == userId }
                .sortedByDescending { (it["submittedAt"] as? Long) ?: 0L }

            val latestCover = userCoverRequests.firstOrNull()
            val coverStatusStr = latestCover?.get("status") as? String

            val coverStatus = when (coverStatusStr?.uppercase()) {
                "APPROVED" -> CoverVerificationStatus.APPROVED
                "PENDING"  -> CoverVerificationStatus.COVER_PENDING
                "REJECTED" -> CoverVerificationStatus.COVER_REJECTED
                else       -> CoverVerificationStatus.COVER_NONE
            }

            Result.success(
                buildResult(
                    code             = code,
                    purpose          = purpose,
                    expiresAt        = expiresAt,
                    usedAt           = usedAt,
                    isActive         = isActive,
                    userId           = userId,
                    patientName      = patientName,
                    patientEmail     = email,
                    medicalId        = medicalId,
                    coverStatus      = coverStatus,
                    insuranceName    = latestCover?.get("insuranceName") as? String,
                    memberNumber     = latestCover?.get("memberNumber") as? String,
                    country          = latestCover?.get("country") as? String,
                    coverSubmittedAt = latestCover?.get("submittedAt") as? Long
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markCodeAsUsed(code: String): Result<Unit> {
        return try {
            firestoreClient.updateDocument(
                collection = "visit_codes",
                documentId = code,
                fields = mapOf(
                    "usedAt"   to System.currentTimeMillis(),
                    "isActive" to false
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun invalidCodeResult(code: String) = buildResult(
        code, "", 0L, null, false, "", "", "", "",
        CoverVerificationStatus.CODE_INVALID,
        null, null, null, null
    )

    private fun buildResult(
        code: String, purpose: String, expiresAt: Long,
        usedAt: Long?, isActive: Boolean,
        userId: String, patientName: String, patientEmail: String, medicalId: String,
        coverStatus: CoverVerificationStatus,
        insuranceName: String?, memberNumber: String?,
        country: String?, coverSubmittedAt: Long?
    ) = VerifiedVisitResult(
        code             = code,
        purpose          = purpose,
        expiresAt        = expiresAt,
        usedAt           = usedAt,
        isActive         = isActive,
        userId           = userId,
        patientName      = patientName,
        patientEmail     = patientEmail,
        medicalId        = medicalId,
        coverStatus      = coverStatus,
        insuranceName    = insuranceName,
        memberNumber     = memberNumber,
        country          = country,
        coverSubmittedAt = coverSubmittedAt
    )
}