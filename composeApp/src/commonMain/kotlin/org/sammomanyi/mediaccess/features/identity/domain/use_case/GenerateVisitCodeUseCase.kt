import kotlinx.coroutines.flow.Flow
import org.sammomanyi.mediaccess.core.domain.util.DataError
import org.sammomanyi.mediaccess.features.identity.domain.model.User
import org.sammomanyi.mediaccess.features.identity.domain.model.VisitCode
import org.sammomanyi.mediaccess.features.identity.domain.model.VisitPurpose
import org.sammomanyi.mediaccess.features.identity.domain.repository.IdentityRepository

// GenerateVisitCodeUseCase.kt
class GenerateVisitCodeUseCase(
    private val repository: IdentityRepository
) {
    suspend operator fun invoke(
        userId: String,
        purpose: VisitPurpose = VisitPurpose.GENERAL_VISIT
    ): org.sammomanyi.mediaccess.core.domain.util.Result<VisitCode, DataError> {
        return repository.generateVisitCode(userId, purpose)
    }
}

// VerifyVisitCodeUseCase.kt
class VerifyVisitCodeUseCase(
    private val repository: IdentityRepository
) {
    suspend operator fun invoke(code: String): org.sammomanyi.mediaccess.core.domain.util.Result<User, DataError> {
        // Validate code format
        if (code.length != 6) {
            return org.sammomanyi.mediaccess.core.domain.util.Result.Error(DataError.Validation.INVALID_MEDICAL_ID)
        }

        return repository.verifyVisitCode(code)
    }
}

// GetCurrentUserUseCase.kt
class GetCurrentUserUseCase(
    private val repository: IdentityRepository
) {
    operator fun invoke(): Flow<User?> {
        return repository.getLocalUser()
    }
}