package org.sammomanyi.mediaccess.features.pharmacy.data.desktop

import org.sammomanyi.mediaccess.features.cover.data.desktop.FirestoreRestClient
import org.sammomanyi.mediaccess.features.pharmacy.domain.model.PharmacyQueueEntry
import org.sammomanyi.mediaccess.features.pharmacy.domain.model.PharmacyStatus
import org.sammomanyi.mediaccess.features.pharmacy.domain.model.Prescription
import org.sammomanyi.mediaccess.features.pharmacy.domain.model.PrescriptionItem
import org.sammomanyi.mediaccess.features.pharmacy.domain.model.PrescriptionStatus

class PharmacyDesktopRepository(
    private val firestoreClient: FirestoreRestClient
) {

    suspend fun getPharmacyQueue(date: String): List<PharmacyQueueEntry> {
        val entries = firestoreClient.getCollectionWithIds("pharmacy_queue")
        return entries.mapNotNull { (id, fields) ->
            try {
                val entryDate = fields["date"] as? String ?: return@mapNotNull null
                val status = fields["status"] as? String ?: return@mapNotNull null
                if (entryDate != date) return@mapNotNull null
                if (status == PharmacyStatus.COMPLETED.name) return@mapNotNull null

                PharmacyQueueEntry(
                    id = id,
                    patientUserId = fields["patientUserId"] as String,
                    patientName = fields["patientName"] as String,
                    patientEmail = fields["patientEmail"] as String,
                    prescriptionId = fields["prescriptionId"] as String,
                    queuePosition = (fields["queuePosition"] as Number).toInt(),
                    status = PharmacyStatus.valueOf(status),
                    assignedAt = (fields["assignedAt"] as Number).toLong(),
                    dispensedAt = (fields["dispensedAt"] as? Number)?.toLong(),
                    date = entryDate
                )
            } catch (e: Exception) {
                println("Error parsing pharmacy queue entry: ${e.message}")
                null
            }
        }.sortedBy { it.queuePosition }
    }

    suspend fun getPrescription(prescriptionId: String): Prescription? {
        val doc = firestoreClient.getDocument("prescriptions", prescriptionId) ?: return null
        return try {
            @Suppress("UNCHECKED_CAST")
            val medicationsList = doc["medications"] as? List<Map<String, Any?>> ?: emptyList()

            Prescription(
                id = doc["id"] as String,
                patientUserId = doc["patientUserId"] as String,
                patientName = doc["patientName"] as String,
                patientEmail = doc["patientEmail"] as String,
                doctorId = doc["doctorId"] as String,
                doctorName = doc["doctorName"] as String,
                queueEntryId = doc["queueEntryId"] as String,
                medications = medicationsList.map {
                    PrescriptionItem(
                        medicationName = it["medicationName"] as String,
                        dosage = it["dosage"] as String,
                        frequency = it["frequency"] as String,
                        duration = it["duration"] as String,
                        quantity = (it["quantity"] as Number).toInt(),
                        unitPrice = (it["unitPrice"] as? Number)?.toDouble() ?: 0.0
                    )
                },
                notes = doc["notes"] as String,
                status = PrescriptionStatus.valueOf(doc["status"] as String),
                createdAt = (doc["createdAt"] as Number).toLong(),
                dispensedAt = (doc["dispensedAt"] as? Number)?.toLong(),
                totalCost = (doc["totalCost"] as? Number)?.toDouble(),
                date = doc["date"] as String
            )
        } catch (e: Exception) {
            println("Error parsing prescription: ${e.message}")
            null
        }
    }

    suspend fun markAsDispensed(
        queueEntryId: String,
        prescriptionId: String,
        totalCost: Double
    ): Result<Unit> = runCatching {
        // Update pharmacy queue
        firestoreClient.updateDocument(
            "pharmacy_queue",
            queueEntryId,
            mapOf(
                "status" to PharmacyStatus.COMPLETED.name,
                "dispensedAt" to System.currentTimeMillis()
            )
        )

        // Update prescription
        firestoreClient.updateDocument(
            "prescriptions",
            prescriptionId,
            mapOf(
                "status" to PrescriptionStatus.COMPLETED.name,
                "dispensedAt" to System.currentTimeMillis(),
                "totalCost" to totalCost
            )
        )
    }
}