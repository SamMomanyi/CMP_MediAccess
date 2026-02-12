package org.sammomanyi.mediaccess.features.identity.presentation.link_cover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.features.identity.domain.model.CoverLinkRequest
import org.sammomanyi.mediaccess.features.identity.domain.model.LinkRequestType
import org.sammomanyi.mediaccess.features.identity.domain.repository.IdentityRepository

// UI State


// Actions

class LinkCoverViewModel(
    private val repository: IdentityRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LinkCoverUiState())
    val state = _state.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            when (val result = repository.getCurrentUser()) {
                is Result.Success -> {
                    result.data?.let { user ->
                        _state.update {
                            it.copy(userEmail = user.email, userId = user.id)
                        }
                    }
                }
                is Result.Error -> { /* Handle error if needed */ }
            }
        }
    }

    fun onAction(action: LinkCoverAction) {
        when (action) {
            is LinkCoverAction.ChangeTab -> {
                _state.update { it.copy(activeTab = action.tabIndex, error = null) }
            }
            is LinkCoverAction.SelectInsurance -> {
                _state.update { it.copy(selectedInsurance = action.name) }
            }
            is LinkCoverAction.EnterMemberNumber -> {
                _state.update { it.copy(memberNumber = action.number) }
            }
            LinkCoverAction.ClearError -> {
                _state.update { it.copy(error = null) }
            }
            LinkCoverAction.Submit -> submitRequest()
        }
    }

    private fun submitRequest() {
        val currentState = _state.value

        // Validation for Manual Tab
        if (currentState.activeTab == 2) {
            if (currentState.selectedInsurance.isBlank()) {
                _state.update { it.copy(error = "Please select an insurance provider") }
                return
            }
            if (currentState.memberNumber.isBlank()) {
                _state.update { it.copy(error = "Please enter your member number") }
                return
            }
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val request = CoverLinkRequest(
                userId = currentState.userId,
                userEmail = currentState.userEmail,
                requestType = if (currentState.activeTab == 1) LinkRequestType.AUTOMATIC else LinkRequestType.MANUAL,
                insuranceProviderName = if (currentState.activeTab == 2) currentState.selectedInsurance else null,
                memberNumber = if (currentState.activeTab == 2) currentState.memberNumber else null
            )

            // Submit logic
            repository.submitCoverLinkRequest(request).collect {
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            }
        }
    }
}