package org.sammomanyi.mediaccess.features.identity.domain.use_case

import org.sammomanyi.mediaccess.core.domain.util.DataError
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.features.identity.domain.repository.IdentityRepository

class GoogleSignInUseCase(
    private val repository: IdentityRepository
) {
    suspend operator fun invoke(
        idToken: String,
        email: String,
        displayName: String,
        photoUrl: String?
    ): Result<Unit, DataError> {
        if (idToken.isBlank()) {
            return Result.Error(DataError.Validation.EMPTY_FIELD)
        }

        if (email.isBlank()) {
            return Result.Error(DataError.Validation.INVALID_EMAIL)
        }

        return repository.loginWithGoogle(idToken, email, displayName, photoUrl)
    }
}