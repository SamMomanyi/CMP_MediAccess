package org.sammomanyi.mediaccess.features.identity.domain.auth

sealed class GoogleSignInResult {
    data class Success(
        val idToken: String,
        val email: String,
        val displayName: String,
        val photoUrl: String?,
        val userId: String
    ) : GoogleSignInResult()

    data class Error(val message: String) : GoogleSignInResult()
    data object Cancelled : GoogleSignInResult()
}