import org.sammomanyi.mediaccess.core.domain.util.DataError
import org.sammomanyi.mediaccess.features.identity.domain.model.User
import org.sammomanyi.mediaccess.features.identity.domain.repository.IdentityRepository

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