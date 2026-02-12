package org.sammomanyi.mediaccess.features.identity.presentation.link_cover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.features.cover.data.CoverRepository
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GetProfileUseCase



class LinkCoverViewModel(
    private val coverRepository: CoverRepository,
    private val getProfileUseCase: GetProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LinkCoverState())
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
                }
            }
        }
    }

    fun onAction(action: LinkCoverAction) {
        when (action) {
            is LinkCoverAction.ChangeTab ->
                _state.update { it.copy(activeTab = action.tab, error = null) }

            is LinkCoverAction.SelectInsurance ->
                _state.update { it.copy(selectedInsurance = action.insurance) }

            is LinkCoverAction.EnterMemberNumber ->
                _state.update { it.copy(memberNumber = action.number) }

            is LinkCoverAction.SelectCountry ->
                _state.update { it.copy(selectedCountry = action.country) }

            LinkCoverAction.DismissError ->
                _state.update { it.copy(error = null) }

            LinkCoverAction.Submit -> handleSubmit()
        }
    }

    private fun handleSubmit() {
        val s = _state.value

        when (s.activeTab) {
            // ── Step 1: Automatic linkage ──────────────────
            1 -> {
                viewModelScope.launch {
                    _state.update {
                        it.copy(isLoading = true, loadingMessage = "Please Wait...")
                    }
                    delay(2500) // Simulate API lookup
                    // Mock: automatic always fails → advance to manual tab
                    _state.update {
                        it.copy(isLoading = false, activeTab = 2)
                    }
                }
            }

            // ── Step 2: Manual linkage ─────────────────────
            2 -> {
                if (s.selectedInsurance.isBlank()) {
                    _state.update { it.copy(error = "Please select an insurance provider") }
                    return
                }
                if (s.memberNumber.isBlank()) {
                    _state.update { it.copy(error = "Please enter your member number") }
                    return
                }

                viewModelScope.launch {
                    _state.update {
                        it.copy(isLoading = true, loadingMessage = "Submitting request...")
                    }

                    val result = coverRepository.submitRequest(
                        userId = s.userId,
                        userEmail = s.userEmail,
                        country = s.selectedCountry,
                        insuranceName = s.selectedInsurance,
                        memberNumber = s.memberNumber
                    )

                    result.fold(
                        onSuccess = {
                            _state.update { it.copy(isLoading = false, isSuccess = true) }
                        },
                        onFailure = { e ->
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Failed to submit. Please try again."
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}