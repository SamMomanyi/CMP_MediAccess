package org.sammomanyi.mediaccess.features.identity.presentation.registration

import org.sammomanyi.mediaccess.core.presentation.UiText

data class RegistrationState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val confirmPassword: String = "",

    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,

    // Errors
    val errorMessage: UiText? = null,
    val firstNameError: UiText? = null,
    val lastNameError: UiText? = null,
    val emailError: UiText? = null,
    val phoneError: UiText? = null,
    val passwordError: UiText? = null
)