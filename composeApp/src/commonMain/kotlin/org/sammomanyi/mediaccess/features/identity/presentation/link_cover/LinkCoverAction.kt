package org.sammomanyi.mediaccess.features.identity.presentation.link_cover

sealed interface LinkCoverAction {
    data class ChangeTab(val tabIndex: Int) : LinkCoverAction
    data class SelectInsurance(val name: String) : LinkCoverAction
    data class EnterMemberNumber(val number: String) : LinkCoverAction
    data object Submit : LinkCoverAction
    data object ClearError : LinkCoverAction
}