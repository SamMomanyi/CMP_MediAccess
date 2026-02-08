package org.sammomanyi.mediaccess.features.identity.domain.use_case

import org.sammomanyi.mediaccess.core.domain.util.DataError
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.features.identity.domain.model.User
import org.sammomanyi.mediaccess.features.identity.domain.repository.IdentityRepository

class RegisterUserUseCase(
    private val repository: IdentityRepository
) {
    suspend operator fun invoke(user: User, password: String): Result<Unit, DataError> {
        // Validate names
        if (user.firstName.isBlank()) {
            return Result.Error(DataError.Validation.EMPTY_FIELD)
        }
        if (user.lastName.isBlank()) {
            return Result.Error(DataError.Validation.EMPTY_FIELD)
        }

        // Validate email
        if (user.email.isBlank()) {
            return Result.Error(DataError.Validation.EMPTY_FIELD)
        }
        if (!isValidEmail(user.email)) {
            return Result.Error(DataError.Validation.INVALID_EMAIL)
        }

        // Validate phone number
        if (user.phoneNumber.isBlank()) {
            return Result.Error(DataError.Validation.EMPTY_FIELD)
        }
        if (!isValidPhoneNumber(user.phoneNumber)) {
            return Result.Error(DataError.Validation.INVALID_PHONE_NUMBER)
        }

        // Validate password
        val passwordValidation = validatePassword(password)
        if (passwordValidation != null) {
            return Result.Error(passwordValidation)
        }

        // Validate medical ID format
        if (!isValidMedicalId(user.medicalId)) {
            return Result.Error(DataError.Validation.INVALID_MEDICAL_ID)
        }

        return repository.signUp(user, password)
    }

    private fun validatePassword(password: String): DataError.Validation? {
        return when {
            password.length < 8 -> DataError.Validation.PASSWORD_TOO_SHORT
            !password.any { it.isDigit() } -> DataError.Validation.PASSWORD_TOO_WEAK
            !password.any { it.isUpperCase() } -> DataError.Validation.PASSWORD_TOO_WEAK
            !password.any { it.isLowerCase() } -> DataError.Validation.PASSWORD_TOO_WEAK
            else -> null
        }
    }

    private fun isValidEmail(email: String): Boolean {
        // Standard email Regex that works across all KMP targets
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        // Simple validation - adjust for your country's format
        return phone.matches(Regex("^\\+?[1-9]\\d{1,14}\$"))
    }

    private fun isValidMedicalId(medicalId: String): Boolean {
        // Define your medical ID format
        // Example: MED-XXXXXX (3 letters, hyphen, 6 digits)
        return medicalId.matches(Regex("^MED-\\d{6}\$"))
    }
}