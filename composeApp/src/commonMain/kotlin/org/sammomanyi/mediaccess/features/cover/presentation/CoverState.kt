package org.sammomanyi.mediaccess.features.cover.presentation

import org.sammomanyi.mediaccess.features.cover.domain.model.CoverLinkRequest

data class CoverState(
    val requests: List<CoverLinkRequest> = emptyList(),
    val userEmail: String = "",
    val userId: String = "",

    // Link Cover dialog state
    val currentStep: Int = 1,
    val selectedCountry: String = "KENYA",
    val selectedInsurance: String = "",
    val memberNumber: String = "",
    val insuranceSearch: String = "",
    val isLoading: Boolean = false,
    val loadingMessage: String = "",
    val submitSuccess: Boolean = false,
    val errorMessage: String? = null
)
