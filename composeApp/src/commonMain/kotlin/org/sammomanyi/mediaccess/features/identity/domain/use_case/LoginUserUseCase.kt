package org.sammomanyi.mediaccess.features.identity.domain.use_case

import org.sammomanyi.mediaccess.features.identity.domain.repository.IdentityRepository

class LoginUserUseCase(private val repository: IdentityRepository) {
    suspend operator fun invoke(email: String, password: String) =
        repository.login(email, password)
}