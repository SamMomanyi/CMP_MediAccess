package org.sammomanyi.mediaccess.features.identity.presentation.login

sealed interface LoginAction {
    data class OnEmailChange(val email: String) : LoginAction
    data class OnPasswordChange(val password: String) : LoginAction
    object OnLoginClick : LoginAction
}