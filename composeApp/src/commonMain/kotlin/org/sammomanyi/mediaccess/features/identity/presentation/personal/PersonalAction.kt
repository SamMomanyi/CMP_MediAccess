package org.sammomanyi.mediaccess.features.identity.presentation.personal

sealed interface PersonalAction {
    data class OnEditProfile(val newName: String) : PersonalAction
    data class OnAddRecoveryEmail(val email: String) : PersonalAction
    data class OnRemoveRecoveryEmail(val email: String) : PersonalAction
    data class OnAddRecoveryPhone(val phone: String) : PersonalAction
    data class OnRemoveRecoveryPhone(val phone: String) : PersonalAction
    data class OnToggleTopic(val topic: String) : PersonalAction
    object OnConfirmTopics : PersonalAction
}