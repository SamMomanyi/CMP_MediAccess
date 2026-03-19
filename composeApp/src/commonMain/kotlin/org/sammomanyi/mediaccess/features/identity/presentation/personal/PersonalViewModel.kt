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
                        recoveryEmails = if (user != null) listOf(user.email ?: "") else emptyList()
                    )
                }
            }
        }
    }

    fun onAction(action: PersonalAction) {
        when (action) {
            is PersonalAction.OnEditProfile -> {
                viewModelScope.launch {
                    // TODO: Update user profile in database
                    println("✅ Updating profile to: ${action.newName}")
                }
            }
            is PersonalAction.OnAddRecoveryEmail -> {
                _state.update {
                    it.copy(recoveryEmails = it.recoveryEmails + action.email)
                }
            }
            is PersonalAction.OnRemoveRecoveryEmail -> {
                _state.update {
                    it.copy(recoveryEmails = it.recoveryEmails - action.email)
                }
            }
            is PersonalAction.OnAddRecoveryPhone -> {
                _state.update {
                    it.copy(recoveryPhones = it.recoveryPhones + action.phone)
                }
            }
            is PersonalAction.OnRemoveRecoveryPhone -> {
                _state.update {
                    it.copy(recoveryPhones = it.recoveryPhones - action.phone)
                }
            }
            is PersonalAction.OnToggleTopic -> {
                _state.update { state ->
                    val currentTopics = state.selectedTopics
                    val newTopics = if (currentTopics.contains(action.topic)) {
                        currentTopics - action.topic
                    } else {
                        currentTopics + action.topic
                    }
                    state.copy(selectedTopics = newTopics)
                }
            }
            PersonalAction.OnConfirmTopics -> {
                viewModelScope.launch {
                    // TODO: Save topics to database
                    println("✅ Saving topics: ${_state.value.selectedTopics}")
                }
            }
        }
    }
}
