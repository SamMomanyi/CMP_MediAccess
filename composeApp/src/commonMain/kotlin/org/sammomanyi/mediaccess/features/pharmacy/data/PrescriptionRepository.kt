package org.sammomanyi.mediaccess.features.pharmacy.data

import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.sammomanyi.mediaccess.features.pharmacy.domain.model.Prescription
import org.sammomanyi.mediaccess.features.pharmacy.domain.model.PrescriptionStatus

class PrescriptionRepository(private val firestore: FirebaseFirestore?) {

    suspend fun createPrescription(prescription: Prescription): Result<String> = runCatching {
        val fs = firestore ?: throw Exception("Firestore not available")
        val docRef = fs.collection("prescriptions").document

        fs.collection("prescriptions").document(docRef.id).set(
            mapOf(
                "id" to docRef.id,
                "patientUserId" to prescription.patientUserId,
                "patientName" to prescription.patientName,
                "patientEmail" to prescription.patientEmail,
                "doctorId" to prescription.doctorId,
                "doctorName" to prescription.doctorName,
                "queueEntryId" to prescription.queueEntryId,
                "medications" to prescription.medications.map {
                    mapOf(
                        "medicationName" to it.medicationName,
                        "dosage" to it.dosage,
                        "frequency" to it.frequency,
                        "duration" to it.duration,
                        "quantity" to it.quantity,
                        "unitPrice" to it.unitPrice
                    )
                },
                "notes" to prescription.notes,
                "status" to PrescriptionStatus.PENDING.name,
                "createdAt" to prescription.createdAt,
                "date" to prescription.date
            )
        )
        docRef.id
    }

    fun observePrescription(prescriptionId: String): Flow<Prescription?> {
        val fs = firestore ?: return kotlinx.coroutines.flow.flowOf(null)
        return fs.collection("prescriptions")
            .document(prescriptionId)
            .snapshots
            .map { doc ->
                if (!doc.exists) return@map null
                try {
                    Prescription(
                        id = doc.get("id"),
                        patientUserId = doc.get("patientUserId"),
                        patientName = doc.get("patientName"),
                        patientEmail = doc.get("patientEmail"),
                        doctorId = doc.get("doctorId"),
                        doctorName = doc.get("doctorName"),
                        queueEntryId = doc.get("queueEntryId"),
                        medications = (doc.get<List<Map<String, Any?>>>("medications")).map {
                            org.sammomanyi.mediaccess.features.pharmacy.domain.model.PrescriptionItem(
                                medicationName = it["medicationName"] as String,
                                dosage = it["dosage"] as String,
                                frequency = it["frequency"] as String,
                                duration = it["duration"] as String,
                                quantity = (it["quantity"] as Long).toInt(),
                                unitPrice = (it["unitPrice"] as? Number)?.toDouble() ?: 0.0
                            )
                        },
                        notes = doc.get("notes"),
                        status = PrescriptionStatus.valueOf(doc.get("status")),
                        createdAt = doc.get<Long>("createdAt"),
                        dispensedAt = runCatching { doc.get<Long?>("dispensedAt") }.getOrNull(),
                        totalCost = runCatching { doc.get<Number?>("totalCost")?.toDouble() }.getOrNull(),
                        date = doc.get("date")
                    )
                } catch (e: Exception) {
                    println("Error parsing prescription: ${e.message}")
                    null
                }
            }
    }

    suspend fun updatePrescriptionStatus(
        prescriptionId: String,
        status: PrescriptionStatus,
        totalCost: Double? = null
    ): Result<Unit> = runCatching {
        val fs = firestore ?: throw Exception("Firestore not available")
        val updates = mutableMapOf<String, Any?>(
            "status" to status.name
        )
        if (status == PrescriptionStatus.COMPLETED) {
            updates["dispensedAt"] = System.currentTimeMillis()
            updates["totalCost"] = totalCost
        }
        fs.collection("prescriptions").document(prescriptionId).update(updates)
    }
}