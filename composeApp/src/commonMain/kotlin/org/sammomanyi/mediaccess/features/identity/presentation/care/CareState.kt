package org.sammomanyi.mediaccess.features.identity.presentation.care

import org.sammomanyi.mediaccess.core.presentation.UiText
import org.sammomanyi.mediaccess.features.identity.domain.model.Hospital

data class CareState(
    val hospitals: List<Hospital> = emptyList(),
    val searchQuery: String = "",
    val showNearbyOnly: Boolean = false,
    val isLoading: Boolean = false,
    val userLatitude: Double? = null,
    val userLongitude: Double? = null,
    val errorMessage: UiText? = null
)