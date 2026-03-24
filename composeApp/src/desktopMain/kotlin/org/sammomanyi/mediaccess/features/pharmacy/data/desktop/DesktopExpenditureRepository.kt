package org.sammomanyi.mediaccess.features.pharmacy.data.desktop

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.sammomanyi.mediaccess.features.cover.data.desktop.FirestoreRestClient
import org.sammomanyi.mediaccess.features.pharmacy.domain.model.Expenditure

class DesktopExpenditureRepository(
    private val firestoreClient: FirestoreRestClient
) {

    suspend fun createExpenditure(expenditure: Expenditure): Result<String> = runCatching {
        val id = java.util.UUID.randomUUID().toString()
        val expendWithId = expenditure.copy(id = id)

        firestoreClient.updateDocument(
            collection = "expenditures",
            documentId = id,
            fields = mapOf(
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

        println("✅ DESKTOP EXPENDITURE: Created record ID: $id, Amount: ${expendWithId.totalAmount}")
        id
    }

    fun observeUserExpenditures(userId: String): Flow<List<Expenditure>> {
        // Desktop doesn't need real-time updates for expenditures
        // This would only be called if you navigate to expenditure history on desktop
        return flowOf(emptyList())
    }
}