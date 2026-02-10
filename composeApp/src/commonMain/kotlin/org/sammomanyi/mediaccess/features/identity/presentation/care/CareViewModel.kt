package org.sammomanyi.mediaccess.features.identity.presentation.care

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GetHospitalsUseCase
import org.sammomanyi.mediaccess.features.identity.domain.use_case.SyncHospitalsUseCase

class CareViewModel(
    private val getHospitalsUseCase: GetHospitalsUseCase,
    private val syncHospitalsUseCase: SyncHospitalsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CareState())
    val state = _state.asStateFlow()

    init {
        loadHospitals()
    }

    private fun loadHospitals() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Sync from Firestore
            syncHospitalsUseCase()

            // Observe local hospitals
            getHospitalsUseCase().collectLatest { hospitals ->
                _state.update {
                    it.copy(
                        hospitals = filterHospitals(hospitals, it),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onAction(action: CareAction) {
        when (action) {
            is CareAction.OnSearchQueryChange -> {
                _state.update {
                    it.copy(
                        searchQuery = action.query,
                        hospitals = filterHospitals(state.value.hospitals, it.copy(searchQuery = action.query))
                    )
                }
            }
            CareAction.OnToggleNearby -> {
                _state.update {
                    it.copy(
                        showNearbyOnly = !it.showNearbyOnly,
                        hospitals = filterHospitals(state.value.hospitals, it.copy(showNearbyOnly = !it.showNearbyOnly))
                    )
                }
            }
            is CareAction.OnLocationUpdated -> {
                _state.update {
                    it.copy(
                        userLatitude = action.latitude,
                        userLongitude = action.longitude
                    )
                }
            }
            is CareAction.OnHospitalClick -> {
                // TODO: Navigate to hospital details
            }
            CareAction.OnFilterClick -> {
                // TODO: Show filter dialog
            }
            CareAction.OnInitiateVisit -> {
                // TODO: Navigate to visit initiation
            }
        }
    }

    private fun filterHospitals(
        hospitals: List<org.sammomanyi.mediaccess.features.identity.domain.model.Hospital>,
        currentState: CareState
    ): List<org.sammomanyi.mediaccess.features.identity.domain.model.Hospital> {
        var filtered = hospitals

        // Filter by search query
        if (currentState.searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.name.contains(currentState.searchQuery, ignoreCase = true) ||
                        it.address.contains(currentState.searchQuery, ignoreCase = true)
            }
        }

        // Filter by nearby
        if (currentState.showNearbyOnly && currentState.userLatitude != null && currentState.userLongitude != null) {
            filtered = filtered.filter {
                it.distanceFrom(currentState.userLatitude, currentState.userLongitude) <= 10.0 // 10km radius
            }.sortedBy {
                it.distanceFrom(currentState.userLatitude, currentState.userLongitude)
            }
        }

        return filtered
    }
}