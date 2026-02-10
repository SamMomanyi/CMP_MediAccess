package org.sammomanyi.mediaccess.features.identity.domain.use_case

import org.sammomanyi.mediaccess.core.domain.util.DataError
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.features.identity.domain.repository.IdentityRepository

class LogoutUseCase(
    private val repository: IdentityRepository
) {
    suspend operator fun invoke(): Result<Unit, DataError> {
        return repository.logout()
    }
}