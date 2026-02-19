package org.sammomanyi.mediaccess.features.pharmacy.data

import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.sammomanyi.mediaccess.features.pharmacy.domain.model.PharmacyQueueEntry
import org.sammomanyi.mediaccess.features.pharmacy.domain.model.PharmacyStatus

class PharmacyQueueRepository(private val firestore: FirebaseFirestore?) {

    suspend fun addToPharmacyQueue(
        patientUserId: String,
        patientName: String,
        patientEmail: String,
        prescriptionId: String,
        date: String
    ): Result<String> = runCatching {
        val fs = firestore ?: throw Exception("Firestore not available")

        // Get current queue count for today
        val existing = fs.collection("pharmacy_queue")
            .where { "date" equalTo date }
            .where { "status" notEqualTo PharmacyStatus.COMPLETED.name }
            .get()
            .documents

        val position = existing.size + 1
        val docRef = fs.collection("pharmacy_queue").document

        fs.collection("pharmacy_queue").document(docRef.id).set(
            mapOf(
                "id" to docRef.id,
                "patientUserId" to patientUserId,
                "patientName" to patientName,
                "patientEmail" to patientEmail,
                "prescriptionId" to prescriptionId,
                "queuePosition" to position,
                "status" to PharmacyStatus.WAITING.name,
                "assignedAt" to System.currentTimeMillis(),
                "date" to date
            )
        )
        docRef.id
    }

    fun observePatientPharmacyQueue(patientUserId: String): Flow<PharmacyQueueEntry?> {
        val fs = firestore ?: return kotlinx.coroutines.flow.flowOf(null)
        return fs.collection("pharmacy_queue")
            .where { "patientUserId" equalTo patientUserId }
            .snapshots
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { doc ->
                        try {
                            PharmacyQueueEntry(
                                id = doc.get("id"),
                                patientUserId = doc.get("patientUserId"),
                                patientName = doc.get("patientName"),
                                patientEmail = doc.get("patientEmail"),
                                prescriptionId = doc.get("prescriptionId"),
                                queuePosition = (doc.get<Long>("queuePosition")).toInt(),
                                status = PharmacyStatus.valueOf(doc.get("status")),
                                assignedAt = doc.get<Long>("assignedAt"),
                                dispensedAt = runCatching { doc.get<Long?>("dispensedAt") }.getOrNull(),
                                date = doc.get("date")
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    .filter { it.status != PharmacyStatus.COMPLETED }
                    .minByOrNull { it.assignedAt }
            }
    }

    suspend fun updatePharmacyQueueStatus(
        entryId: String,
        status: PharmacyStatus
    ): Result<Unit> = runCatching {
        val fs = firestore ?: throw Exception("Firestore not available")
        val updates = mutableMapOf<String, Any?>("status" to status.name)
        if (status == PharmacyStatus.COMPLETED) {
            updates["dispensedAt"] = System.currentTimeMillis()
        }
        fs.collection("pharmacy_queue").document(entryId).update(updates)
    }
}