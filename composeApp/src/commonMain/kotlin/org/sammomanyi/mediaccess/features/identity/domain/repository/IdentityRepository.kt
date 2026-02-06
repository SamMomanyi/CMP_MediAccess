package org.sammomanyi.mediaccess.features.identity.domain.repository

import kotlinx.coroutines.flow.Flow
import org.sammomanyi.mediaccess.core.domain.util.DataError
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.features.identity.domain.model.User

interface IdentityRepository {
    fun getLocalUser(): Flow<User?>
    suspend fun generateVisitCode(userId: String): Result<String, DataError>
    suspend fun verifyVisitCode(code: String): Result<User, DataError>
}