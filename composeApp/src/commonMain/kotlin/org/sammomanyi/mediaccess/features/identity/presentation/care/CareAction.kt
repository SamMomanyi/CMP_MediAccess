package org.sammomanyi.mediaccess.features.identity.presentation.care

import org.sammomanyi.mediaccess.features.identity.domain.model.Hospital

sealed interface CareAction {
    data object OnInitiateVisit : CareAction
    data class OnSearchQueryChange(val query: String) : CareAction
    data object OnFilterClick : CareAction
    data object OnToggleNearby : CareAction
    data class OnHospitalClick(val hospital: Hospital) : CareAction
    data class OnLocationUpdated(val latitude: Double, val longitude: Double) : CareAction
}