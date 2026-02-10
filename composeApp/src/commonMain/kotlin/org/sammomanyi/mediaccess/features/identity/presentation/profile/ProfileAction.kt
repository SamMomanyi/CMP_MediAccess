package org.sammomanyi.mediaccess.features.identity.presentation.profile

sealed interface ProfileAction {
    data object OnLogoutClick : ProfileAction
    data object OnConfirmLogout : ProfileAction
    data object OnDismissLogoutDialog : ProfileAction
    data object OnEditProfile : ProfileAction
}