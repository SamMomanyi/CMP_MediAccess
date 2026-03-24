package org.sammomanyi.mediaccess.features.cover.data.desktop

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import org.sammomanyi.mediaccess.features.cover.data.local.CoverLinkRequestDao
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverLinkRequest
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverStatus

data class CoverRequestsState(
    val requests: List<CoverLinkRequest> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastRefreshedAt: Long? = null
)

class DesktopCoverRepository(
    private val dao: CoverLinkRequestDao,
    private val firestoreClient: FirestoreRestClient
) {
    private val _state = MutableStateFlow(CoverRequestsState())
    val state: StateFlow<CoverRequestsState> = _state.asStateFlow()

    // ── Manual refresh ────────────────────────────────────────
    suspend fun refresh() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        try {
            val rawDocs = firestoreClient.getCollection("cover_requests")
            val requests = rawDocs.mapNotNull { doc ->
                try { doc.toCoverLinkRequest() }
                catch (e: Exception) {
                    println("⚠️ Skipping malformed doc: ${e.message}")
                    null
                }
            }.sortedByDescending { it.submittedAt }

            _state.value = CoverRequestsState(
                requests = requests,
                isLoading = false,
                lastRefreshedAt = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = "Failed to load: ${e.message}"
            )
        }
    }

    // ── Approve ───────────────────────────────────────────────
    suspend fun approveRequest(id: String, note: String = "Approved"): Result<Unit> =
        updateStatus(id, CoverStatus.APPROVED, note)

    // ── Reject ────────────────────────────────────────────────
    suspend fun rejectRequest(id: String, note: String): Result<Unit> =
        updateStatus(id, CoverStatus.REJECTED, note)

    private suspend fun updateStatus(
        id: String,
        status: CoverStatus,
        note: String
    ): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()

            firestoreClient.updateDocument(
                collection = "cover_requests",
                documentId = id,
                fields = mapOf(
                    "status" to status.name,
                    "reviewedAt" to now,
                    "reviewNote" to note
                )
            )

            dao.updateStatus(id, status.name, now, note)

            val updated = _state.value.requests.map {
                if (it.id == id) it.copy(
                    status = status,
                    reviewedAt = now,
                    reviewNote = note
                ) else it
            }
            _state.value = _state.value.copy(requests = updated)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ NEW: Get approved cover for a specific user
    suspend fun getApprovedCoverForUser(userId: String): CoverLinkRequest? {
        // First check in-memory state
        val fromState = _state.value.requests.firstOrNull {
            it.userId == userId && it.status == CoverStatus.APPROVED
        }
        if (fromState != null) return fromState

        // If not in state, fetch fresh from Firestore
        return try {
            val rawDocs = firestoreClient.getCollection("cover_requests")
            rawDocs.mapNotNull { doc ->
                try { doc.toCoverLinkRequest() }
                catch (e: Exception) { null }
            }.firstOrNull {
                it.userId == userId && it.status == CoverStatus.APPROVED
            }
        } catch (e: Exception) {
            println("🔴 Failed to get approved cover for user $userId: ${e.message}")
            null
        }
    }

    // ✅ NEW: Update cover balance after billing
    suspend fun updateCoverBalance(
        coverRequestId: String,
        newBalance: Double,
        amountSpent: Double
    ) {
        try {
            // Get current document to read totalSpent
            val currentDoc = firestoreClient.getDocument("cover_requests", coverRequestId)
            val currentSpent = (currentDoc?.get("totalSpent") as? Number)?.toDouble() ?: 0.0

            // Update Firestore
            firestoreClient.updateDocument(
                collection = "cover_requests",
                documentId = coverRequestId,
                fields = mapOf(
                    "remainingBalance" to newBalance,
                    "totalSpent" to (currentSpent + amountSpent)
                )
            )

            // Update local state
            val updated = _state.value.requests.map {
                if (it.id == coverRequestId) it.copy(
                    remainingBalance = newBalance,
                    totalSpent = currentSpent + amountSpent
                ) else it
            }
            _state.value = _state.value.copy(requests = updated)

            println("✅ COVER: Updated balance - Remaining: $newBalance, Total spent: ${currentSpent + amountSpent}")
        } catch (e: Exception) {
            println("🔴 COVER: Failed to update balance: ${e.message}")
        }
    }
}

// ── Firestore Map → Domain model ──────────────────────────────
private fun Map<String, Any?>.toCoverLinkRequest(): CoverLinkRequest {
    return CoverLinkRequest(
        id = get("id") as? String ?: error("Missing id"),
        userId = get("userId") as? String ?: "",
        userEmail = get("userEmail") as? String ?: "",
        country = get("country") as? String ?: "",
        insuranceName = get("insuranceName") as? String ?: "",
        memberNumber = get("memberNumber") as? String ?: "",
        status = CoverStatus.valueOf(
            (get("status") as? String)?.uppercase() ?: "PENDING"
        ),
        submittedAt = (get("submittedAt") as? Long) ?: 0L,
        reviewedAt = get("reviewedAt") as? Long,
        reviewNote = get("reviewNote") as? String ?: "",
        // ✅ Parse financial fields
        coverAmount = (get("coverAmount") as? Number)?.toDouble() ?: 100000.0,
        remainingBalance = (get("remainingBalance") as? Number)?.toDouble() ?: 100000.0,
        totalSpent = (get("totalSpent") as? Number)?.toDouble() ?: 0.0
    )
}