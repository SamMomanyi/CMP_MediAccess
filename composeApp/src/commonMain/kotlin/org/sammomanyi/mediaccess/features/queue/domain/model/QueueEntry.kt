// ─────────────────────────────────────────────────────────────
// FILE: commonMain/.../features/queue/domain/model/QueueEntry.kt
// ─────────────────────────────────────────────────────────────
package org.sammomanyi.mediaccess.features.queue.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class QueueEntry(
    val id: String = "",
    val patientUserId: String = "",
    val patientName: String = "",
    val patientEmail: String = "",
    val visitCodeId: String = "",
    val purpose: String = "",               // "CONSULTATION" | "PHARMACY"
    val doctorId: String = "",
    val doctorName: String = "",
    val roomNumber: String = "",
    val status: String = "WAITING",         // "WAITING" | "IN_PROGRESS" | "DONE"
    val queuePosition: Int = 0,
    val insuranceName: String = "",
    val memberNumber: String = "",
    val assignedAt: Long = 0L,
    val calledAt: Long? = null,
    val completedAt: Long? = null,
    val date: String = ""                   // "2026-02-16"
)

enum class QueueStatus { WAITING, IN_PROGRESS, DONE }
