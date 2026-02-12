package org.sammomanyi.mediaccess.features.identity.presentation.link_cover

// UI State
data class LinkCoverUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val activeTab: Int = 1, // 1 = Automatic, 2 = Manual

    // User Data
    val userEmail: String = "",
    val userId: String = "",

    // Form Data
    val selectedCountry: String = "KENYA",
    val selectedInsurance: String = "",
    val memberNumber: String = "",

    // Dropdown Data
    val availableInsurances: List<String> = listOf(
        "MP SHAH", "AAR INSURANCE", "JUBILEE", "BRITAM",
        "NHIF", "MSO DISCOVERY KENYA", "MERIDIAN HEALTH GROUP"
    )
)
