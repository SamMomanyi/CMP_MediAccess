package org.sammomanyi.mediaccess.features.identity.presentation.login

import org.sammomanyi.mediaccess.core.presentation.UiText

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: UiText? = null,

    // Field-specific errors
    val emailError: UiText? = null,
    val passwordError: UiText? = null
)