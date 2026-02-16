package org.sammomanyi.mediaccess.features.cover.data.desktop

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import org.sammomanyi.mediaccess.features.cover.data.local.CoverLinkRequestDao
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverLinkRequest
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverStatus

data class CoverRequestsState(
    val requests: List<CoverLinkRequest> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastRefreshedAt: Long? = null   // epoch millis — shown in UI
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
                lastRefreshedAt = Clock.System.now().toEpochMilliseconds()
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
            val now = Clock.System.now().toEpochMilliseconds()

            firestoreClient.updateDocument(
                collection = "cover_requests",
                documentId = id,
                fields = mapOf(
                    "status" to status.name,
                    "reviewedAt" to now,
                    "reviewNote" to note
                )
            )

            // Update local Room cache
            dao.updateStatus(id, status.name, now, note)

            // Optimistically update in-memory state so UI reflects change immediately
            // without waiting for a full refresh
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
        reviewNote = get("reviewNote") as? String ?: ""
    )
}