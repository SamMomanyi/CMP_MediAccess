package org.sammomanyi.mediaccess.features.identity.presentation.hospitals

import org.sammomanyi.mediaccess.core.presentation.UiText
import org.sammomanyi.mediaccess.features.identity.domain.model.Hospital

data class HospitalsState(
    val hospitals: List<Hospital> = emptyList(),
    val selectedHospital: Hospital? = null,
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val showBookingDialog: Boolean = false,
    val bookingPurpose: String = "",
    val errorMessage: UiText? = null,
    val successMessage: UiText? = null
)