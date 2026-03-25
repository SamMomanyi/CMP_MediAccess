package org.sammomanyi.mediaccess.features.pharmacy.data.desktop

import kotlinx.coroutines.flow.firstOrNull
import org.sammomanyi.mediaccess.features.cover.data.desktop.DesktopCoverRepository
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

    // ⬇️ ADD THESE TWO FUNCTIONS TO THE BOTTOM OF THE CLASS ⬇️

    suspend fun createPrescription(prescription: Prescription): Result<String> {
        return try {
            val id = java.util.UUID.randomUUID().toString()
            val presWithId = prescription.copy(id = id)

            // Convert medications list to a list of maps for Firestore JSON
            val medsList = presWithId.medications.map { med ->
                mapOf(
                    "medicationName" to med.medicationName,
                    "dosage" to med.dosage,
                    "frequency" to med.frequency,
                    "duration" to med.duration,
                    "quantity" to med.quantity,
                    "unitPrice" to med.unitPrice
                )
            }

            firestoreClient.updateDocument(
                collection = "prescriptions",
                documentId = id,
                fields = mapOf(
                    "id" to id,
                    "patientUserId" to presWithId.patientUserId,
                    "patientName" to presWithId.patientName,
                    "patientEmail" to presWithId.patientEmail,
                    "doctorId" to presWithId.doctorId,
                    "doctorName" to presWithId.doctorName,
                    "queueEntryId" to presWithId.queueEntryId,
                    "medications" to medsList,
                    "notes" to presWithId.notes,
                    "status" to presWithId.status.name,
                    "createdAt" to presWithId.createdAt,
                    "date" to presWithId.date
                )
            )
            Result.success(id)
        } catch (e: Exception) {
            println("🔴 Error creating prescription: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun addToPharmacyQueue(
        patientUserId: String,
        patientName: String,
        patientEmail: String,
        prescriptionId: String,
        date: String
    ): Result<Unit> {
        return try {
            println("🟪 PHARMACY REPO: Adding to queue - User: $patientUserId, Date: $date")

            val currentQueue = getPharmacyQueue(date)
            println("🟪 PHARMACY REPO: Current queue size: ${currentQueue.size}")

            val nextPosition = (currentQueue.maxOfOrNull { it.queuePosition } ?: 0) + 1
            val id = java.util.UUID.randomUUID().toString()

            println("🟪 PHARMACY REPO: Assigning position #$nextPosition, ID: $id")

            firestoreClient.updateDocument(
                collection = "pharmacy_queue",
                documentId = id,
                fields = mapOf(
                    "id" to id,
                    "patientUserId" to patientUserId,
                    "patientName" to patientName,
                    "patientEmail" to patientEmail,
                    "prescriptionId" to prescriptionId,
                    "queuePosition" to nextPosition,
                    "status" to PharmacyStatus.WAITING.name,
                    "assignedAt" to System.currentTimeMillis(),
                    "date" to date
                )
            )

            println("✅ PHARMACY REPO: Successfully added to Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            println("🔴 PHARMACY REPO: Error adding to queue: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun markAsDispensedWithExpenditure(
        queueEntryId: String,
        prescriptionId: String,
        totalCost: Double,
        pharmacistId: String,
        pharmacistName: String,
        expenditureRepository: DesktopExpenditureRepository,
        coverRepository: DesktopCoverRepository
    ): Result<Unit> = runCatching {
        println("🟪 PHARMACY: Starting billing for queue entry: $queueEntryId")

        // 1. Get prescription details
        val prescription = getPrescription(prescriptionId) ?: throw Exception("Prescription not found")

        // 2. Get patient's approved cover
        val patientCover = coverRepository.getApprovedCoverForUser(prescription.patientUserId)
            ?: throw Exception("No approved cover found for patient")

        val consultationFee = 500.0
        val medicationCost = totalCost
        val totalAmount = consultationFee + medicationCost

        val coverBalanceBefore = patientCover.remainingBalance
        val coverBalanceAfter = (coverBalanceBefore - totalAmount).coerceAtLeast(0.0)
        val coverUsed = coverBalanceBefore - coverBalanceAfter
        val outOfPocket = (totalAmount - coverUsed).coerceAtLeast(0.0)

        println("🟪 PHARMACY: Billing breakdown - Total: $totalAmount, Cover: $coverUsed, OOP: $outOfPocket")

        // 3. Create expenditure record
        val expenditure = org.sammomanyi.mediaccess.features.pharmacy.domain.model.Expenditure(
            patientUserId = prescription.patientUserId,
            patientName = prescription.patientName,
            patientEmail = prescription.patientEmail,
            doctorId = prescription.doctorId,
            doctorName = prescription.doctorName,
            pharmacistId = pharmacistId,
            pharmacistName = pharmacistName,
            visitType = "Consultation",
            prescriptionId = prescriptionId,
            consultationFee = consultationFee,
            medicationCost = medicationCost,
            totalAmount = totalAmount,
            coverUsed = coverUsed,
            outOfPocket = outOfPocket,
            insuranceName = patientCover.insuranceName,
            memberNumber = patientCover.memberNumber,
            coverBalanceBefore = coverBalanceBefore,
            coverBalanceAfter = coverBalanceAfter,
            date = org.sammomanyi.mediaccess.features.queue.data.QueueRepository.todayString(),
            timestamp = System.currentTimeMillis()
        )

        expenditureRepository.createExpenditure(expenditure).getOrThrow()

        // 4. Update cover balance
        coverRepository.updateCoverBalance(
            coverRequestId = patientCover.id,
            newBalance = coverBalanceAfter,
            amountSpent = totalAmount
        )

        // 5. Mark pharmacy queue as completed
        markAsDispensed(queueEntryId, prescriptionId, totalCost)

        println("✅ PHARMACY: Billing complete! New balance: $coverBalanceAfter")
    }
}