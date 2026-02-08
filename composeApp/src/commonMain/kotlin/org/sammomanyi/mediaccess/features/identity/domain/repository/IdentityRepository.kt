package org.sammomanyi.mediaccess.features.identity.domain.repository

import kotlinx.coroutines.flow.Flow
import org.sammomanyi.mediaccess.core.domain.util.DataError
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.features.identity.domain.model.User
import org.sammomanyi.mediaccess.features.identity.domain.model.VisitCode
import org.sammomanyi.mediaccess.features.identity.domain.model.VisitPurpose

interface IdentityRepository {
    // User Management
    fun getLocalUser(): Flow<User?>
    suspend fun getCurrentUser(): Result<User?, DataError>
    suspend fun signUp(user: User, password: String): Result<Unit, DataError>
    suspend fun login(email: String, password: String): Result<Unit, DataError>
    suspend fun logout(): Result<Unit, DataError>
    suspend fun updateUserProfile(user: User): Result<Unit, DataError>
    suspend fun resetPassword(email: String): Result<Unit, DataError>

    // Visit Code Management (OTP)
    suspend fun generateVisitCode(
        userId: String,
        purpose: VisitPurpose = VisitPurpose.GENERAL_VISIT
    ): Result<VisitCode, DataError>

    suspend fun verifyVisitCode(code: String): Result<User, DataError>
    suspend fun getActiveVisitCode(userId: String): Result<VisitCode?, DataError>
    suspend fun invalidateVisitCode(code: String): Result<Unit, DataError>

    // For hospital staff to verify patient
    suspend fun markVisitCodeAsUsed(code: String): Result<Unit, DataError>
}