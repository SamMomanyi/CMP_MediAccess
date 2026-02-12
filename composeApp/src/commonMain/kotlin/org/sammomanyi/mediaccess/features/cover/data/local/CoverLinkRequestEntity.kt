package org.sammomanyi.mediaccess.features.cover.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cover_link_requests")
data class CoverLinkRequestEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userEmail: String,
    val country: String,
    val insuranceName: String,
    val memberNumber: String,
    val status: String,       // "PENDING" | "APPROVED" | "REJECTED"
    val submittedAt: Long,
    val reviewedAt: Long?,
    val reviewNote: String
)