
package org.sammomanyi.mediaccess.features.queue.data

import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.sammomanyi.mediaccess.app.DateProvider
import org.sammomanyi.mediaccess.features.queue.domain.model.QueueEntry

class QueueRepository(
    private val firestore: FirebaseFirestore?
) {
    fun observePatientQueueEntry(patientUserId: String): Flow<QueueEntry?> {
        val fs = firestore ?: return flowOf(null)
        val today = DateProvider.today()

        // ✅ SIMPLE: Just query by patientUserId, filter date in code
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
                            println("🔴 QueueRepo error: ${e.message}")
                            null
                        }
                    }
                    .filter { it.date == today } // ✅ Filter by date in code
                    .maxByOrNull { it.assignedAt }
            }
    }


    // Helper to get today's date string
    companion object {

        fun todayString(): String {
            return DateProvider.today()


        }
    }
}