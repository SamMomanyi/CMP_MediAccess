package org.sammomanyi.mediaccess.features.identity.presentation.verification

sealed interface VerificationAction {
    data class OnCodeChange(val code: String) : VerificationAction
    data class OnVerifierNameChange(val name: String) : VerificationAction
    data object OnVerifyClick : VerificationAction
    data class OnVerifyCode(val code: String) : VerificationAction
}