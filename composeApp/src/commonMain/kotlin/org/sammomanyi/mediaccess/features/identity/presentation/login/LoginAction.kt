package org.sammomanyi.mediaccess.features.identity.presentation.login

sealed interface LoginAction {
    data class OnEmailChange(val email: String) : LoginAction
    data class OnPasswordChange(val password: String) : LoginAction
    data object OnLoginClick : LoginAction
    data class OnGoogleSignIn(
        val idToken: String,
        val email: String,
        val displayName: String,
        val photoUrl: String?
    ) : LoginAction
}