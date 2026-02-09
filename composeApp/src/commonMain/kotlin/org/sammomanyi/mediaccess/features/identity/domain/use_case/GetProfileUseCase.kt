package org.sammomanyi.mediaccess.features.identity.domain.use_case

import kotlinx.coroutines.flow.Flow
import org.sammomanyi.mediaccess.features.identity.domain.model.User
import org.sammomanyi.mediaccess.features.identity.domain.repository.IdentityRepository

class GetProfileUseCase(
    private val repository: IdentityRepository
) {
    operator fun invoke(): Flow<User?> {
        return repository.getLocalUser()
    }
}