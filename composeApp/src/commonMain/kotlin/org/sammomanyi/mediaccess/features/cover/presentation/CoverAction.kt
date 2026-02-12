package org.sammomanyi.mediaccess.features.cover.presentation

sealed interface CoverAction {
    data object OnLinkCoverClick : CoverAction
    data class OnCountrySelected(val country: String) : CoverAction
    data class OnInsuranceSelected(val insurance: String) : CoverAction
    data class OnInsuranceSearchChanged(val query: String) : CoverAction
    data class OnMemberNumberChanged(val number: String) : CoverAction
    data object OnLinkAccountClick : CoverAction   // Step 1 button
    data object OnSubmitClick : CoverAction         // Step 2 button
    data object OnDismissError : CoverAction
    data object OnReset : CoverAction

    // Admin actions (desktop)
    data class OnApproveRequest(val id: String) : CoverAction
    data class OnRejectRequest(val id: String, val note: String) : CoverAction
}

val KENYAN_INSURERS = listOf(
    "NHIF",
    "AAR Insurance",
    "Jubilee Health Insurance",
    "Britam Health Insurance",
    "Madison Insurance",
    "Resolution Insurance",
    "CIC Insurance",
    "Sanlam Kenya",
    "GA Insurance",
    "Kenindia Assurance",
    "MP SHAH",
    "MSO DISCOVERY KENYA",
    "MERIDIAN HEALTH GROUP",
    "ALUPE UNIVERSITY COLLEGE",
    "NYANZA REPRODUCTIVE HEALTH SOCIETY",
    "IMANI HEALTH WALLET",
    "Minet Kenya",
    "Heritage Insurance",
    "APA Insurance",
    "Old Mutual Kenya"
)

val COUNTRIES = listOf("KENYA", "UGANDA", "TANZANIA", "RWANDA", "ETHIOPIA")