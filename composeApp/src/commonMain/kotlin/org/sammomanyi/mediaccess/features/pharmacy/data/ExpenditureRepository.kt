package org.sammomanyi.mediaccess.features.pharmacy.data

import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.sammomanyi.mediaccess.features.pharmacy.domain.model.Expenditure
import kotlin.runCatching

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

//    fun observeUserExpenditures(userId: String): Flow<List<Expenditure>> {
//        val fs = firestore ?: return kotlinx.coroutines.flow.flowOf(emptyList())
//
//        return fs.collection("expenditures")
//            .where { "patientUserId" equalTo userId }
//            .snapshots
//            .map { snapshot ->
//                snapshot.documents.mapNotNull { doc ->
//                    try {
//                        Expenditure(
//                            id = doc.get("id"),
//                            patientUserId = doc.get("patientUserId"),
//                            patientName = doc.get("patientName"),
//                            patientEmail = doc.get("patientEmail"),
//                            doctorId = doc.get("doctorId"),
//                            doctorName = doc.get("doctorName"),
//
//                            // ✅ Fix: use runCatching for fields that might be missing
//                            pharmacistId = runCatching { doc.get<String?>("pharmacistId") }.getOrNull(),
//                            pharmacistName = runCatching { doc.get<String?>("pharmacistName") }.getOrNull(),
//                            prescriptionId = runCatching { doc.get<String?>("prescriptionId") }.getOrNull(),
//
//                            hospitalName = doc.get("hospitalName"),
//                            visitType = doc.get("visitType"),
//
//                            // ✅ Fix: Cast to Number first to handle Int/Double/Long safely
//                            consultationFee = (doc.get<Number>("consultationFee")).toDouble(),
//                            medicationCost = (doc.get<Number>("medicationCost")).toDouble(),
//                            totalAmount = (doc.get<Number>("totalAmount")).toDouble(),
//                            coverUsed = (doc.get<Number>("coverUsed")).toDouble(),
//                            outOfPocket = (doc.get<Number>("outOfPocket")).toDouble(),
//                            coverBalanceBefore = (doc.get<Number>("coverBalanceBefore")).toDouble(),
//                            coverBalanceAfter = (doc.get<Number>("coverBalanceAfter")).toDouble(),
//
//                            insuranceName = doc.get("insuranceName"),
//                            memberNumber = doc.get("memberNumber"),
//                            date = doc.get("date"),
//
//                            // ✅ Fix: REST API often sends Longs as Doubles
//                            timestamp = (doc.get<Number>("timestamp")).toLong()
//                        )
//                    } catch (e: Exception) {
//                        println("🔴 Error parsing expenditure ${doc.id}: ${e.message}")
//                        null
//                    }
//                }.sortedByDescending { it.timestamp }
//            }
//    }
fun observeUserExpenditures(userId: String): Flow<List<Expenditure>> {
    val fs = firestore ?: return kotlinx.coroutines.flow.flowOf(emptyList())

    return fs.collection("expenditures")
        .where { "patientUserId" equalTo userId }
        .snapshots
        .map { snapshot ->
            snapshot.documents.mapNotNull { doc ->
                try {
                    // The strict GitLive fallback parser
                    fun getSafeDouble(field: String): Double {
                        return runCatching { doc.get<Double>(field) }
                            .recoverCatching { doc.get<Long>(field).toDouble() }
                            .recoverCatching { doc.get<Int>(field).toDouble() }
                            .getOrDefault(0.0)
                    }

                    fun getSafeLong(field: String): Long {
                        return runCatching { doc.get<Long>(field) }
                            .recoverCatching { doc.get<Double>(field).toLong() }
                            .recoverCatching { doc.get<Int>(field).toLong() }
                            .getOrDefault(0L)
                    }

                    // 🔥 ADD THESE DEBUG LINES HERE 🔥
                    println("🔥 EXPENDITURE DEBUG - Parsing Document: ${doc.id}")

                    val rawConsultFee = getSafeDouble("consultationFee")
                    val rawMedsCost = getSafeDouble("medicationCost")
                    val rawTime = getSafeLong("timestamp")

                    println("   -> consultationFee parsed as: $rawConsultFee")
                    println("   -> medicationCost parsed as: $rawMedsCost")
                    println("   -> timestamp parsed as: $rawTime")
                    // 🔥 END DEBUG LINES 🔥

                    Expenditure(
                        id = doc.get("id"),
                        patientUserId = doc.get("patientUserId"),
                        patientName = doc.get("patientName"),
                        patientEmail = doc.get("patientEmail"),
                        doctorId = doc.get("doctorId"),
                        doctorName = doc.get("doctorName"),

                        // Safe parsing for fields that might be missing
                        pharmacistId = runCatching { doc.get<String?>("pharmacistId") }.getOrNull(),
                        pharmacistName = runCatching { doc.get<String?>("pharmacistName") }.getOrNull(),
                        prescriptionId = runCatching { doc.get<String?>("prescriptionId") }.getOrNull(),

                        hospitalName = doc.get("hospitalName"),
                        visitType = doc.get("visitType"),

                        // Safe parsing using the helpers
                        consultationFee = getSafeDouble("consultationFee"),
                        medicationCost = getSafeDouble("medicationCost"),
                        totalAmount = getSafeDouble("totalAmount"),
                        coverUsed = getSafeDouble("coverUsed"),
                        outOfPocket = getSafeDouble("outOfPocket"),
                        coverBalanceBefore = getSafeDouble("coverBalanceBefore"),
                        coverBalanceAfter = getSafeDouble("coverBalanceAfter"),

                        insuranceName = doc.get("insuranceName"),
                        memberNumber = doc.get("memberNumber"),
                        date = doc.get("date"),

                        timestamp = getSafeLong("timestamp")
                    )
                } catch (e: Exception) {
                    println("🔴 Error parsing expenditure ${doc.id}: ${e.message}")
                    null
                }
            }.sortedByDescending { it.timestamp }
        }
}
}