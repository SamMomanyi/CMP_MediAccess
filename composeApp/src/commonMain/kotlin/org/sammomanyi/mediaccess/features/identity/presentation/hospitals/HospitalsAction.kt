package org.sammomanyi.mediaccess.features.identity.presentation.hospitals

import org.sammomanyi.mediaccess.features.identity.domain.model.Hospital

sealed interface HospitalsAction {
    data object OnRefresh : HospitalsAction
    data class OnHospitalClick(val hospital: Hospital) : HospitalsAction
    data object OnDismissBooking : HospitalsAction
    data class OnPurposeChange(val purpose: String) : HospitalsAction
    data object OnConfirmBooking : HospitalsAction
}