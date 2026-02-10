package org.sammomanyi.mediaccess.features.identity.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.core.presentation.UiText
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GetProfileUseCase
import org.sammomanyi.mediaccess.features.identity.domain.use_case.LogoutUseCase

class ProfileViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val onLogoutSuccess: () -> Unit
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = combine(_state, getProfileUseCase()) { currentState, user ->
        currentState.copy(user = user)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileState())

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.OnLogoutClick -> {
                _state.update { it.copy(showLogoutDialog = true) }
            }
            ProfileAction.OnConfirmLogout -> {
                logout()
            }
            ProfileAction.OnDismissLogoutDialog -> {
                _state.update { it.copy(showLogoutDialog = false) }
            }
            ProfileAction.OnEditProfile -> {
                // TODO: Navigate to edit profile screen
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showLogoutDialog = false) }

            when (logoutUseCase()) {
                is Result.Success -> {
                    onLogoutSuccess()
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = UiText.DynamicString("Failed to logout. Please try again.")
                        )
                    }
                }
            }
        }
    }
}