package org.sammomanyi.mediaccess.features.admin.data

import kotlinx.datetime.Clock
import org.sammomanyi.mediaccess.core.data.database.AdminDao
import org.sammomanyi.mediaccess.core.data.database.AdminEntity
import org.sammomanyi.mediaccess.features.admin.domain.model.Admin
import java.security.MessageDigest
import java.util.UUID

class AdminRepository(private val adminDao: AdminDao) {

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<Admin> {
        return try {
            val existing = adminDao.getAdminByEmail(email.trim().lowercase())
            if (existing != null) {
                return Result.failure(Exception("An account with this email already exists"))
            }

            val entity = AdminEntity(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                email = email.trim().lowercase(),
                passwordHash = hashPassword(password),
                role = "ADMIN",
                createdAt = Clock.System.now().toEpochMilliseconds()
            )
            adminDao.insertAdmin(entity)
            Result.success(entity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<Admin> {
        return try {
            val entity = adminDao.getAdminByEmail(email.trim().lowercase())
                ?: return Result.failure(Exception("No account found with this email"))

            if (entity.passwordHash != hashPassword(password)) {
                return Result.failure(Exception("Incorrect password"))
            }

            Result.success(entity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun hasAnyAdmin(): Boolean {
        return adminDao.getAdminCount() > 0
    }
}

private fun AdminEntity.toDomain() = Admin(
    id = id,
    name = name,
    email = email,
    role = role,
    createdAt = createdAt
)