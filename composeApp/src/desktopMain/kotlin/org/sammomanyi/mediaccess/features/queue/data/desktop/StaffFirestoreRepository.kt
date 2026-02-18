package org.sammomanyi.mediaccess.features.queue.data.desktop

import kotlinx.datetime.Clock
import org.sammomanyi.mediaccess.features.cover.data.desktop.FirestoreRestClient
import org.sammomanyi.mediaccess.features.queue.domain.model.StaffAccount
import org.sammomanyi.mediaccess.features.queue.domain.model.StaffRole

class StaffFirestoreRepository(
    private val firestoreClient: FirestoreRestClient
) {
    // Fetch all staff from Firestore
    suspend fun getAllStaff(): List<StaffAccount> {
        return try {
            val docs = firestoreClient.getCollectionWithIds("staff_accounts")
            docs.map { (id, fields) ->
                StaffAccount(
                    id = id,
                    name = fields["name"]?.toString() ?: "",
                    email = fields["email"]?.toString() ?: "",
                    role = fields["role"]?.toString() ?: "DOCTOR",
                    roomNumber = fields["roomNumber"]?.toString() ?: "",
                    specialization = fields["specialization"]?.toString() ?: "",
                    isOnDuty = fields["isOnDuty"] as? Boolean ?: false,
                    lastSeenAt = (fields["lastSeenAt"] as? Long) ?: 0L,
                    passwordHash = fields["passwordHash"]?.toString() ?: ""
                )
            }
        } catch (e: Exception) {
            println("🔴 StaffFirestoreRepository.getAllStaff error: ${e.message}")
            emptyList()
        }
    }

    // Only returns on-duty doctors — used by receptionist doctor picker
// Replace getOnDutyDoctors() with this:
    suspend fun getAllDoctors(): List<StaffAccount> =
        getAllStaff().filter { it.role == StaffRole.DOCTOR.name }
            .sortedBy { it.name }

    // Set a staff member's duty status (called on login/logout)
    suspend fun setOnDuty(staffId: String, isOnDuty: Boolean) {
        try {
            firestoreClient.updateDocument(
                collection = "staff_accounts",
                documentId = staffId,
                fields = mapOf(
                    "isOnDuty" to isOnDuty,
                    "lastSeenAt" to Clock.System.now().toEpochMilliseconds()
                )
            )
        } catch (e: Exception) {
            println("🔴 StaffFirestoreRepository.setOnDuty error: ${e.message}")
        }
    }

    // Create or update a staff_accounts doc from admin
    suspend fun upsertStaff(staff: StaffAccount) {
        try {
            firestoreClient.updateDocument(
                collection = "staff_accounts",
                documentId = staff.id,
                fields = mapOf(
                    "name" to staff.name,
                    "email" to staff.email,
                    "role" to staff.role,
                    "roomNumber" to staff.roomNumber,
                    "specialization" to staff.specialization,
                    "isOnDuty" to staff.isOnDuty,
                    "lastSeenAt" to staff.lastSeenAt,
                    "passwordHash" to staff.passwordHash
                )
            )
        } catch (e: Exception) {
            println("🔴 StaffFirestoreRepository.upsertStaff error: ${e.message}")
        }
    }

    suspend fun deleteStaff(staffId: String) {
        try {
            firestoreClient.deleteDocument("staff_accounts", staffId)
        } catch (e: Exception) {
            println("🔴 StaffFirestoreRepository.deleteStaff error: ${e.message}")
        }
    }
}