package org.sammomanyi.mediaccess.features.identity.presentation.verification

import org.sammomanyi.mediaccess.core.presentation.UiText
import org.sammomanyi.mediaccess.features.identity.domain.model.Appointment

data class VerificationState(
    val visitCode: String = "",
    val verifierName: String = "",
    val isLoading: Boolean = false,
    val pendingAppointments: List<Appointment> = emptyList(),
    val verifiedAppointment: Appointment? = null,
    val errorMessage: UiText? = null
)