package org.sammomanyi.mediaccess.features.cover.data

import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import org.sammomanyi.mediaccess.features.cover.data.local.CoverLinkRequestDao
import org.sammomanyi.mediaccess.features.cover.data.local.CoverLinkRequestEntity
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverLinkRequest
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverStatus
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CoverRepository(
    private val dao: CoverLinkRequestDao?,  // ✅ Make nullable - desktop only
    private val firestore: FirebaseFirestore?
) {
    // ── User: get their own requests ──────────────────────────

    fun getUserRequests(userId: String): Flow<List<CoverLinkRequest>> {
        // ✅ Mobile: use Firestore only
        if (dao == null) {
            return flow {
                try {
                    val snapshot = firestore?.collection("cover_requests")
                        ?.where { "userId" equalTo userId }
                        ?.get()

                    val requests = snapshot?.documents?.map { doc ->
                        doc.data<CoverLinkRequestEntity>().toDomain()
                    } ?: emptyList()

                    emit(requests)
                } catch (e: Exception) {
                    println("🔴 Error fetching cover requests: ${e.message}")
                    emit(emptyList())
                }
            }
        }

        // ✅ Desktop: use Room DAO
        return dao.getRequestsForUser(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    // ── Submit a new pending request ──────────────────────────

    @OptIn(ExperimentalUuidApi::class)
    suspend fun submitRequest(
        userId: String,
        userEmail: String,
        country: String,
        insuranceName: String,
        memberNumber: String
    ): Result<CoverLinkRequest> {
        return try {
            val request = CoverLinkRequest(
                id = Uuid.random().toString(),
                userId = userId,
                userEmail = userEmail,
                country = country,
                insuranceName = insuranceName,
                memberNumber = memberNumber,
                status = CoverStatus.PENDING,
                submittedAt = Clock.System.now().toEpochMilliseconds()
            )

            val entity = request.toEntity()

            // ✅ Save locally ONLY on desktop
            dao?.upsert(entity)

            // ✅ Always sync to Firestore
            firestore?.collection("cover_requests")
                ?.document(request.id)
                ?.set(entity.toFirestoreMap())

            Result.success(request)
        } catch (e: Exception) {
            println("🔴 CoverRepository.submitRequest error: ${e.message}")
            Result.failure(e)
        }
    }

    // ── Admin: get all requests ───────────────────────────────

    fun getAllRequests(): Flow<List<CoverLinkRequest>> {
        // ✅ Mobile: use Firestore
        if (dao == null) {
            return flow {
                try {
                    val snapshot = firestore?.collection("cover_requests")?.get()
                    val requests = snapshot?.documents?.map { doc ->
                        doc.data<CoverLinkRequestEntity>().toDomain()
                    } ?: emptyList()
                    emit(requests)
                } catch (e: Exception) {
                    println("🔴 Error fetching all requests: ${e.message}")
                    emit(emptyList())
                }
            }
        }

        // ✅ Desktop: use Room DAO
        return dao.getAllRequests().map { list -> list.map { it.toDomain() } }
    }

    fun getPendingRequests(): Flow<List<CoverLinkRequest>> {
        // ✅ Mobile: use Firestore
        if (dao == null) {
            return flow {
                try {
                    val snapshot = firestore?.collection("cover_requests")
                        ?.where { "status" equalTo CoverStatus.PENDING.name }
                        ?.get()

                    val requests = snapshot?.documents?.map { doc ->
                        doc.data<CoverLinkRequestEntity>().toDomain()
                    } ?: emptyList()

                    emit(requests)
                } catch (e: Exception) {
                    println("🔴 Error fetching pending requests: ${e.message}")
                    emit(emptyList())
                }
            }
        }

        // ✅ Desktop: use Room DAO
        return dao.getPendingRequests().map { list -> list.map { it.toDomain() } }
    }

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

            // ✅ Update locally ONLY on desktop
            dao?.updateStatus(id, status.name, now, note)

            // ✅ Always update Firestore
            firestore?.collection("cover_requests")
                ?.document(id)
                ?.update(
                    "status" to status.name,
                    "reviewedAt" to now,
                    "reviewNote" to note
                )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Sync from Firestore to local Room (desktop only) ──────

    suspend fun syncFromFirestore(userId: String) {
        // ✅ Skip if no DAO (mobile)
        if (dao == null) return

        try {
            val snapshot = firestore?.collection("cover_requests")
                ?.where { "userId" equalTo userId }
                ?.get()

            snapshot?.documents?.forEach { doc ->
                val entity = doc.data<CoverLinkRequestEntity>()
                dao.upsert(entity)
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