package org.sammomanyi.mediaccess.features.identity.presentation.personal

sealed interface PersonalAction {
    data object OnEditProfile : PersonalAction
    data object OnAddRecoveryEmail : PersonalAction
    data object OnAddRecoveryPhone : PersonalAction
    data object OnConfirmTopics : PersonalAction
}