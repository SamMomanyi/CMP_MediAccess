package org.sammomanyi.mediaccess.features.cover.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CoverLinkRequestDao {

    @Query("SELECT * FROM cover_link_requests WHERE userId = :userId ORDER BY submittedAt DESC")
    fun getRequestsForUser(userId: String): Flow<List<CoverLinkRequestEntity>>

    @Query("SELECT * FROM cover_link_requests ORDER BY submittedAt DESC")
    fun getAllRequests(): Flow<List<CoverLinkRequestEntity>>

    @Query("SELECT * FROM cover_link_requests WHERE status = 'PENDING' ORDER BY submittedAt DESC")
    fun getPendingRequests(): Flow<List<CoverLinkRequestEntity>>

    @Upsert
    suspend fun upsert(entity: CoverLinkRequestEntity)

    @Query("UPDATE cover_link_requests SET status = :status, reviewedAt = :reviewedAt, reviewNote = :note WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, reviewedAt: Long, note: String)

    @Query("DELETE FROM cover_link_requests WHERE id = :id")
    suspend fun delete(id: String)
}