package org.sammomanyi.mediaccess.features.identity.presentation.link_cover

// UI State
data class LinkCoverState(
    val activeTab: Int = 1,             // 1 = automatic, 2 = manual
    val selectedCountry: String = "KENYA",
    val selectedInsurance: String = "",
    val memberNumber: String = "",
    val userEmail: String = "",
    val userId: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val availableInsurances: List<String> = KENYAN_INSURERS,
    val loadingMessage: String = "Please Wait..."
)

// Dropdown Data
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

