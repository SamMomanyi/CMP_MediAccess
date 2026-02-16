package org.sammomanyi.mediaccess.features.identity.domain.use_case

import kotlinx.coroutines.flow.Flow
import org.sammomanyi.mediaccess.core.domain.util.DataError
import org.sammomanyi.mediaccess.core.domain.util.Result
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
    ): Result<VisitCode, DataError> {
        if (userId.isBlank()) {
            return Result.Error(DataError.Validation.EMPTY_FIELD)
        }

        return repository.generateVisitCode(userId, purpose)
    }
}



