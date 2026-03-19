package org.sammomanyi.mediaccess.features.identity.presentation.more.presentation

sealed interface MoreAction {
    object OnToggleTheme : MoreAction
    object OnHelpCenter : MoreAction
    object OnAbout : MoreAction
    object OnFeedback : MoreAction
    object OnSettings : MoreAction
    object OnNotifications : MoreAction
    object OnLanguage : MoreAction
    object OnPrivacy : MoreAction
    object OnTerms : MoreAction
    object OnLicenses : MoreAction
    object OnLogout : MoreAction
}