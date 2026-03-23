package org.sammomanyi.mediaccess.features.cover.data

import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import org.sammomanyi.mediaccess.features.cover.data.local.CoverLinkRequestDao
import org.sammomanyi.mediaccess.features.cover.data.local.CoverLinkRequestEntity
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverLinkRequest
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverStatus
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CoverRepository(
    private val dao: CoverLinkRequestDao?,
    private val firestore: FirebaseFirestore?
) {
    // ✅ REAL-TIME USER REQUESTS
    fun getUserRequests(userId: String): Flow<List<CoverLinkRequest>> {
        if (dao == null) {
            // ✅ Mobile: Real-time Firestore listener
            return callbackFlow {
                try {
                    firestore?.collection("cover_requests")
                        ?.where { "userId" equalTo userId }
                        ?.snapshots  // ✅ Real-time updates!
                        ?.collect { snapshot ->
                            val requests = snapshot.documents.map { doc ->
                                doc.data<CoverLinkRequestEntity>().toDomain()
                            }
                            trySend(requests)
                        }
                } catch (e: Exception) {
                    println("🔴 Error fetching cover requests: ${e.message}")
                    trySend(emptyList())
                }
                awaitClose { }
            }
        }

        // ✅ Desktop: Room DAO
        return dao.getRequestsForUser(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

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
            dao?.upsert(entity)

            firestore?.collection("cover_requests")
                ?.document(request.id)
                ?.set(entity.toFirestoreMap())

            Result.success(request)
        } catch (e: Exception) {
            println("🔴 CoverRepository.submitRequest error: ${e.message}")
            Result.failure(e)
        }
    }

    // ✅ REAL-TIME ALL REQUESTS (for admin)
    fun getAllRequests(): Flow<List<CoverLinkRequest>> {
        if (dao == null) {
            return callbackFlow {
                try {
                    firestore?.collection("cover_requests")
                        ?.snapshots  // ✅ Real-time!
                        ?.collect { snapshot ->
                            val requests = snapshot.documents.map { doc ->
                                doc.data<CoverLinkRequestEntity>().toDomain()
                            }
                            trySend(requests)
                        }
                } catch (e: Exception) {
                    println("🔴 Error fetching all requests: ${e.message}")
                    trySend(emptyList())
                }
                awaitClose { }
            }
        }

        return dao.getAllRequests().map { list -> list.map { it.toDomain() } }
    }

    // ✅ REAL-TIME PENDING REQUESTS
    fun getPendingRequests(): Flow<List<CoverLinkRequest>> {
        if (dao == null) {
            return callbackFlow {
                try {
                    firestore?.collection("cover_requests")
                        ?.where { "status" equalTo CoverStatus.PENDING.name }
                        ?.snapshots  // ✅ Real-time!
                        ?.collect { snapshot ->
                            val requests = snapshot.documents.map { doc ->
                                doc.data<CoverLinkRequestEntity>().toDomain()
                            }
                            trySend(requests)
                        }
                } catch (e: Exception) {
                    println("🔴 Error fetching pending requests: ${e.message}")
                    trySend(emptyList())
                }
                awaitClose { }
            }
        }

        return dao.getPendingRequests().map { list -> list.map { it.toDomain() } }
    }

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
            dao?.updateStatus(id, status.name, now, note)

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

    suspend fun syncFromFirestore(userId: String) {
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

    suspend fun updateCoverBalance(
        coverRequestId: String,
        newBalance: Double,
        amountSpent: Double
    ) {
        val fs = firestore ?: return

        try {
            val doc = fs.collection("cover_requests").document(coverRequestId).get()
            val currentSpent = (doc.get<Number?>("totalSpent") ?: 0).toDouble()

            fs.collection("cover_requests").document(coverRequestId).update(
                mapOf(
                    "remainingBalance" to newBalance,
                    "totalSpent" to (currentSpent + amountSpent)
                )
            )

            println("✅ COVER: Updated balance - Remaining: $newBalance, Total spent: ${currentSpent + amountSpent}")
        } catch (e: Exception) {
            println("🔴 COVER: Failed to update balance: ${e.message}")
        }
    }
    suspend fun reviewRequest(requestId: String, isApproved: Boolean, note: String) {
        val fs = firestore ?: return

        try {
            val newStatus = if (isApproved) CoverStatus.APPROVED else CoverStatus.REJECTED

            fs.collection("cover_requests").document(requestId).update(
                mapOf(
                    "status" to newStatus.name,
                    "reviewedAt" to System.currentTimeMillis(),
                    "reviewNote" to note,
                    // ✅ Set initial balances when approved
                    "coverAmount" to 100000.0,
                    "remainingBalance" to 100000.0,
                    "totalSpent" to 0.0
                )
            )

            println("✅ COVER: Request reviewed - Status: $newStatus, Balance: 100000.0")
        } catch (e: Exception) {
            println("🔴 COVER: Failed to review request: ${e.message}")
        }
    }
}

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