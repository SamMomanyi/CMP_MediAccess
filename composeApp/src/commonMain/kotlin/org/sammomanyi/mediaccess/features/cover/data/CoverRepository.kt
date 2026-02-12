package org.sammomanyi.mediaccess.features.cover.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import org.sammomanyi.mediaccess.features.cover.data.local.CoverLinkRequestDao
import org.sammomanyi.mediaccess.features.cover.data.local.CoverLinkRequestEntity
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverLinkRequest
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverStatus
import java.util.UUID

class CoverRepository(
    private val dao: CoverLinkRequestDao,
    private val firestore: FirebaseFirestore
) {
    // ── User: get their own requests ──────────────────────────

    fun getUserRequests(userId: String): Flow<List<CoverLinkRequest>> =
        dao.getRequestsForUser(userId).map { list ->
            list.map { it.toDomain() }
        }

    // ── Submit a new pending request ──────────────────────────

    suspend fun submitRequest(
        userId: String,
        userEmail: String,
        country: String,
        insuranceName: String,
        memberNumber: String
    ): Result<CoverLinkRequest> {
        return try {
            val request = CoverLinkRequest(
                id = UUID.randomUUID().toString(),
                userId = userId,
                userEmail = userEmail,
                country = country,
                insuranceName = insuranceName,
                memberNumber = memberNumber,
                status = CoverStatus.PENDING,
                submittedAt = Clock.System.now().toEpochMilliseconds()
            )

            val entity = request.toEntity()

            // Save locally first
            dao.upsert(entity)

            // Sync to Firestore
            firestore.collection("cover_requests")
                .document(request.id)
                .set(entity.toFirestoreMap())
                .await()

            Result.success(request)
        } catch (e: Exception) {
            println("🔴 CoverRepository.submitRequest error: ${e.message}")
            Result.failure(e)
        }
    }

    // ── Admin: get all requests (desktop) ─────────────────────

    fun getAllRequests(): Flow<List<CoverLinkRequest>> =
        dao.getAllRequests().map { list -> list.map { it.toDomain() } }

    fun getPendingRequests(): Flow<List<CoverLinkRequest>> =
        dao.getPendingRequests().map { list -> list.map { it.toDomain() } }

    // ── Admin: approve / reject ───────────────────────────────

    suspend fun approveRequest(id: String, note: String = "Approved"): Result<Unit> =
        updateStatus(id, CoverStatus.APPROVED, note)

    suspend fun rejectRequest(id: String, note: String): Result<Unit> =
        updateStatus(id, CoverStatus.REJECTED, note)

    private suspend fun updateStatus(
        id: String,
        status: CoverStatus,
        note: String
    ): Result<Unit> {
        return try {
            val now = Clock.System.now().toEpochMilliseconds()
            dao.updateStatus(id, status.name, now, note)

            firestore.collection("cover_requests")
                .document(id)
                .update(
                    mapOf(
                        "status" to status.name,
                        "reviewedAt" to now,
                        "reviewNote" to note
                    )
                ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Sync from Firestore to local Room ─────────────────────

    suspend fun syncFromFirestore(userId: String) {
        try {
            val snapshot = firestore.collection("cover_requests")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                val entity = doc.toObject(CoverLinkRequestEntity::class.java)
                entity?.let { dao.upsert(it) }
            }
        } catch (e: Exception) {
            println("🔴 CoverRepository.sync error: ${e.message}")
        }
    }
}

// ── Mappers ───────────────────────────────────────────────────

private fun CoverLinkRequestEntity.toDomain() = CoverLinkRequest(
    id = id,
    userId = userId,
    userEmail = userEmail,
    country = country,
    insuranceName = insuranceName,
    memberNumber = memberNumber,
    status = CoverStatus.valueOf(status),
    submittedAt = submittedAt,
    reviewedAt = reviewedAt,
    reviewNote = reviewNote
)

private fun CoverLinkRequest.toEntity() = CoverLinkRequestEntity(
    id = id,
    userId = userId,
    userEmail = userEmail,
    country = country,
    insuranceName = insuranceName,
    memberNumber = memberNumber,
    status = status.name,
    submittedAt = submittedAt,
    reviewedAt = reviewedAt,
    reviewNote = reviewNote
)

private fun CoverLinkRequestEntity.toFirestoreMap() = mapOf(
    "id" to id,
    "userId" to userId,
    "userEmail" to userEmail,
    "country" to country,
    "insuranceName" to insuranceName,
    "memberNumber" to memberNumber,
    "status" to status,
    "submittedAt" to submittedAt,
    "reviewedAt" to reviewedAt,
    "reviewNote" to reviewNote
)