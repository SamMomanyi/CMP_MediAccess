
package org.sammomanyi.mediaccess.features.queue.data

import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.sammomanyi.mediaccess.features.queue.domain.model.QueueEntry
import org.sammomanyi.mediaccess.features.queue.domain.model.QueueStatus

class QueueRepository(
    private val firestore: FirebaseFirestore?
) {
    // ── Real-time listener for patient's active queue entry ──
    // Used by mobile CheckInScreen to get live position + status
    fun observePatientQueueEntry(patientUserId: String): Flow<QueueEntry?> {
        val fs = firestore ?: return flowOf(null)
        return fs.collection("queue_entries")
            .where { "patientUserId" equalTo patientUserId }
            .snapshots
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { doc ->
                        try {
                            QueueEntry(
                                id = doc.id,
                                patientUserId = doc.get("patientUserId"),
                                patientName = doc.get("patientName"),
                                patientEmail = doc.get("patientEmail"),
                                visitCodeId = doc.get("visitCodeId"),
                                purpose = doc.get("purpose"),
                                doctorId = doc.get("doctorId"),
                                doctorName = doc.get("doctorName"),
                                roomNumber = doc.get("roomNumber"),
                                status = doc.get("status"),
                                queuePosition = (doc.get<Long>("queuePosition")).toInt(),
                                insuranceName = doc.get("insuranceName"),
                                memberNumber = doc.get("memberNumber"),
                                assignedAt = doc.get<Long>("assignedAt"),
                                calledAt = runCatching { doc.get<Long?>("calledAt") }.getOrNull(),
                                completedAt = runCatching { doc.get<Long?>("completedAt") }.getOrNull(),
                                date = doc.get("date")
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    .filter { it.status != QueueStatus.DONE.name }
                    .minByOrNull { it.queuePosition }  // Their most recent active entry
            }
    }

    // Helper to get today's date string
    companion object {
        fun todayString(): String {
            val now = Clock.System.now()
            val date = now.toLocalDateTime(TimeZone.currentSystemDefault())
            return "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${
                date.dayOfMonth.toString().padStart(2, '0')
            }"
        }
    }
}