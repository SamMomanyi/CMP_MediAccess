package org.sammomanyi.mediaccess.features.cover.data.desktop

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import org.sammomanyi.mediaccess.features.cover.data.local.CoverLinkRequestDao
import org.sammomanyi.mediaccess.features.cover.data.local.CoverLinkRequestEntity
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverLinkRequest
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverStatus

/**
 * Desktop-only version of CoverRepository.
 * Uses Firebase Admin SDK (via FirestoreAdminClient) for full read/write access
 * that bypasses Firestore security rules — appropriate for an admin tool.
 * Keeps Room in sync as a local cache.
 */
class DesktopCoverRepository(
    private val dao: CoverLinkRequestDao,
    private val firestoreAdmin: FirestoreAdminClient
) {

    // ── Real-time stream from Firestore → auto-updates UI ─────
    fun getAllRequests(): Flow<List<CoverLinkRequest>> =
        firestoreAdmin.collectionSnapshots("cover_requests")
            .map { rawDocs ->
                rawDocs.mapNotNull { doc ->
                    try {
                        doc.toCoverLinkRequest()
                    } catch (e: Exception) {
                        println("⚠️ Skipping malformed doc: ${e.message}")
                        null
                    }
                }.sortedByDescending { it.submittedAt }
            }

    fun getPendingRequests(): Flow<List<CoverLinkRequest>> =
        getAllRequests().map { list -> list.filter { it.status == CoverStatus.PENDING } }

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

            firestoreAdmin.updateDocument(
                collection = "cover_requests",
                documentId = id,
                fields = mapOf(
                    "status" to status.name,
                    "reviewedAt" to now,
                    "reviewNote" to note
                )
            )

            // Keep local Room cache in sync so mobile reflects it after next sync
            dao.updateStatus(id, status.name, now, note)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ── Map Firestore raw document → domain model ─────────────────

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
        reviewNote = get("reviewNote") as? String
    )
}