package org.sammomanyi.mediaccess.features.identity.presentation.link_cover

sealed interface LinkCoverAction {
    data class ChangeTab(val tab: Int) : LinkCoverAction
    data class SelectInsurance(val insurance: String) : LinkCoverAction
    data class EnterMemberNumber(val number: String) : LinkCoverAction
    data class SelectCountry(val country: String) : LinkCoverAction
    data object Submit : LinkCoverAction
    data object DismissError : LinkCoverAction

}