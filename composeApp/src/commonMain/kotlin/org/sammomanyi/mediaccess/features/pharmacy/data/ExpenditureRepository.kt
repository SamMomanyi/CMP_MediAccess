package org.sammomanyi.mediaccess.features.pharmacy.data

import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.sammomanyi.mediaccess.features.pharmacy.domain.model.Expenditure

class ExpenditureRepository(private val firestore: FirebaseFirestore?) {

    suspend fun createExpenditure(expenditure: Expenditure): Result<String> = runCatching {
        val fs = firestore ?: throw Exception("Firestore not available")

        val docRef = fs.collection("expenditures").document
        val id = docRef.id

        val expendWithId = expenditure.copy(id = id)

        fs.collection("expenditures").document(id).set(
            mapOf(
                "id" to id,
                "patientUserId" to expendWithId.patientUserId,
                "patientName" to expendWithId.patientName,
                "patientEmail" to expendWithId.patientEmail,
                "doctorId" to expendWithId.doctorId,
                "doctorName" to expendWithId.doctorName,
                "pharmacistId" to expendWithId.pharmacistId,
                "pharmacistName" to expendWithId.pharmacistName,
                "hospitalName" to expendWithId.hospitalName,
                "visitType" to expendWithId.visitType,
                "prescriptionId" to expendWithId.prescriptionId,
                "consultationFee" to expendWithId.consultationFee,
                "medicationCost" to expendWithId.medicationCost,
                "totalAmount" to expendWithId.totalAmount,
                "coverUsed" to expendWithId.coverUsed,
                "outOfPocket" to expendWithId.outOfPocket,
                "insuranceName" to expendWithId.insuranceName,
                "memberNumber" to expendWithId.memberNumber,
                "coverBalanceBefore" to expendWithId.coverBalanceBefore,
                "coverBalanceAfter" to expendWithId.coverBalanceAfter,
                "date" to expendWithId.date,
                "timestamp" to expendWithId.timestamp
            )
        )

        println("✅ EXPENDITURE: Created record ID: $id, Amount: ${expendWithId.totalAmount}")
        id
    }

    fun observeUserExpenditures(userId: String): Flow<List<Expenditure>> {
        val fs = firestore ?: return kotlinx.coroutines.flow.flowOf(emptyList())

        return fs.collection("expenditures")
            .where { "patientUserId" equalTo userId }
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        Expenditure(
                            id = doc.get("id"),
                            patientUserId = doc.get("patientUserId"),
                            patientName = doc.get("patientName"),
                            patientEmail = doc.get("patientEmail"),
                            doctorId = doc.get("doctorId"),
                            doctorName = doc.get("doctorName"),
                            pharmacistId = doc.get<String?>("pharmacistId"),
                            pharmacistName = doc.get<String?>("pharmacistName"),
                            hospitalName = doc.get("hospitalName"),
                            visitType = doc.get("visitType"),
                            prescriptionId = doc.get<String?>("prescriptionId"),
                            consultationFee = (doc.get<Number>("consultationFee")).toDouble(),
                            medicationCost = (doc.get<Number>("medicationCost")).toDouble(),
                            totalAmount = (doc.get<Number>("totalAmount")).toDouble(),
                            coverUsed = (doc.get<Number>("coverUsed")).toDouble(),
                            outOfPocket = (doc.get<Number>("outOfPocket")).toDouble(),
                            insuranceName = doc.get("insuranceName"),
                            memberNumber = doc.get("memberNumber"),
                            coverBalanceBefore = (doc.get<Number>("coverBalanceBefore")).toDouble(),
                            coverBalanceAfter = (doc.get<Number>("coverBalanceAfter")).toDouble(),
                            date = doc.get("date"),
                            timestamp = doc.get<Long>("timestamp")
                        )
                    } catch (e: Exception) {
                        println("🔴 Error parsing expenditure: ${e.message}")
                        null
                    }
                }.sortedByDescending { it.timestamp }
            }
    }
}