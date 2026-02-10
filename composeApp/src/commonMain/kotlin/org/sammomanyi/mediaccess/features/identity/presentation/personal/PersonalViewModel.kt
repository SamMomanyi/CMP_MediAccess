package org.sammomanyi.mediaccess.features.identity.presentation.personal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.features.identity.domain.use_case.GetProfileUseCase

class PersonalViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val onLogoutSuccess: () -> Unit
) : ViewModel() {

    private val _state = MutableStateFlow(PersonalState())
    val state = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            getProfileUseCase().collectLatest { user ->
                _state.update {
                    it.copy(
                        user = user,
                        recoveryEmails = if (user != null) listOf(user.email) else emptyList()
                    )
                }
            }
        }
    }

    fun onAction(action: PersonalAction) {
        when (action) {
            PersonalAction.OnEditProfile -> {
                // TODO: Navigate to edit profile
            }
            PersonalAction.OnAddRecoveryEmail -> {
                // TODO: Show add email dialog
            }
            PersonalAction.OnAddRecoveryPhone -> {
                // TODO: Show add phone dialog
            }
            PersonalAction.OnConfirmTopics -> {
                // TODO: Save topics
            }
        }
    }
}