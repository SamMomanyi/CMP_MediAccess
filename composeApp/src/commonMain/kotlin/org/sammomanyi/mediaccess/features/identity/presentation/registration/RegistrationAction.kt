package org.sammomanyi.mediaccess.features.identity.presentation.registration

sealed interface RegistrationAction {
    data class OnFirstNameChange(val name: String) : RegistrationAction
    data class OnLastNameChange(val name: String) : RegistrationAction
    data class OnEmailChange(val email: String) : RegistrationAction
    data class OnPhoneNumberChange(val phone: String) : RegistrationAction
    data class OnPasswordChange(val password: String) : RegistrationAction
    data class OnConfirmPasswordChange(val password: String) : RegistrationAction
    data object OnRegisterClick : RegistrationAction
}