package org.sammomanyi.mediaccess.features.cover.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.features.cover.data.CoverRepository
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverLinkRequest
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverStatus
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GetProfileUseCase
class CoverViewModel(
    private val coverRepository: CoverRepository,
    private val getProfileUseCase: GetProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CoverState())
    val state = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            getProfileUseCase().collectLatest { user ->
                user?.let {
                    _state.update { s ->
                        s.copy(
                            userEmail = user.email ?: "",
                            userId = user.id ?: ""
                        )
                    }
                    // Load requests for this user
                    coverRepository.getUserRequests(user.id ?: "")
                        .collectLatest { requests ->
                            _state.update { s -> s.copy(requests = requests) }
                        }
                }
            }
        }
    }

    fun onAction(action: CoverAction) {
        when (action) {
            CoverAction.OnReset -> _state.update {
                it.copy(
                    currentStep = 1,
                    selectedInsurance = "",
                    memberNumber = "",
                    insuranceSearch = "",
                    isLoading = false,
                    submitSuccess = false,
                    errorMessage = null
                )
            }

            is CoverAction.OnCountrySelected ->
                _state.update { it.copy(selectedCountry = action.country) }

            is CoverAction.OnInsuranceSelected ->
                _state.update { it.copy(selectedInsurance = action.insurance) }

            is CoverAction.OnInsuranceSearchChanged ->
                _state.update { it.copy(insuranceSearch = action.query) }

            is CoverAction.OnMemberNumberChanged ->
                _state.update { it.copy(memberNumber = action.number) }

            CoverAction.OnDismissError ->
                _state.update { it.copy(errorMessage = null) }

            // Step 1: Try automatic linkage
            CoverAction.OnLinkAccountClick -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true, loadingMessage = "Please Wait...") }
                    delay(2500) // Simulate API call
                    // Automatic linkage always "fails" in mock → advance to step 2
                    _state.update {
                        it.copy(
                            isLoading = false,
                            currentStep = 2
                        )
                    }
                }
            }

            // Step 2: Submit manual request
            CoverAction.OnSubmitClick -> {
                val s = _state.value
                if (s.selectedInsurance.isBlank()) {
                    _state.update { it.copy(errorMessage = "Please select an insurance provider") }
                    return
                }
                if (s.memberNumber.isBlank()) {
                    _state.update { it.copy(errorMessage = "Please enter your member number") }
                    return
                }

                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true, loadingMessage = "Submitting request...") }

                    val result = coverRepository.submitRequest(
                        userId = s.userId,
                        userEmail = s.userEmail,
                        country = s.selectedCountry,
                        insuranceName = s.selectedInsurance,
                        memberNumber = s.memberNumber
                    )

                    result.fold(
                        onSuccess = {
                            _state.update { it.copy(isLoading = false, submitSuccess = true) }
                        },
                        onFailure = { e ->
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "Failed to submit: ${e.message}"
                                )
                            }
                        }
                    )
                }
            }

            // Desktop admin actions
            is CoverAction.OnApproveRequest -> {
                viewModelScope.launch {
                    coverRepository.approveRequest(action.id)
                }
            }

            is CoverAction.OnRejectRequest -> {
                viewModelScope.launch {
                    coverRepository.rejectRequest(action.id, action.note)
                }
            }

            else -> {}
        }
    }
}