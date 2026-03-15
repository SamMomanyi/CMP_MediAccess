package org.sammomanyi.mediaccess.features.queue.data.desktop

import org.sammomanyi.mediaccess.features.auth.data.local.AdminAccountEntity
import org.sammomanyi.mediaccess.features.cover.data.desktop.FirestoreRestClient
import org.sammomanyi.mediaccess.features.queue.domain.model.StaffAccount
import org.sammomanyi.mediaccess.features.queue.domain.model.StaffRole
import java.security.MessageDigest

class StaffFirestoreRepository(
    private val firestoreClient: FirestoreRestClient
) {
    // ✅ Authenticate staff against Firestore
    suspend fun authenticateStaff(
        email: String,
        password: String,
        role: StaffRole
    ): AdminAccountEntity? {
        return try {
            val docs = firestoreClient.getCollectionWithIds("staff_accounts")
            val passwordHash = hashPassword(password)

            // Find matching staff account
            val matchingDoc = docs.firstOrNull { (id, fields) ->
                val staffEmail = fields["email"]?.toString() ?: ""
                val staffRole = fields["role"]?.toString() ?: ""
                val staffPasswordHash = fields["passwordHash"]?.toString() ?: ""

                staffEmail.equals(email, ignoreCase = true) &&
                        staffRole == role.name &&
                        staffPasswordHash == passwordHash
            }

            if (matchingDoc != null) {
                val (id, fields) = matchingDoc
                AdminAccountEntity(
                    id = id,
                    email = fields["email"]?.toString() ?: "",
                    passwordHash = fields["passwordHash"]?.toString() ?: "",
                    role = fields["role"]?.toString() ?: "",
                    name = fields["name"]?.toString() ?: "",
                    roomNumber = fields["roomNumber"]?.toString() ?: "",      // ✅ Now matches entity
                    specialization = fields["specialization"]?.toString() ?: "" // ✅ Now matches entity
                )
            } else {
                null
            }
        } catch (e: Exception) {
            println("🔴 StaffFirestoreRepository.authenticateStaff error: ${e.message}")
            null
        }
    }

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

    suspend fun getAllDoctors(): List<StaffAccount> =
        getAllStaff().filter { it.role == StaffRole.DOCTOR.name }
            .sortedBy { it.name }

    suspend fun setOnDuty(staffId: String, isOnDuty: Boolean) {
        try {
            firestoreClient.updateDocument(
                collection = "staff_accounts",
                documentId = staffId,
                fields = mapOf(
                    "isOnDuty" to isOnDuty,
                    "lastSeenAt" to System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            println("🔴 StaffFirestoreRepository.setOnDuty error: ${e.message}")
        }
    }

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

    suspend fun createStaff(staff: StaffAccount, password: String): Result<Unit> = runCatching {
        val passwordHash = hashPassword(password)

        firestoreClient.setDocument(
            collection = "staff_accounts",
            documentId = staff.id,
            fields = mapOf(
                "name" to staff.name,
                "email" to staff.email,
                "role" to staff.role,
                "roomNumber" to staff.roomNumber,
                "specialization" to staff.specialization,
                "isOnDuty" to false,
                "lastSeenAt" to System.currentTimeMillis(),
                "passwordHash" to passwordHash
            )
        )
    }

    suspend fun deleteStaff(staffId: String) {
        try {
            firestoreClient.deleteDocument("staff_accounts", staffId)
        } catch (e: Exception) {
            println("🔴 StaffFirestoreRepository.deleteStaff error: ${e.message}")
        }
    }

    private fun hashPassword(password: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}