package org.sammomanyi.mediaccess.features.cover.data

import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.sammomanyi.mediaccess.features.cover.data.local.CoverLinkRequestDao
import org.sammomanyi.mediaccess.features.cover.data.local.CoverLinkRequestEntity
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverLinkRequest
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverStatus
import kotlin.collections.emptyList
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CoverRepository(
    private val dao: CoverLinkRequestDao?,
    private val firestore: FirebaseFirestore?
) {
    // ✅ REAL-TIME USER REQUESTS
    @OptIn(DelicateCoroutinesApi::class)
//    fun getUserRequests(userId: String): Flow<List<CoverLinkRequest>> {
//        if (dao == null) {
//            return callbackFlow {
//                try {
//                    firestore?.collection("cover_requests")
//                        ?.where { "userId" equalTo userId }
//                        ?.snapshots
//                        ?.collect { snapshot ->
//                            val requests = snapshot.documents.mapNotNull { doc ->
//                                parseFirestoreDocument(doc)
//                            }
//                            trySend(requests)
//                        }
//                } catch (e: Exception) {
//                    println("🔴 Error fetching cover requests: ${e.message}")
//                    trySend(emptyList())
//                }
//                awaitClose { }
//            }
//        }
//
////        return dao.getRequestsForUser(userId).map { list ->
////            list.map { it.toDomain() }
////        }
//        // Mobile: Emit local DB data, but force a sync with Firestore instantly
//        return kotlinx.coroutines.flow.flow {
//            dao.getRequestsForUser(userId).collect { entities ->
//                emit(entities.map { it.toDomain() })
//            }
//        }.apply {
//            // Trigger background sync to pull the fresh 87,200 balance
//            kotlinx.coroutines.GlobalScope.launch {
//                syncFromFirestore(userId)
//            }
//        }
//    }

    fun getUserRequests(userId: String): Flow<List<CoverLinkRequest>> {
        val localFlow = dao?.getRequestsForUser(userId)
            ?.map { it.map { entity -> entity.toDomain() } }
            ?: flowOf(emptyList())

        val remoteFlow = callbackFlow {
            try {
                firestore?.collection("cover_requests")
                    ?.where { "userId" equalTo userId }
                    ?.snapshots
                    ?.collect { snapshot ->
                        val requests = snapshot.documents.mapNotNull {
                            parseFirestoreDocument(it)
                        }
                        trySend(requests)
                    }
            } catch (e: Exception) {
                trySend(emptyList())
            }
            awaitClose {}
        }

        return remoteFlow // 🔥 ALWAYS prefer realtime truth
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
                ?.set(request.toFirestoreMap())

            Result.success(request)
        } catch (e: Exception) {
            println("🔴 CoverRepository.submitRequest error: ${e.message}")
            Result.failure(e)
        }
    }

    // ✅ REAL-TIME ALL REQUESTS (for admin) - CLEANED UP!
    fun getAllRequests(): Flow<List<CoverLinkRequest>> {
        if (dao == null) {
            return callbackFlow {
                try {
                    firestore?.collection("cover_requests")
                        ?.snapshots
                        ?.collect { snapshot ->
                            val requests = snapshot.documents.mapNotNull { doc ->
                                parseFirestoreDocument(doc)
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
                        ?.snapshots
                        ?.collect { snapshot ->
                            val requests = snapshot.documents.mapNotNull { doc ->
                                parseFirestoreDocument(doc)
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

//    suspend fun syncFromFirestore(userId: String) {
//        if (dao == null) return
//
//        try {
//            val snapshot = firestore?.collection("cover_requests")
//                ?.where { "userId" equalTo userId }
//                ?.get()
//
//            snapshot?.documents?.forEach { doc ->
//                val entity = doc.data<CoverLinkRequestEntity>()
//                dao.upsert(entity)
//            }
//        } catch (e: Exception) {
//            println("🔴 CoverRepository.sync error: ${e.message}")
//        }
//    }

    suspend fun syncFromFirestore(userId: String) {
        if (dao == null) return
        try {
            val snapshot = firestore?.collection("cover_requests")
                ?.where { "userId" equalTo userId }
                ?.get()

            snapshot?.documents?.forEach { doc ->
                // ✅ MUST USE parseFirestoreDocument, NOT .data()
                val domainModel = parseFirestoreDocument(doc)
                if (domainModel != null) {
                    dao.upsert(domainModel.toEntity())
                }
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
            val currentSpent =
                runCatching { (doc.get<Number?>("totalSpent") ?: 0).toDouble() }.getOrDefault(0.0)

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

    //     ✅ SAFE FIRESTORE DOCUMENT PARSER
    private fun parseFirestoreDocument(doc: dev.gitlive.firebase.firestore.DocumentSnapshot): CoverLinkRequest? {
        return try {
            // ✅ Serializer-safe number extractor
            fun getSafeDouble(field: String, default: Double): Double {
                return runCatching { doc.get<Double>(field) }
                    .recoverCatching { doc.get<Long>(field).toDouble() }
                    .recoverCatching { doc.get<Int>(field).toDouble() }
                    .getOrDefault(default)
            }
            CoverLinkRequest(
                id = doc.get("id"),
                userId = doc.get("userId"),
                userEmail = doc.get("userEmail"),
                country = doc.get("country"),
                insuranceName = doc.get("insuranceName"),
                memberNumber = doc.get("memberNumber"),
                status = CoverStatus.valueOf(doc.get("status")),
                submittedAt = doc.get<Long>("submittedAt"),
                reviewedAt = runCatching { doc.get<Long?>("reviewedAt") }.getOrNull(),
                reviewNote = doc.get<String?>("reviewNote") ?: "",
                coverAmount = getSafeDouble("coverAmount", 100000.0),
                remainingBalance = getSafeDouble("remainingBalance", 100000.0),
                totalSpent = getSafeDouble("totalSpent", 0.0)
            )
        } catch (e: Exception) {
            println("🔴 Error parsing cover request document: ${e.message}")
            null
        }
    }
}
//    private fun parseFirestoreDocument(doc: dev.gitlive.firebase.firestore.DocumentSnapshot): CoverLinkRequest? {
//        return try {
//            CoverLinkRequest(
//                id = doc.get("id"),
//                userId = doc.get("userId"),
//                userEmail = doc.get("userEmail"),
//                country = doc.get("country"),
//                insuranceName = doc.get("insuranceName"),
//                memberNumber = doc.get("memberNumber"),
//                status = CoverStatus.valueOf(doc.get("status")),
//
//                // ✅ Fix: Handle potential Double-to-Long issues from Desktop
//                submittedAt = (doc.get<Number>("submittedAt")).toLong(),
//                reviewedAt = runCatching { (doc.get<Number?>("reviewedAt"))?.toLong() }.getOrNull(),
//
//                reviewNote = doc.get<String?>("reviewNote") ?: "",
//
//                // ✅ Financial fields (Keep your runCatching logic but use Number for safety)
//                coverAmount = runCatching { (doc.get<Number?>("coverAmount") ?: 100000.0).toDouble() }.getOrDefault(100000.0),
//                remainingBalance = runCatching { (doc.get<Number?>("remainingBalance") ?: 100000.0).toDouble() }.getOrDefault(100000.0),
//                totalSpent = runCatching { (doc.get<Number?>("totalSpent") ?: 0.0).toDouble() }.getOrDefault(0.0)
//            )
//        } catch (e: Exception) {
//            println("🔴 Error parsing cover request ${doc.id}: ${e.message}")
//            null
//        }
//    }
//}

// ✅ ENTITY CONVERSION - Updated
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
    reviewNote = reviewNote,
    // Desktop entities won't have these, so use defaults
//    coverAmount = 100000.0,
//    remainingBalance = 100000.0,
//    totalSpent = 0.0
    //changed to this
    // ✅ USE REAL VALUES
    //converted to this
    coverAmount = coverAmount,
    remainingBalance = remainingBalance,
    totalSpent = totalSpent
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
    reviewNote = reviewNote,

    //so added this
    coverAmount = coverAmount,
    remainingBalance = remainingBalance,
    totalSpent = totalSpent
)

// ✅ FIRESTORE MAP - Updated to include new fields
private fun CoverLinkRequest.toFirestoreMap() = mapOf(
    "id" to id,
    "userId" to userId,
    "userEmail" to userEmail,
    "country" to country,
    "insuranceName" to insuranceName,
    "memberNumber" to memberNumber,
    "status" to status.name,
    "submittedAt" to submittedAt,
    "reviewedAt" to reviewedAt,
    "reviewNote" to reviewNote,
    "coverAmount" to coverAmount,
    "remainingBalance" to remainingBalance,
    "totalSpent" to totalSpent
)