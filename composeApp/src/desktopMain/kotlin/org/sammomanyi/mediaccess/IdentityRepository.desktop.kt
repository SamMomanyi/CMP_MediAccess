package org.sammomanyi.mediaccess





import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.sammomanyi.mediaccess.core.domain.util.DataError
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.features.identity.domain.model.User
import org.sammomanyi.mediaccess.features.identity.domain.model.VisitCode
import org.sammomanyi.mediaccess.features.identity.domain.model.VisitPurpose
import org.sammomanyi.mediaccess.features.identity.domain.repository.IdentityRepository

class DesktopIdentityRepositoryImpl : IdentityRepository {

    override fun getLocalUser(): Flow<User?> {
        // Return empty flow - no user logged in on desktop
        return flowOf(null)
    }

    override suspend fun getCurrentUser(): Result<User?, DataError> {
        TODO("Not yet implemented")
    }

    override suspend fun signUp(user: User, password: String): Result<Unit, DataError> {
        return Result.Error(
            DataError.Network.UNKNOWN // or create a custom error type
        )
    }

    override suspend fun login(email: String, password: String): Result<Unit, DataError> {
        return Result.Error(
            DataError.Network.UNKNOWN // or create a custom error type
        )
    }

    override suspend fun logout(): Result<Unit, DataError> {
        TODO("Not yet implemented")
    }

    override suspend fun updateUserProfile(user: User): Result<Unit, DataError> {
        TODO("Not yet implemented")
    }

    override suspend fun resetPassword(email: String): Result<Unit, DataError> {
        TODO("Not yet implemented")
    }

    override suspend fun generateVisitCode(
        userId: String,
        purpose: VisitPurpose
    ): Result<VisitCode, DataError> {
        TODO("Not yet implemented")
    }



    override suspend fun verifyVisitCode(code: String): Result<User, DataError> {
        return Result.Error(
            DataError.Network.UNKNOWN
        )
    }

    override suspend fun getActiveVisitCode(userId: String): Result<VisitCode?, DataError> {
        TODO("Not yet implemented")
    }

    override suspend fun invalidateVisitCode(code: String): Result<Unit, DataError> {
        TODO("Not yet implemented")
    }

    override suspend fun markVisitCodeAsUsed(code: String): Result<Unit, DataError> {
        TODO("Not yet implemented")
    }
}